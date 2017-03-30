package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.util.List;

public interface ABSExportable {

    /**
     * Creates the ABS definition for the current element
     *
     * @return ABS definition for the current element
     */
    String export();

    /**
     * Chooses an ABS name for the element. Usually it will look something like
     * <tt>{elementClassName}_{id}</tt>
     *
     * @return ABS name for the current element
     */
    String getAbsName();

    /**
     * A list with all children the element has.
     *
     * <p>e.g.: A {@link Betriebsstelle} can have
     * multiple {@link Vertex vertices} which can be added to the ABS element for the {@link
     * Betriebsstelle}</p>
     *
     * <p>This creates a list of all the calls (in ABS) for adding the
     * children elements to the current (ABS) element. The names for the children elements will be
     * retrieved via {@link #getAbsName}</p>
     *
     * @return List of abs calls to add the children elements (abs) to this element (abs)
     */
    List<String> exportChildren();
}
