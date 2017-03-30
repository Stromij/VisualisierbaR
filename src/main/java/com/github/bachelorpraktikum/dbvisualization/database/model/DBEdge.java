package com.github.bachelorpraktikum.dbvisualization.database.model;

import com.github.bachelorpraktikum.dbvisualization.database.Database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class DBEdge implements ABSExportable, Element {

    private final int id;
    private Vertex from;
    private final int fromID;
    private Vertex to;
    private final int toID;
    private final int wayNumber;
    private double length;
    private Set<Vertex> vertices;

    public DBEdge(int id, Vertex from,
        Vertex to, int wayNumber) {
        this.id = id;
        this.from = from;
        fromID = from.getId();
        this.to = to;
        toID = to.getId();
        this.wayNumber = wayNumber;
        setLength();
        vertices = new HashSet<>();
    }

    /**
     * Creates an edge constructed from an SQL ResultSet with the column names defined
     * in {@link Tables#EDGES}
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public DBEdge(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.EDGES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        fromID = rs.getInt(columnNames.next());
        toID = rs.getInt(columnNames.next());
        wayNumber = rs.getInt(columnNames.next());
        vertices = new HashSet<>();
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
     * Get the {@link Vertex} the edge starts from.
     *
     * @return {@link Vertex} the edge starts from
     */
    public Optional<Vertex> getFrom() {
        return Optional.ofNullable(from);
    }

    /**
     * Get the {@link Vertex} the edge ends on.
     *
     * @return {@link Vertex} the edge ends on
     */
    public Optional<Vertex> getTo() {
        return Optional.ofNullable(to);
    }

    /**
     * Get the length of the edge
     * Calculates the length with {@link DBEdge#setLength()} if the current length is lower/equal 0.
     *
     * @return Length of the edge
     */
    public double getLength() {
        if (length <= 0) {
            setLength();
        }
        return length;
    }

    /**
     * Manually set the length for this edge.
     *
     * @param length New length for the edge
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Calculates the length by using the {@link Vertex#getKilometer()} values of the {@link
     * DBEdge#getFrom() from} and {@link DBEdge#getTo() to} vertices.
     */
    public void setLength() {
        length = (Math.max(from.getKilometer(), to.getKilometer()) -
            Math.min(from.getKilometer(), to.getKilometer()));
    }

    /**
     * Set the <tt>from</tt> {@link Vertex}
     *
     * @param from <tt>from</tt> {@link Vertex}
     */
    public void setVertexFrom(Vertex from) {
        this.from = from;
    }

    /**
     * Set the <tt>to</tt> {@link Vertex}
     *
     * @param to <tt>to</tt> {@link Vertex}
     */
    public void setVertexTo(Vertex to) {
        this.to = to;
    }

    /**
     * Checks whether the given vertex is equivalent to the <code>FROM</code> or the <code>TO</code>
     * vertex and sets it accordingly. The success of this operation is returned
     * <code>from.ID</code> or <code>to.ID</code>
     *
     * @param vertex Vertex to check for
     * @return Whether the vertex is on this edge
     */
    public boolean setVertex(Vertex vertex) {
        int vertexID = vertex.getId();
        boolean success = false;
        if (vertexID == fromID) {
            from = vertex;
            success = true;
        } else if (vertexID == toID) {
            to = vertex;
            success = true;
        } else if (vertex.getEdgeID() == getId()) {
            addVertex(vertex);
            success = true;
        }

        return success;
    }

    /**
     * Returns the way number
     * This is <tt>-1</tt> if this is a free edge({@link DBEdge#isFree()}).
     *
     * @return Way number
     */
    public int getWayNumber() {
        return wayNumber;
    }

    /**
     * <p>{@inheritDoc}</p>
     *
     * <p>Attention: The {@link Vertex#getId()} is used to get the ID from the
     * <code>from</code> and <code>to</code> vertices. An info message will be shown when neither of
     * them is found. This is, to prevent using an Edge with no corresponding <code>from</code>
     * and <code>to</code> vertices. This shouldn't occur if the 'track' in the database is
     * correct.</p>
     */
    @Override
    public String export() {
        // ABSName, NodeFromName, NodeToName, Length(in m)
        String formattableString = "Edge %s = new local EdgeImpl(%s,%s,%d);";
        if (from == null || to == null) {
            String message = String
                .format("Edge with ID %d is has a null vertex | From: %d(%s) | To: %d(%s)", getId(),
                    getFromID(), from, getToID(), to);
            Logger.getLogger(getClass().getName()).info(message);
            return "";
        }
        return String
            .format(formattableString, getAbsName(), from.getId(), to.getId(), kmToM(getLength()));
    }

    private int kmToM(double length) {
        return new Double(length * 1000).intValue();
    }

    /**
     * <p>{@inheritDoc}</p>
     * Form: edge_{id}
     */
    @Override
    public String getAbsName() {
        return String.format("edge_%d", getId());
    }

    /**
     * <p>{@inheritDoc}</p>
     * This is an empty list.
     */
    @Override
    public List<String> exportChildren() {
        return Collections.emptyList();
    }

    /**
     * Returns the ID of the <tt>from</tt> {@link Vertex}.
     *
     * @return ID of the <tt>from</tt> {@link Vertex}
     */
    public int getFromID() {
        return fromID;
    }

    /**
     * Returns the ID of the <tt>to</tt> {@link Vertex}.
     *
     * @return ID of the <tt>to</tt> {@link Vertex}
     */
    public int getToID() {
        return toID;
    }

    /**
     * Returns all {@link Vertex vertices} which are on this edge.
     *
     * @return All {@link Vertex vertices} on this edge
     */
    public Set<Vertex> getVertices() {
        return vertices;
    }

    /**
     * Add a {@link Vertex} to this edge
     *
     * @param vertex Additional {@link Vertex} on this edge
     * @return False if the {@link Vertex} were already present, true otherwise
     */
    public boolean addVertex(Vertex vertex) {
        return vertices.add(vertex);
    }

    /**
     * Whether the edge is on a free track (manually created {@link Database#createFreeEdges()})
     *
     * @return Whether the edge is on a free track
     */
    public boolean isFree() {
        return wayNumber == -1;
    }

    /**
     * <p>Turns this mapping into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | [%s] | [%s] | %d | %d | %d | {%s}}'</p>
     */
    @Override
    public String toString() {
        String formatable = "{%d | [%s] | [%s] | %d | %f | #%d | {%s}}";
        return String.format(formatable, getId(), getFrom(), getTo(), getWayNumber(), getLength(),
            getVertices().size(), getAbsName());
    }
}
