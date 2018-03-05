package com.github.bachelorpraktikum.visualisierbar.logparser;

import com.github.bachelorpraktikum.visualisierbar.logparser.LogParser.MsgContext;
import com.github.bachelorpraktikum.visualisierbar.logparser.LogParser.RatContext;
import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.model.train.Train;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

@ParametersAreNonnullByDefault
public final class GraphParser {

    private static final Logger log = Logger.getLogger(GraphParser.class.getName());

    public GraphParser() {
    }

    @Nonnull
    private Context parse(@Nonnull CharStream input, @Nonnull Context context) {
        LogLexer lexer = new LogLexer(input);
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LogParser parser = new LogParser(tokens);
        parser.setErrorHandler(new DefaultErrorStrategy() {
            @Override
            public void reportError(Parser recognizer, RecognitionException e) {
                // TODO maybe log errors?
            }
        });
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        ParseTree parseTree;
        try {
            parseTree = parser.start();  // STAGE 1
        } catch (Exception ex) {
            tokens.reset(); // rewind input stream
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parseTree = parser.start();  // STAGE 2
            // if we parse ok, it's LL not SLL
        }

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener(context);
        walker.walk(listener, parseTree);
        return context;
    }

    @Nonnull
    public Context parse(String fileName) throws IOException {
        return parse(fileName, new Context());
    }

    @Nonnull
    public Context parse(String fileName, Context context) throws IOException {
        CharStream input = new ANTLRFileStream(fileName);
        return parse(input, context);
    }

    @Nonnull
    public Context parse(InputStream string) throws IOException {
        return parse(string, new Context());
    }

    @Nonnull
    public Context parse(InputStream input, Context context) throws IOException {
        ANTLRInputStream stream = new ANTLRInputStream(input);
        return parse(stream, context);
    }

    private static class Listener extends LogBaseListener {

        private final Context context;
        private final BigInteger thousandInt;

        private HashMap<String, Edge> elemViewTracker = new HashMap();
        private HashMap<String, Group> elemGroupTracker = new HashMap();

        Listener(Context context) {
            this.context = context;
            this.thousandInt = BigInteger.valueOf(1000);
        }

        private int createTime(LogParser.TimeContext ctx) {
            if (ctx.rat() != null) {
                RatContext ratContext = ctx.rat();
                BigInteger left = new BigInteger(ratContext.INT(0).getText());
                BigInteger right = new BigInteger(ratContext.INT(1).getText());
                return left.multiply(thousandInt).divide(right).intValue();
            }
            return Integer.parseInt(ctx.INT().getText()) * 1000;
        }

        private Coordinates createCoordinates(LogParser.CoordContext ctx) {
            int x = Integer.parseInt(ctx.INT(0).getText());
            int y = Integer.parseInt(ctx.INT(1).getText());
            return new Coordinates(x, y);
        }

