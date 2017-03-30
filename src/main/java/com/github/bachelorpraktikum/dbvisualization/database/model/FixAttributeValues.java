package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.util.Optional;

enum FixAttributeValues {
    ZUGFOLGE(85, "ActiveZugFolge", "ZugFolgeImpl"),
    HAUPTSIGNAL(1, "HauptSignal", null),
    HAUPTSIGNAL_MIT_SPERRSIGNAL(2, null, null),
    VORSIGNAL(3, "VorSignal", null),
    SPERRSIGNAL(4, "SperrSignal", null),
    HAUPT_UND_VORSIGNAL(5, null, null),
    HAUPT_UND_VORSIGNAL_MIT_SPERRSIGNAL(6, null, null),
    WEICHEN_PUNKT(32, "WeichenPunkt", null);

    private final int id;
    private final String lhs;
    private final String rhs;

    /**
     * Describes a fixed attribute value, possibly with it's ABS class names.
     *
     * @param id ID for the attribute
     * @param lhs Left hand class name
     * @param rhs Right hand class name
     */
    FixAttributeValues(int id, String lhs, String rhs) {
        this.id = id;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Returns the ID
     *
     * @return ID
     */
    int getId() {
        return id;
    }

    /**
     * Returns the left hand side class name for the attribute. If this is null, the attribute can't
     * be instantiated as one abs element only, it's a composition of different elements. This has
     * to be handled elsewhere.
     *
     * @return Lefthand side class name if it's single abs element, null otherwise
     */
    String classNameLhs() {
        return lhs;
    }

    /**
     * Returns the right hand side class name for the attribute. Can be null of attribute has to be
     * constructed from different elements. Can also be null if the right hand side can be
     * constructed from the left hand side ({lhs}Impl).
     *
     * @return Righthand side class name, null if undefined
     */
    String classNameRhs() {
        if (rhs == null) {
            return String.format("%sImpl", lhs);
        }

        return rhs;
    }

    /**
     * Get a FixAttributeValue via the attribute ID.
     *
     * @param id ID for the FixAttributeValue to retriebe
     * @return Attribute with <code>id</code>, empty Optional if not found
     */
    static Optional<FixAttributeValues> get(int id) {
        for (FixAttributeValues attributeValue : FixAttributeValues.values()) {
            if (id == attributeValue.getId()) {
                return Optional.of(attributeValue);
            }
        }

        return Optional.empty();
    }

    /**
     * <p>Turns this <tt>FixAttributeValue</tt> into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | %s | %s}'</p>
     */
    @Override
    public String toString() {
        return String.format("{%d | %s | %s}", getId(), classNameLhs(), classNameRhs());
    }
}
