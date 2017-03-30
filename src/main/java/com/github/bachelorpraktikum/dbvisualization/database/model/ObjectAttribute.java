package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class ObjectAttribute implements Element {

    private final int id;
    private final String type;
    private Vertex object;
    private final int objectID;
    private Attribute attribute;
    private final int attributeID;
    private final String value;

    public ObjectAttribute(int id, String type, Vertex object, Attribute attribute,
        String value) {
        this.id = id;
        this.type = type;
        this.object = object;
        objectID = object.getId();
        this.attribute = attribute;
        attributeID = attribute.getId();
        this.value = value;
    }

    /**
     * Creates a binding for a {@link Vertex} and an {@link Attribute} from an SQL ResultSet with
     * the column names defined in {@link Tables#OBJECT_ATTRIBUTES}.
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public ObjectAttribute(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.OBJECT_ATTRIBUTES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        type = rs.getString(columnNames.next());
        objectID = rs.getInt(columnNames.next());
        attributeID = rs.getInt(columnNames.next());
        value = rs.getString(columnNames.next());
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
     * Get type of the mapping
     *
     * @return Type
     */
    public String getType() {
        return type;
    }

    /**
     * Get corresponding {@link Vertex}
     *
     * @return {@link Vertex}
     */
    public Optional<Vertex> getVertex() {
        return Optional.ofNullable(object);
    }

    /**
     * Get ID from the corresponding {@link Vertex}
     *
     * @return ID for the {@link Vertex}
     */
    public int getVertexID() {
        return objectID;
    }

    /**
     * Returns the corresponding {@link Attribute}
     *
     * @return {@link Attribute}
     */
    public Optional<Attribute> getAttribute() {
        return Optional.ofNullable(attribute);
    }

    /**
     * Get ID from the corresponding {@link Attribute}
     *
     * @return ID for the {@link Attribute}
     */
    public int getAttributeID() {
        return attributeID;
    }

    /**
     * Get the value
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * <p>Turns this mapping into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | %s | [%s] | [%s] | %s}'</p>
     */
    public String toString() {
        String formatable = "{%d | %s | [%s] | [%s] | %s}";
        return String
            .format(formatable, getId(), getType(), getVertex(), getAttribute(), getValue());
    }
}
