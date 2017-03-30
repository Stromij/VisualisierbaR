package com.github.bachelorpraktikum.dbvisualization.database.model;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class Vertex implements ABSExportable, Element {

    private final int id;
    private Betriebsstelle betriebsstelle;
    private final int betriebsstelle_ID;
    private final int kennziffer;
    private final String name;
    private final double kilometer;
    private final Direction direction;
    private DBEdge edge;
    private final int edge_ID;
    private final Coordinates local;
    private final Coordinates global;
    private Set<Attribute> attributes;

    Vertex(int id, int betriebsstelle_id, int kennziffer, String name, double kilometer,
        Direction direction, int edge_id, Coordinates local, Coordinates global) {
        this.id = id;
        betriebsstelle_ID = betriebsstelle_id;
        this.kennziffer = kennziffer;
        this.name = name;
        this.kilometer = kilometer;
        this.direction = direction;
        edge_ID = edge_id;
        this.local = local;
        this.global = global;

        attributes = new HashSet<>();
    }

    /**
     * Creates a vertex from a SQL ResultSet with the column names defined in {@link
     * Tables#VERTICES}.
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public Vertex(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.VERTICES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        betriebsstelle_ID = rs.getInt(columnNames.next());
        kennziffer = rs.getInt(columnNames.next());
        name = rs.getString(columnNames.next());
        kilometer = rs.getDouble(columnNames.next());
        int direction_index = rs.getInt(columnNames.next());
        direction = Direction.get(direction_index);
        edge_ID = rs.getInt(columnNames.next());
        int localX = rs.getInt(columnNames.next());
        int localY = rs.getInt(columnNames.next());
        // TODO
        local = new Coordinates(Math.max(localX, 0), Math.max(localY, 0));
        int globalX = rs.getInt(columnNames.next());
        int globalY = rs.getInt(columnNames.next());
        // TODO
        global = new Coordinates(Math.max(globalX, 0), Math.max(globalY, 0));

        attributes = new HashSet<>();
    }

    /**
     * Checks whether the Vertex is valid (The vertex is in a Betriebsstelle)
     *
     * @return Whether the Vertex is valid
     */
    public boolean isValid() {
        return betriebsstelle_ID != 0;
    }

    /**
     * Returns the id for the vertex
     *
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the {@link Betriebsstelle} this vertex is in.
     * Every vertex has to be inside a {@link Betriebsstelle}.
     *
     * @return {@link Betriebsstelle} the vertex is in.
     */
    public Optional<Betriebsstelle> getBetriebsstelle() {
        return Optional.ofNullable(betriebsstelle);
    }

    /**
     * Checks whether the vertex is part of a <tt>WEICHEN_PUNKT</tt> by checking its attributes
     *
     * @return Whether the vertex belongs to a <tt>WEICHEN_PUNKT</tt>
     */
    public boolean isWeiche() {
        for (Attribute attribute : attributes) {
            if (attribute.getId() == FixAttributeValues.WEICHEN_PUNKT.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all 3 vertices for the <tt>WeichenPunkt</tt>
     *
     * @return 3 vertices for the WeichenPunkt
     * @throws IllegalStateException if no 3 vertices for the <tt>WeichenPunkt</tt> were found
     */
    public List<Vertex> getWeichenPunkte() {
        List<Vertex> vertices = new LinkedList<>();
        vertices.add(this);
        for (Attribute attribute : attributes) {
            if (attribute.getId() == FixAttributeValues.WEICHEN_PUNKT.getId()
                && attribute.getVertex().getId() != getId()) {
                vertices.add(attribute.getVertex());
            }
        }

        if (vertices.size() != 3) {
            throw new IllegalStateException("WEICHEN_PUNKT has to have 3 vertices.");
        }
        return vertices;
    }

    /**
     * Assigns the {@link Betriebsstelle} the vertex is in.
     *
     * @param bst The {@link Betriebsstelle} this vertex is in.
     */
    public void setBetriebsstelle(Betriebsstelle bst) {
        betriebsstelle = bst;
    }

    /**
     * Returns the <tt>ID</tt> from the {@link Betriebsstelle} this vertex is in.
     *
     * @return ID of the {@link Betriebsstelle} this vertex is in
     */
    public int getBetriebsstelleID() {
        return betriebsstelle_ID;
    }

    /**
     * Returns the <tt>Kennziffer</tt> of this vertex.
     *
     * @return <tt>Kennziffer</tt>
     */
    public int getKennziffer() {
        return kennziffer;
    }

    /**
     * Returns the name of this vertex
     *
     * @return Name of this vertex, can be null/empty
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Returns the <tt>Kilometer</tt> this vertex is on
     *
     * @return Kilometer this vertex is on
     */
    public double getKilometer() {
        return kilometer;
    }

    /**
     * Get the direction of the vertex
     *
     * @return Direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Returns the {@link DBEdge edge} this vertex is on
     *
     * @return {@link DBEdge edge} this vertex is on
     */
    public Optional<DBEdge> getEdge() {
        return Optional.ofNullable(edge);
    }

    /**
     * Sets the {@link DBEdge edge} this vertex is on
     *
     * @param edge Edge where the vertex is on
     */
    public void setEdge(DBEdge edge) {
        this.edge = edge;
    }

    /**
     * Returns the local coordinates in the corresponding <tt>Betriebsstelle</tt>
     *
     * @return Local coordinates (local to the Betriebsstelle Coordinate)
     */
    @Deprecated
    public Coordinates getLocalCoordinates() {
        return local;
    }

    /**
     * <p>Returns the global coordinates for this vertex</p>
     * <p>This is used for the ABS-{@link Vertex#export export}</p>
     *
     * @return Global coordinates for this vertex
     */
    public Coordinates getGlobalCoordinates() {
        return global;
    }

    /**
     * Returns the <tt>ID</tt> of the {@link DBEdge edge} this vertex is on.
     *
     * @return <tt>ID</tt> for the {@link DBEdge edge}
     */
    public int getEdgeID() {
        return edge_ID;
    }

    /**
     * Adds an attribute to the attributes set of the vertex
     *
     * @param attribute Attribute to add
     * @return <tt>true</tt> if this set did not already contain the specified element
     */
    public boolean addAttribute(Attribute attribute) {
        return attributes.add(attribute);
    }

    /**
     * Returns all {@link Attribute attributes} associated with this vertex
     *
     * @return All associated {@link Attribute attributes}
     */
    Set<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * <p>{@inheritDoc}</p> <p>Returns with the following format: <tt>Node %s = new local
     * NodeImpl(%d,%d);</tt>, where %s is replaced by the {@link Vertex#getAbsName() absName} and %d
     * by the {@link Vertex#getGlobalCoordinates() global coordinates}</p>
     *
     * @return ABS-Node with the following form: <tt>Node %s = new local NodeImpl(%d,%d);</tt>
     */
    @Override
    public String export() {
        String formattableString = "Node %s = new local NodeImpl(%d,%d);";
        return String.format(formattableString, getAbsName(), global.getX(), global.getY());
    }

    /**
     * <p>{@inheritDoc}</p>
     * <p>ABS-Name with the following form: <tt>node_{id}</tt></p>
     *
     * @return ABS-name (<tt>node_{id}</tt>)
     */
    @Override
    public String getAbsName() {
        return String.format("node_%d", id);
    }

    /**
     * <p>{@inheritDoc}</p>
     * <p>Exports all associated attributes and adds them to this element.</p>
     */
    @Override
    public List<String> exportChildren() {
        List<String> export = new LinkedList<>();

        Pattern nameRegex = Pattern.compile("[^ ]* (?<name>.*) *=");
        List<String> attrNames = new LinkedList<>();
        for (Attribute attribute : attributes) {
            export.add(attribute.export());

            for (String attrExport : attribute.export().split(System.lineSeparator())) {
                if (attrExport.isEmpty()) {
                    continue;
                }
                Matcher m = nameRegex.matcher(attrExport);
                if (m.find()) {
                    String name = m.group("name");
                    attrNames.add(name);
                }
            }
        }

        String formatableIn = String.format("%s.addElement(%s);", getAbsName(), "%s");
        for (String name : attrNames) {
            export.add(String.format(formatableIn, name.trim()));
        }

        return export;
    }

    /**
     * Turns the vertex into a string with all associated elements, the {@link Vertex#getEdgeID()
     * edge ID} will be printed instead of the {@link DBEdge edge} representation to avoid
     * recursion.
     */
    @Override
    public String toString() {
        String formatable = "{%d | [%s] | %d | %s | %f | [%s] | [%d] | [%s] | #%d | {%s}}";
        return String
            .format(formatable, getId(), getBetriebsstelle().get(), getKennziffer(), getName(),
                getKilometer(), getDirection(), getEdgeID(), getGlobalCoordinates(),
                getAttributes().size(),
                getAbsName());
    }
}
