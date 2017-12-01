package com.github.bachelorpraktikum.visualisierbar.datasource;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Context;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

public class AbsSource implements DataSource {

    private final File file;
    private final Context context;

    public AbsSource(File file, String product) throws IOException {
        this.file = compileABS(file, product);
        this.context = parseFile();
    }

    /**
     * Compiles a given ABS-File
     * @param file The ABS File which is meant to be compiled
     * @return the path to the compiled ABS-file
     * @throws IOException throws an Error, when the Input is not valid e.g. it's not an ABS-File
     */
    private File compileABS(File file, String product) throws IOException {
        // Platzhalter für das Compiling der ABS-Files
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