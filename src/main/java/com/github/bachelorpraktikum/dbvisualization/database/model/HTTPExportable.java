package com.github.bachelorpraktikum.dbvisualization.database.model;

public interface HTTPExportable {

    /**
     * Exports the ABS element as a HTTPName. This enables later retrieval via an HTTP-API.
     *
     * @return HTTP ABS-Definition of the element.
     */
    String exportHTTP();
}
