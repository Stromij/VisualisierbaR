package com.github.bachelorpraktikum.visualisierbar.datasource;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Context;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

public class FileSource implements DataSource {

    private final File file;
    private final Context context;

    public FileSource(File file) throws IOException {
        this.file = file;
        this.context = parseFile();
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
    public void close() {
    }
}
