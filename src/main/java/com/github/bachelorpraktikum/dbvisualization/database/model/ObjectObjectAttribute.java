package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class ObjectObjectAttribute implements Element {

    private final int id;
    private Vertex object1;
    private final int object1ID;
    private Vertex object2;
    private final int object2ID;
    private Attribute attribute;
    private final int attributeID;

    public ObjectObjectAttribute(int id,
        Vertex object1, Vertex object2,
        Attribute attribute) {
        this.id = id;
        this.object1 = object1;
        object1ID = object1.getId();
        this.object2 = object2;
        object2ID = object2.getId();
        this.attribute = attribute;
        attributeID = attribute.getId();
    }

    /**
     * Creates a binding for 2 objects and an attribute from an SQL ResultSet with the column names
     * defined in {@link Tables#OBJECT_OBJECT_ATTRIBUTES}.
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public ObjectObjectAttribute(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.OBJECT_OBJECT_ATTRIBUTES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        object1ID = rs.getInt(columnNames.next());
        object2ID = rs.getInt(columnNames.next());
        attributeID = rs.getInt(columnNames.next());
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
     * Get the first referenced {@link Vertex}
     *
     * @return First {@link Vertex}
     */
    public Optional<Vertex> getVertex1() {
        return Optional.ofNullable(object1);
    }

    /**
     * Get the second referenced {@link Vertex}
     *
     * @return Second {@link Vertex}
     */
    public Optional<Vertex> getVertex2() {
        return Optional.ofNullable(object2);
    }

    /**
     * Get the associated {@link Attribute attribute}
     *
     * @return {@link Attribute}
     */
    public Optional<Attribute> getAttribute() {
        return Optional.ofNullable(attribute);
    }

    /**
     * Returns the <tt>ID</tt> of the first {@link Vertex}
     *
     * @return <tt>ID</tt> of the first {@link Vertex}
     */
    public int getVertex1ID() {
        return object1ID;
    }

    /**
     * Returns the <tt>ID</tt> of the second {@link Vertex}
     *
     * @return <tt>ID</tt> of the second {@link Vertex}
     */
    public int getVertex2ID() {
        return object2ID;
    }

    public int getAttributeID() {
        return attributeID;
    }

    /**
     * <p>Turns this mapping into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | [%s] | [%s] | [%s]}'</p>
     */
    public String toString() {
        String formatable = "{%d | [%s] | [%s] | [%s]}";
        return String
            .format(formatable, getVertex1(), getVertex1ID(), getVertex2(), getAttribute());
    }
}
