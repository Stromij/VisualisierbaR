package com.github.bachelorpraktikum.visualisierbar.absparser;


import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;

@ParametersAreNonnullByDefault
public class AbsParser {
    private static final Logger log = Logger.getLogger(AbsParser.class.getName());

    public AbsParser()
        {}

    @Nonnull
    public Document parse(String toStyle, Document doc) throws IOException {
        System.out.println(toStyle);
        CharStream stream = new ANTLRInputStream(toStyle);
        ABSLexer lexer = new ABSLexer(stream);
        lexer.removeErrorListeners();
        System.out.println("tmp4");
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ABSParser parser = new ABSParser(tokens);
        System.out.println("tmp3");

        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        ParseTree parseTree;
        System.out.println("tmp2");
        try {
            parseTree = parser.goal();  // STAGE 1
        } catch (Exception ex) {
            tokens.reset(); // rewind input stream
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parseTree = parser.goal();  // STAGE 2
            // if we parse ok, it's LL not SLL
        }
        System.out.println("tmp1");
        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener(doc);
        walker.walk(listener, parseTree);
        return doc;
    }


    private static class Listener extends ABSBaseListener {

        private Document doc;

        private final Style commentStyle = new StyleContext().addStyle("comment", null);

        Listener(Document doc)
            {this.doc = doc;

             StyleConstants.setForeground(commentStyle, new Color(13, 178,0));
             StyleConstants.setItalic(commentStyle, true);

            }


        private void insertText(String text, @Nullable Style style)
            {try {
                doc.insertString(0,text, style);
                }
             catch(BadLocationException e)
                {e.printStackTrace();
                 // Maybe log?
                }
            }

        @Override
        public void enterQualified_type_identifier(ABSParser.Qualified_type_identifierContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterQualified_identifier(ABSParser.Qualified_identifierContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterAny_identifier(ABSParser.Any_identifierContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterType_use(ABSParser.Type_useContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterType_exp(ABSParser.Type_expContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterParamlist(ABSParser.ParamlistContext ctx)
            {insertText(ctx.getText(), null);
            }

        @Override
        public void enterParam_decl(ABSParser.Param_declContext ctx)
            {insertText(ctx.getText(), null);
            }



    }
}
