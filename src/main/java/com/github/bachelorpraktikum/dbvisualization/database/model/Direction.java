package com.github.bachelorpraktikum.dbvisualization.database.model;

public enum Direction implements Element {
    None("None", 0),
    AscendingChainage("Ascending", 1),
    DescendingChainage("Descending", 2),
    Both("Both", 3);

    private String name;
    private int numericalValue;

    /**
     * Creates a Direction element with a name and a numerical value
     *
     * @param name Name for the direction
     * @param numericalValue value for the directions (as used in the database)
     */
    Direction(String name, int numericalValue) {
        this.name = name;
        this.numericalValue = numericalValue;
    }

    /**
     * Get the name for the direction
     *
     * @return Name
     */
    String getName() {
        return name;
    }

    /**
     * Get the numerical value for this direction
     *
     * @return Numerical value
     */
    int numericalValue() {
        return numericalValue;
    }

    /**
     * Return a direction based on the numerical value
     *
     * @param index Index for the direction to retrieve
     * @return Corresponding direction, if found, null otherwise
     * @throws IllegalArgumentException if numericalValue is larger than the enum
     */
    public static Direction get(int index) {
        if (index > Direction.values().length) {
            throw new IllegalArgumentException(
                "Index can't be larger than the length of the Direction enum.");
        }

        for (Direction dir : Direction.values()) {
            if (dir.numericalValue() == index) {
                return dir;
            }
        }

        return null;
    }

    /**
     * Prints the name of the direction (Form: {{name}})
     */
    @Override
    public String toString() {
        String formatable = "{%s}";
        return String.format(formatable, getName());
    }
}
