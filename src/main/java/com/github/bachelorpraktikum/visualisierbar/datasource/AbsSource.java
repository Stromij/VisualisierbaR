package com.github.bachelorpraktikum.visualisierbar.datasource;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Context;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import javafx.scene.control.Alert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class AbsSource implements DataSource {


    private final File originalFile;
    private final File fileToAbs;
    private final File fileToAbsSource;
    private final Context context;
    private final URI parent;
    private final String product;

    private final String name;

    private ArrayList<String> delta;


    public AbsSource(String command, File path, String product) throws IOException {
        // Problem: beim recompile ist er schon in Origin und legt wieder ein Origin an!

        this.originalFile = path;

        if(!path.toString().contains("origin")) {           // Falls er nicht im Origin-Ordner arbeitet
            // Kopiere den Originaldatensatz in den orign-Ordner
            long milis = System.currentTimeMillis();
            String copyCommand = String.format("source /etc/bash.bashrc; rm -r %s/origin/*; mkdir -p %s/origin/%s-%s/; cp -rf %s/*  %s/origin/%s-%s/",
                    originalFile.getParent(), originalFile.getParent(), milis, originalFile.getName(), originalFile.toString(), originalFile.getParent(), milis, originalFile.getName());
            System.out.println(copyCommand);
            String fileToConsole = "/bin/bash";
            String c = "-c";

            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(this.originalFile);
            builder.command(fileToConsole, c, copyCommand);

            Process p = builder.start();

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fileToAbsSource = new File(String.format("%s/origin/%s-%s/", originalFile.getParent(), milis, originalFile.getName()));
        }

        else
            {fileToAbsSource = path;}

        // Setzte restliche Paramter

        this.parent = new File(fileToAbsSource.getParent()).toURI();
        this.product = product;

        //Kompiliere
        if(command.contains("%s"))
            {command = String.format(command, fileToAbsSource.toString(), fileToAbsSource.getParent());}
        this.fileToAbs = compileABS(command);

        this.context = parseFile();
        this.delta = new ArrayList<>();

        int posOfName = path.getName().indexOf("-") == -1 ? 0 : path.getName().indexOf("-") + 1;
        name = path.getName().substring(posOfName);
        System.out.println(name);
    }

    /**
     * Compiles a given ABS-File
     * @param command The complete shell-command to compile the ABS
     * @return the path to the compiled ABS-file
     * @throws IOException throws an Error, when the Input is not valid e.g. it's not an ABS-File
     */
    private File compileABS(String command) throws IOException {
        File file = new File(String.format("%sactual.zug", this.parent.getPath()));
        String OS = System.getProperty("os.name").toLowerCase();
        String fileToConsole = "/bin/bash";
        String c = "-c";
        String printConsole = String.format("source /etc/bash.bashrc; rm -r gen/erlang/*; %s; cd ./gen/erlang; ./run > %sactual.zug;", command, this.parent.getPath());

        if(OS.contains("win"))
            {fileToConsole = "cmd.exe";
             c = "/c";
             printConsole = String.format("rmdir gen\\erlang /S /Q; %s; cd gen\\erlang; ./run > %s/actual.zug;", command, this.parent.getPath());
            }

        System.out.println(printConsole);

        ProcessBuilder builder = new ProcessBuilder();
        System.out.println(parent);
        builder.directory(new File(this.parent.getPath()));
        builder.command(fileToConsole, c, printConsole);


        // Debugging Output (Ausgabe der Konsolenrückgabe)
        // builder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT); //Process out auf stdout zum testen

        Process process = builder.start();

        // System zum Abbrechen, wenn die Simulation durchgelaufen ist.

        PrintStream stdout = System.out;

        System.setOut(new PrintStream(System.out) {
            Timestamp newTimestamp;

            public void println(String s) {
                if(newTimestamp == null) {startObservation();}
                newTimestamp = new Timestamp(System.currentTimeMillis());
                // super.print(s);  // Consume Console-Output. Only for debugging
            }

            public void startObservation()
                {Runnable runnable = () -> {super.println("Started process observation.");
                 long aktTime = System.currentTimeMillis();
                 while(newTimestamp == null || newTimestamp.getTime() + 10000 > aktTime) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(newTimestamp.getTime() - aktTime);
                    aktTime = System.currentTimeMillis();
                 }
                 if(process.isAlive()) {
                     process.destroy();
                     super.println("Destroyed process due to running out of time.");
                 }

                };
                 Thread thread = new Thread(runnable);
                 thread.start();
                }
        });

        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        new Thread(outputGobbler).start();

        try {
            int exitCode = process.waitFor();
            System.setOut(stdout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        return file;
    }



    class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumeInputLine;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
            this.inputStream = inputStream;
            this.consumeInputLine = consumeInputLine;
        }

        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
        }
    }

    private Context parseFile() throws IOException {
        return new GraphParser().parse(fileToAbs.getPath());
    }

    public String getNameOfOriginal()
        {return name;}

    @Nonnull
    @Override
    public Context getContext() {
        return context;
    }

    /**
     * This method parses all used Deltas from the given product from the file.
     * It collect them in the ArrayList @var delta and return them.
     *
     * @return An ArrayList of all Deltas
     */
    @Nonnull
    public ArrayList<String> getDeltas(){
        ArrayList<Matcher> foundedDeltaLines = new ArrayList<>();
        ArrayList<String> productNames = new ArrayList<>();
        Matcher matcherProduct;
        Matcher matcherDelta;

        Pattern patternDeltaLines = compile("(delta )(.*?)(( after .*?)?)( when )(.*?)(;)");
        Pattern patternProduct = compile("(product "+this.product+" \\()(.*?)(\\);)");

        try {
            String aktLine;
            BufferedReader reader = new BufferedReader( new FileReader(this.fileToAbsSource+"/Run.abs") );
            while((aktLine = reader.readLine()) != null)
                {matcherDelta = patternDeltaLines.matcher(aktLine);
                 if(matcherDelta.find())
                    {foundedDeltaLines.add(matcherDelta);continue;}

                 matcherProduct = patternProduct.matcher(aktLine);
                 if(matcherProduct.find())
                    {productNames = new ArrayList<>(Arrays.asList(matcherProduct.group(2).split(", "))); break;}
                }
            reader.close();

            // Suchen der Deltas

            for (String productName : productNames) {
                for (Matcher foundedDeltaLine : foundedDeltaLines) {
                    if (foundedDeltaLine.group(6).matches("(|.*? )" + productName + "(| .*?)")) {
                        this.delta.add(foundedDeltaLine.group(2));
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return this.delta;
    }


    /**
     * This method copies an existing ABS-Directory and changes the old ABS to the new one, generated by
     * the printToAbs-Method of Element, LogicalGroup, Node and Edge
     * @param graph iterating graph
     * @return returns the destination Directory
     */

    public File refactorSource(@Nullable Graph graph)
        {return refactorSource(graph, null);}

    public File refactorSource(@Nullable Graph graph, @Nullable File destDir)
        {if(destDir == null) {
            //TODO Main ersetzten
            destDir = new File( parent.getPath().concat(String.valueOf(System.currentTimeMillis())).concat("-").concat(name));
            if(!copyFiles(destDir)) {return null;}
         }


         String newCode = "";

         try {
                FileReader fr = new FileReader(destDir + "/Run.abs");
                BufferedReader br = new BufferedReader(fr);
                String deltaContent = "";

                /*
                 * Lade zuerst einmal das Grid in einen String (deltaContent) zum Finden fehlender Daten
                 */
                searchForDelta(br, newCode);
                String zeile;

                // Suche nach grid start. Wenn nicht gefunden, werfe einen Fehler
                while((zeile = br.readLine()) != null) {
                    if(zeile.toLowerCase().contains("grid start")) {break;}
                    if(zeile.toLowerCase().contains("delta")) {throw new IOException("Cannot find grid start!");}
                }

                BufferedReader contentReader = br;
                while ((zeile = contentReader.readLine()) != null)
                    {if(zeile.toLowerCase().contains("grid end")) {break;}
                     deltaContent = deltaContent.concat(zeile).concat("EOL");
                    }

                /*
                 * Beginne nun mit dem Einlesen der Datei für die Erstellung der neuen Datei
                 */

                fr = new FileReader(destDir + "/Run.abs");
                br = new BufferedReader(fr);
                // Suche nach dem Delta
                newCode = newCode.concat(searchForDelta(br, newCode));

                // Suche nach grid start. Wenn nicht gefunden, werfe einen Fehler
                while((zeile = br.readLine()) != null) {
                    newCode = newCode.concat(zeile).concat("\n");
                    if (zeile.toLowerCase().contains("grid start")) {
                        break;
                    }
                    if (zeile.toLowerCase().contains("delta")) {
                        throw new IOException("Cannot find grid start!");
                    }
                }

                String nodeAbs = graph.printNodesToAbs("\t\t");
                String edgeAbs = graph.printEdgesToAbs("\t\t");
                String elementAbs = graph.printElementsToAbs("\t\t", deltaContent);
                String logicalGroupAbs = graph.printLogicalGroupsToAbs("\t\t", deltaContent);

                // Füge Nodes und Edges in Datei ein
                newCode = newCode.concat(nodeAbs);
                newCode = newCode.concat("\n\n\n");
                newCode = newCode.concat(edgeAbs);
                newCode = newCode.concat("\n\n\n");



                // Füge alles, dessen Löschung ungewiss ist ein
                while((zeile = br.readLine()) != null)
                    {if (zeile.toLowerCase().contains("grid end")) {break;}
                     if (!(zeile.contains("new") || zeile.contains(".add") || zeile.contains(".set") || zeile.contains("SpeedLimiter") || zeile.replaceAll("(\t| )*", "").length() <= 2 || zeile.contains("//"))) {
                        boolean found = false;
                        for (Element.Type t : Element.Type.values()) {
                            if (zeile.toLowerCase().contains(t.getName().toLowerCase())) found = true;
                        }
                        if (!found) {
                            newCode = newCode.concat(zeile).concat("\n");
                        }
                    }
                }


                // Füge Elemente und LogicalGroups ein
                newCode = newCode.concat("\n\n\n");
                newCode = newCode.concat(elementAbs);
                newCode = newCode.concat("\n\n\n");
                newCode = newCode.concat(logicalGroupAbs);
                newCode = newCode.concat("\n\n\n");


                // Schreibe den Rest der Datei
                if(zeile != null)
                     {newCode = newCode.concat(zeile).concat("\n");}
                while((zeile = br.readLine()) != null)
                    {newCode = newCode.concat(zeile).concat("\n");}

                FileWriter fw = new FileWriter(destDir+"/Run.abs");
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(newCode);

                bw.close();
                fw.close();
                fr.close();
                br.close();
             System.out.println("----- newCode start -----");
                System.out.println(newCode);

                System.out.println("----- ABS start -----");
                System.out.println(nodeAbs.concat("\n").replace("\t\t", ""));
                System.out.println(edgeAbs.concat("\n").replace("\t\t", ""));
                System.out.println(elementAbs.concat("\n").replace("\t\t", ""));
                System.out.println(logicalGroupAbs.concat("\n").replace("\t\t", ""));
                System.out.println("----- ABS end -----");
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
                ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                String headerText = bundle.getString("file_not_copied_header");
                alert.setHeaderText(headerText);
                String contentText = bundle.getString("file_not_copied_content");
                contentText = String.format(contentText, e.getMessage());
                alert.setContentText(contentText);
                alert.showAndWait();
            }
            catch(IOException e){
                e.printStackTrace();
                ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                String headerText = bundle.getString("file_io_exception_header");
                alert.setHeaderText(headerText);
                String contentText = bundle.getString("file_io_exception_content");
                contentText = String.format(contentText, e.getMessage());
                alert.setContentText(contentText);
                alert.showAndWait();
            }

         return destDir;
        }

    @Nonnull
    private String searchForDelta(BufferedReader br, @Nonnull String newCode) throws IOException
            {String zeile;
             while((zeile = br.readLine()) != null)
                {newCode = newCode.concat(zeile).concat("\n");
                 for(String aDelta : delta) {
                    if (zeile.contains("delta ".concat(aDelta).concat(";"))) {
                        return newCode;
                       }
                   }
                }
             return newCode;
            }

    private boolean copyFiles(@Nonnull File destDir)
        {try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File(this.parent.getPath()));

            String OS = System.getProperty("os.name").toLowerCase();
            String fileToConsole = "/bin/bash";
            String c = "-c";
            String command = String.format("cp -r %s %s", fileToAbsSource, destDir);
            if(OS.contains("win"))
            {fileToConsole = "cmd.exe";
                c = "/c";
                command = String.format("xcopy %s %s /E /I /Y;", fileToAbsSource, destDir);
            }
            builder.command(fileToConsole, c, command);


            Process process = builder.start();
            process.waitFor();
            return true;
         }
         catch(IOException | InterruptedException ignored) {}
            return false;
        }

    @Override
    public void close(){ }

    public String getProduct()
        {return product;}

    public URI getParent()
        {return parent;}

    public File getFileToAbsSource()
        {return fileToAbsSource;}
}