        @Override
        public void enterNode(LogParser.NodeContext ctx) {
            try {
                String nodeName = ctx.node_name().getText();
                String nodeAbsName;
                try {nodeAbsName = ctx.node_abs_name().getText();}
                catch (NullPointerException e){nodeAbsName = null;}
                Coordinates coordinates = createCoordinates(ctx.coord());
                Node.in(context).create(nodeName, coordinates, nodeAbsName);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
        }

        @Override
        public void enterElem(LogParser.ElemContext ctx) {
            try {
                String elementName = ctx.elem_name().getText();
                String nodeName = ctx.node_name().getText();
                Node node = Node.in(context).get(nodeName);
                Element.State state = Element.State.fromName(ctx.STATE().getText());
                Element.Type type = Element.Type.fromName(elementName);
                Element elemNew = Element.in(context).create(elementName, type, node, state);

                if(elemViewTracker.get(elementName) != null)
                    {Edge view = elemViewTracker.get(elementName);
                     Node viewNode = view.getNode1().equals(node) ? view.getNode2() : view.getNode1();
                     elemNew.setDirection(viewNode);
                     elemViewTracker.remove(elementName);
                    }

                if(elemGroupTracker.get(elementName) != null)
                    {Group group = elemGroupTracker.get(elementName);
                     elemNew.setGroup(group);
                     group.addElement(elemNew);
                     elemGroupTracker.remove(elementName);
                    }
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
        }

        @Override
        public void enterEdge(LogParser.EdgeContext ctx) {
            try {
                String edgeName = ctx.edge_name().getText();
                String node1Name = ctx.node_name(0).getText();
                String node2Name = ctx.node_name(1).getText();
                int length = Integer.parseInt(ctx.INT().getText());

                Node node1 = Node.in(context).get(node1Name);
                Node node2 = Node.in(context).get(node2Name);
                String absName;
                try {absName = ctx.edge_abs_name().getText();}
                catch (NullPointerException e){absName = null;}
                Edge.in(context).create(edgeName, length, node1, node2, absName);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
        }

        @Override
        public void enterView(LogParser.ViewContext ctx)
            {try {String edgeName = ctx.edge_name().getText();
                  String elemName = ctx.elem_name().getText();
                  Edge edge1 = Edge.in(context).get(edgeName);
                  // Versuche die Edge zu finden - falls nicht vorhanden, schiebe sie in die HashMap
                  try{Element elem1 = Element.in(context).get(elemName);
                      Node viewNode = elem1.getNode().equals(edge1.getNode1()) ? edge1.getNode2() : edge1.getNode1();
                      elem1.setDirection(viewNode);
                     }
                  catch (IllegalArgumentException e)
                     {elemViewTracker.put(elemName, edge1);}
                 }
             catch (IllegalArgumentException e){
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage());
             }
            }

        @Override
        public void enterGroup(LogParser.GroupContext ctx)
            {try {String groupName = ctx.log_name().getText();
                  String kind = ctx.kind().getText();
                  Group group = new Group(groupName, kind);

                  for(int i = 0; ctx.elem_name(i) != null; i++)
                    {if(ctx.elem_name(i).getText().equals("null")) continue;
                     elemGroupTracker.put(ctx.elem_name(i).getText(), group);
                    }
            }
            catch (IllegalArgumentException e){
                log.warning("Could not parse line: " + ctx.getText()
                        + "\nReason: " + e.getMessage());
            }
            }


        @Override
        public void enterTrain(LogParser.TrainContext ctx) {
            try {
                String trainName = ctx.train_name().getText();
                String humanName = ctx.train_readable_name().getText();
                int length = Integer.parseInt(ctx.INT().getText());

                Train.in(context).create(trainName, humanName, length);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
        }

        @Override
        public void enterMv_init(LogParser.Mv_initContext ctx) {
            Train train = Train.in(context).get(ctx.train_name().getText());
            Edge edge = Edge.in(context).get(ctx.edge_name().getText());
            int time = createTime(ctx.time());
            train.eventFactory().init(time, edge);
        }

        @Override
        public void enterMv_speed(LogParser.Mv_speedContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            if (ctx.speed() == null) {
                train.eventFactory().move(time, distance);
            } else {
                int speed = Integer.parseInt(ctx.speed().INT().getText());
                train.eventFactory().speed(time, distance, speed);
            }
        }

        @Override
        public void enterMv_start(LogParser.Mv_startContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().reach(time, edge, distance);
        }

        @Override
        public void enterMv_leaves(LogParser.Mv_leavesContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().leave(time, edge, distance);
        }

        @Override
        public void enterMv_term(LogParser.Mv_termContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().terminate(time, distance);
        }

        @Override
        public void enterCh(LogParser.ChContext ctx) {
            String elementName = ctx.elem_name().getText();
            Element element = Element.in(context).get(elementName);
            Element.State state = Element.State.fromName(ctx.STATE().getText());
            int time = createTime(ctx.time());
            element.addEvent(state, time);
        }

        @Override
        public void enterMsg(MsgContext ctx) {
            int time = createTime(ctx.time());
            String text = ctx.message().getText();
            Node node = Node.in(context).get(ctx.node_name().getText());
            Messages.in(context).add(time, text, node);
        }
    }

}
