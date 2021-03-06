package com.github.bachelorpraktikum.visualisierbar.view.sourcechooser;

import com.github.bachelorpraktikum.visualisierbar.datasource.DataSource;
import java.io.IOException;
import javafx.beans.value.ObservableBooleanValue;
import javax.annotation.Nonnull;

/**
 * Represents a chooser for a data source.<br>The user can input a URL by which the data source can
 * be accessed.<br>The chooser does not check whether that is the case.<br>The data which can be
 * retrieved from the source has to be compatible with the described {@link
 * com.github.bachelorpraktikum.visualisierbar.model model}
 */
interface SourceChooser<T extends DataSource> {

    /**
     * Returns the resource.
     *
     * @return the resource
     * @throws IOException if any error occurs creating the resource
     */
    @Nonnull
    T getResource() throws IOException;

    /**
     * An observable boolean value indicating whether a semi-legitimate input has been chosen and
     * the 'Open'-button can be enabled.
     *
     * @return an ObservableBooleanValue
     */
    @Nonnull
    ObservableBooleanValue inputChosen();
}
