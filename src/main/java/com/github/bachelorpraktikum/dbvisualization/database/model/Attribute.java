package com.github.bachelorpraktikum.dbvisualization.database.model;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Logger;

public class Attribute implements ABSExportable, Cloneable, Element {

    private final int id;
    private final String title;
    private final String description;
    private final String acronym;
    private Vertex vertex;

    public Attribute(int id, String title, String description, String acronym) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.acronym = acronym;
    }

    /**
     * Creates an Attribute constructed from an SQL ResultSet with the column names
     * defined in {@link Tables#ATTRIBUTES}
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public Attribute(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.ATTRIBUTES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        title = rs.getString(columnNames.next());
        description = rs.getString(columnNames.next());
        acronym = rs.getString(columnNames.next());
    }

    /**
     * Returns a clone of this object
     *
     * @return Clone, null if the clone failed.
     */
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ignored) {

        }

        return null;
    }

    /**
     * Returns the id
     *
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title
     *
     * @return Title
     */
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    /**
     * Returns the description
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the acronym
     *
     * @return Acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * <p>{@inheritDoc}</p> <p>Creates the attributes depending on the type of the attribute. The
     * types are defined in {@link FixAttributeValues}. If no explicit case for the attribute is
     * defined in this function, the function can guess/use the values put into {@link
     * FixAttributeValues}.</p>
     */
    @Override
    public String export() {
        if (vertex == null) {
            throw new IllegalArgumentException(
                "Attribute needs to be tied to a vertex to be exported.");
        }

        String name = getAbsName();
        String exportString = "";

        String hauptsignalFormatString = "HauptSignal %s_%2$s_%3$s = new local HauptSignalImpl(%2$s, %3$s);";
        String vorsignalFormatString = "VorSignal %s_%2$s = new local VorSignalImpl(%2$s);";
        if (getId() == FixAttributeValues.HAUPT_UND_VORSIGNAL.getId()
            || getId() == FixAttributeValues.HAUPT_UND_VORSIGNAL_MIT_SPERRSIGNAL.getId()) {
            if (!getEdge().isPresent()) {
                Logger.getLogger(getClass().getName()).severe(
                    String.format(
                        "Attribute(HauptUndVorsignal) needs to be tied to a edge to be exported. Vertex (%s) doesn't seem to be tied to an actual edge.",
                        getVertex()));
                return "";
            }

            String hauptName = String.format("hauptsignal_%s", name);
            exportString = String
                .format(hauptsignalFormatString, hauptName, getVertex().getAbsName(),
                    getEdge().get().getAbsName());
            String vorName = String.format("vorsignal_%s", name);
            exportString = new StringJoiner(System.lineSeparator()).add(exportString)
                .add(String.format(vorsignalFormatString, vorName, getVertex().getAbsName()))
                .toString();
        } else if (getId() == FixAttributeValues.VORSIGNAL.getId()) {
            exportString = String
                .format(vorsignalFormatString, name, getVertex().getAbsName());
        } else if (getId() == FixAttributeValues.SPERRSIGNAL.getId()) {
            Logger.getLogger(getClass().getName())
                .finer("Not creating abs class SPERRSIGNAL, implementation is unknown.");
        } else {
            ConfigKey experimentalKey = ConfigKey.experimentalAbsExportForAttributes;
            if (!experimentalKey.getBoolean()) {
                String message = String.format(
                    "Not exporting attribute (#%d). Experimental export can be enabled in the config file via %s",
                    getId(), experimentalKey.getKey());
                Logger.getLogger(getClass().getName()).config(message);
                return "";
            }
            Optional<FixAttributeValues> fixAttributeOpt = FixAttributeValues.get(getId());
            if (fixAttributeOpt.isPresent()) {
                FixAttributeValues fixAttribute = fixAttributeOpt.get();
                if (fixAttribute.classNameLhs() == null) {
                    return exportString;
                }
                String formattableString = "%s %s_%4$s = new local %s(%s);";
                exportString = String
                    .format(formattableString, fixAttribute.classNameLhs(), name,
                        fixAttribute.classNameRhs(), getVertex().getAbsName());
                String comment = "// Experimental. This was constructed by using default values. See Attribute.java and FixAttributeValues.java (VisualisierbaR project) for more information.";
                exportString = new StringJoiner(System.lineSeparator()).add(comment)
                    .add(exportString).toString();
            }
        }

        return exportString;
    }

    /**
     * <p>{@inheritDoc}</p>
     * <p>Tries to use the {@link #getTitle() title} as name, uses the {@link #getId() id}
     * otherwise. Spaces in the title will be replaced with underscores. Form: '{title|id}_id'
     */
    @Override
    public String getAbsName() {
        String name_prefix = getTitle().orElse(String.valueOf(getId())).replace(" ", "_")
            .toLowerCase();
        return String.format("%s_%d", name_prefix, getId());
    }

    /**
     * <p>{@inheritDoc}</p>
     * <p>An empty list in this case</p>
     *
     * @return Empty list
     */
    @Override
    public List<String> exportChildren() {
        return Collections.emptyList();
    }

    /**
     * Set a {@link Vertex} for this attribute
     *
     * @param vertex Corresponding {@link Vertex}
     */
    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    /**
     * Returns the associated {@link Vertex}
     *
     * @return Associated {@link Vertex}
     */
    Vertex getVertex() {
        return vertex;
    }

    /**
     * Returns the associated {@link DBEdge edge}
     *
     * @return Associated {@link DBEdge edge}
     */
    Optional<DBEdge> getEdge() {
        return vertex.getEdge();
    }

    /**
     * Provide a public method to retrieve the clone which is constructed via {@link #clone()}
     *
     * @return Clone of the attribute, null if cloning failed.
     */
    public Attribute getClone() {
        return (Attribute) this.clone();
    }

    /**
     * <p>Turns this <tt>Betriebsstelle</tt> into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | %s | %s | %s | [%s] | {%s}}'</p>
     */
    @Override
    public String toString() {
        String formatable = "{%d | %s | %s | %s | [%s] | {%s}}";
        return String
            .format(formatable, getId(), getTitle(), getDescription(), getAcronym(), getVertex(),
                getAbsName());
    }
}
