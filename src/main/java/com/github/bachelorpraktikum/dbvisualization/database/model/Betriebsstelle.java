package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Betriebsstelle implements ABSExportable, Element {

    private static final String ZUGFOLGE_EXPORT = "[HTTPName: \"%s\"]ActiveZugFolge %<s = new ZugFolgeImpl(\"%<s\");";
    private static final String BAHNHOF_EXPORT = "[HTTPName: \"%s\"]ZugMelde %<s = new BahnhofImpl(\"%<s\");";

    private final int id;
    private final String title;
    private final String shortName;
    private final String rl100;
    private final int weatherID;
    private final int kennziffer;
    private Set<Vertex> vertices;

    public Betriebsstelle(int id, String title, String shortName, String rl100, int weatherID,
        int kennziffer) {
        this.id = id;
        this.title = title;
        this.shortName = shortName;
        this.rl100 = rl100;
        this.weatherID = weatherID;
        this.kennziffer = kennziffer;
    }

    /**
     * Creates a <tt>Betriebsstelle</tt> constructed from an SQL ResultSet with the column names
     * defined in {@link Tables#BETRIEBSSTELLEN}
     *
     * @param rs ResultSet to get details from
     * @throws SQLException if an error during element retrievel from the {@link ResultSet} occurs
     */
    public Betriebsstelle(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.BETRIEBSSTELLEN.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        title = rs.getString(columnNames.next());
        shortName = rs.getString(columnNames.next());
        rl100 = rs.getString(columnNames.next());
        weatherID = rs.getInt(columnNames.next());
        kennziffer = rs.getInt(columnNames.next());
        vertices = new HashSet<>(1024);
    }

    /**
     * Checks whether the Betriebsstelle has an <tt>ID</tt> larger 0
     *
     * @return Whether the Betriebsstelle is valid
     */
    public boolean isValid() {
        return id != 0;
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
    public String getTitle() {
        return title;
    }

    /**
     * Returns the short name
     *
     * @return Short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Returns the <tt>Rl100</tt>
     *
     * @return Rl100
     */
    public String getRl100() {
        return rl100;
    }

    /**
     * Returns the weather ID
     *
     * @return Weather ID
     */
    public int getWeatherID() {
        return weatherID;
    }

    /**
     * Returns the <tt>Kennziffer</tt>
     *
     * @return <tt>Kennziffer</tt>
     */
    public int getKennziffer() {
        return kennziffer;
    }

    /**
     * <p>{@inheritDoc}</p>
     *
     * <p>The <tt>Betriebsstelle</tt> will be exported differently, depending on whether it's a
     * <tt>{@link FixAttributeValues#ZUGFOLGE Zugfolge}</tt> or a <tt>Bahnhof</tt></p>
     */
    @Override
    public String export() {
        // TODO
        if (isZugfolge()) {
            return String.format(ZUGFOLGE_EXPORT, getAbsName());
        }

        return String.format(BAHNHOF_EXPORT, getAbsName());
    }

    /**
     * <p>{@inheritDoc}</p> <p>In the case of a <tt>{@link FixAttributeValues#ZUGFOLGE
     * Zugfolge}</tt> the following form is used: 'zufolge_{id}'</p> <p>In the case of a
     * <tt>Bahnhof</tt> the following form is used: 'bahnhof_{id}'</p>
     */
    @Override
    public String getAbsName() {
        String formattableString = "bahnhof_%d";
        if (isZugfolge()) {
            formattableString = "zugfolge_%d";
        }

        return String.format(formattableString, getId());
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
     * Checks whether the Betriebsstelle is a Zugfolge. This is hardcoded since it's the same way in
     * the database. The ID for a Zugfolge is defined in `FixAttributeValues`.
     *
     * @return Whether the element is a `Zugfolge`
     */
    boolean isZugfolge() {
        for (Vertex vertex : getVertices()) {
            for (Attribute attribute : vertex.getAttributes()) {
                if (attribute.getId() == FixAttributeValues.ZUGFOLGE.getId()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns all {@link Vertex vertices} that are in this <tt>Betriebsstelle</tt>
     *
     * @return List of {@link Vertex vertices} in this <tt>Betriebsstelle</tt>
     */
    public Set<Vertex> getVertices() {
        return vertices;
    }

    /**
     * Add a {@link Vertex} which is in this <tt>Betriebsstelle</tt>
     *
     * @param vertex Vertex in this <tt>Betriebsstelle</tt>
     */
    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    /**
     * Add a list of {@link Vertex vertices} that are in this <tt>Betriebsstelle</tt>.
     *
     * @param vertices List of {@link Vertex vertices} in this <tt>Betriebsstelle</tt>
     */
    public void addAllVertices(Collection<Vertex> vertices) {
        this.vertices.addAll(vertices);
    }

    /**
     * <p>Turns this <tt>Betriebsstelle</tt> into a string with all associated elements.</p>
     * <p>Has the following form: '{%d | %s | %s | %s | %d | %d | %d | {%s}}'</p>
     */
    @Override
    public String toString() {
        String formatable = "{%d | %s | %s | %s | %d | %d | #%d | {%s}}";
        return String
            .format(formatable, getId(), getTitle(), getShortName(), getRl100(), getWeatherID(),
                getKennziffer(), getVertices().size(), getAbsName());
    }
}
