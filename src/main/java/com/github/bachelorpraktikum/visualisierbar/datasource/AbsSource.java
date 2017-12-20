package com.github.bachelorpraktikum.visualisierbar.datasource;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Context;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbsSource implements DataSource {

    private final File fileToAbs;
    private final File fileToAbsSource;
    private final Context context;
    private final URI parent;
    private final String product;

    private ArrayList delta;


    public AbsSource(String command, File path, String product) throws IOException {
        this.parent = new File(path.getParent()).toURI();
        this.fileToAbs = compileABS(command);
        this.fileToAbsSource = path;
        this.product = product;

        this.context = parseFile();
        this.delta = new ArrayList();
    }

    /**
     * Compiles a given ABS-File
     * @param command The complete shell-command to compile the ABS
     * @return the path to the compiled ABS-file
     * @throws IOException throws an Error, when the Input is not valid e.g. it's not an ABS-File
     */
    private File compileABS(String command) throws IOException {
        File file = new File(String.format("%s/actual.zug", this.parent.getPath()));

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(this.parent.getPath()));
        builder.command("/bin/bash", "-c", String.format(
                "rm -r gen/erlang/*; %s; cd gen/erlang; ./run > %s/actual.zug;",
                command,
                this.parent.getPath()));

        Process process = builder.start();

        try {
            int exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        return file;
    }

    private Context parseFile() throws IOException {
        return new GraphParser().parse(fileToAbs.getPath());
    }

    @Nonnull
    @Override
    public Context getContext() {
        return context;
    }

    public ArrayList getDeltas(){
        ArrayList<Matcher> foundedDeltaLines = new ArrayList();
        ArrayList<String> productNames = new ArrayList<>();
        Matcher matcherProduct;
        Matcher matcherDelta;

        Pattern patternDeltaLines = Pattern.compile("(delta )(.*?)((after .*?)?)( when )(.*?)(;)");
        Pattern patternProduct = Pattern.compile("(product "+this.product+" \\()(.*?)(\\);)");

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

            // suchen der Deltas
            // hier müsste auch in ieiner Form die verundete Delta-Klausel implementiert werden

            for(int i=0; i<productNames.size(); i++)
                {for(int n=0; n<foundedDeltaLines.size(); n++)
                    {if(foundedDeltaLines.get(n).group(6).matches("(|.*? )" + productNames.get(i) + "(| .*?)"))
                        {this.delta.add(foundedDeltaLines.get(n).group(2));}
                    }
                }
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}

        return this.delta;
    }

    @Override
    public void close(){ }
}