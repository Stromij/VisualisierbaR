package com.github.bachelorpraktikum.dbvisualization.database.model;

import com.github.bachelorpraktikum.dbvisualization.database.Database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class Neighbors implements Element {

    private final int id;
    private final int vertex1ID;
    private final int vertex2ID;
    private Vertex vertex1;
    private Vertex vertex2;

    public Neighbors(int id, Vertex vertex1, Vertex vertex2) {
        this.id = id;
        this.vertex1 = vertex1;
        vertex1ID = vertex1.getId();
        this.vertex2 = vertex2;
        vertex2ID = vertex2.getId();
    }

    /**
     * Creates a binding for 2 {@link Vertex vertices} which connect different {@link Betriebsstelle
     * betriebsstellen} via an edge ({@link Database#createFreeEdges()}) This is constructed from an
     * SQL ResultSet with the column names defined in {@link Tables#NEIGHBORS}
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public Neighbors(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.NEIGHBORS.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        vertex1ID = rs.getInt(columnNames.next());
        vertex2ID = rs.getInt(columnNames.next());
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
    public Vertex getVertex1() {
        return vertex1;
    }

    /**
     * Set the first referenced {@link Vertex}
     *
     * @param vertex1 First {@link Vertex}
     */
    public void setVertex1(Vertex vertex1) {
        this.vertex1 = vertex1;
    }

    /**
     * Get the second referenced {@link Vertex}
     *
     * @return Second {@link Vertex}
     */
    public Vertex getVertex2() {
        return vertex2;
    }

    /**
     * Set the second referenced {@link Vertex}
     *
     * @param vertex2 Second {@link Vertex}
     */
    public void setVertex2(Vertex vertex2) {
        this.vertex2 = vertex2;
    }

    /**
     * Get ID for the first {@link Vertex}
     *
     * @return ID for the first {@link Vertex}
     */
    public int getVertex1ID() {
        return vertex1ID;
    }

    /**
     * Get ID for the second {@link Vertex}
     *
     * @return ID for the second {@link Vertex}
     */
    public int getVertex2ID() {
        return vertex2ID;
    }

    /**
     * <p>Turns this mapping into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | [%s] | [%s]}'</p>
     */
    public String toString() {
        String formatable = "{%d | [%s] | [%s]}";
        return String.format(formatable, getVertex1(), getVertex2());
    }
}
