package com.github.bachelorpraktikum.visualisierbar.model;

import javafx.collections.ObservableList;
import javax.annotation.Nonnull;

/**
 * Represents one or more events that happened at a specific point in time.
 */
public interface Event extends Comparable<Event> {

    /**
     * Gets the time this event occured.
     *
     * @return the time in milliseconds since simulation start
     */
    int getTime();

    /**
     * Gets a string representation of this event.
     *
     * @return a potentially human readable description
     */
    @Nonnull
    String getDescription();

    @Nonnull
    ObservableList<String> getWarnings();

    default int compareTo(@Nonnull Event other) {
        return Integer.compare(getTime(), other.getTime());
    }
}
