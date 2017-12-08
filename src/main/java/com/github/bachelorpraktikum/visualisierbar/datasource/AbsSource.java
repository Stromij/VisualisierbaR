package com.github.bachelorpraktikum.visualisierbar.datasource;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Context;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class AbsSource implements DataSource {

    private final File file;
    private final Context context;
    private final URI parent;

    public AbsSource(String command, URI parent) throws IOException {
        this.parent = parent;
        this.file = compileABS(command);

        this.context = parseFile();
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
        return new GraphParser().parse(file.getPath());
    }

    @Nonnull
    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void close(){ }
}