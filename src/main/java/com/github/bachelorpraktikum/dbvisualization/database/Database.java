package com.github.bachelorpraktikum.dbvisualization.database;

import com.github.bachelorpraktikum.dbvisualization.database.model.ABSExportable;
import com.github.bachelorpraktikum.dbvisualization.database.model.Attribute;
import com.github.bachelorpraktikum.dbvisualization.database.model.Betriebsstelle;
import com.github.bachelorpraktikum.dbvisualization.database.model.DBEdge;
import com.github.bachelorpraktikum.dbvisualization.database.model.Neighbors;
import com.github.bachelorpraktikum.dbvisualization.database.model.ObjectAttribute;
import com.github.bachelorpraktikum.dbvisualization.database.model.ObjectObjectAttribute;
import com.github.bachelorpraktikum.dbvisualization.database.model.Tables;
import com.github.bachelorpraktikum.dbvisualization.database.model.Vertex;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Database implements AutoCloseable {

    private final URI uri;
    private HikariDataSource dataSource;
    private final int CONNECTION_TIMEOUT = 1000;
    private List<Attribute> attributes;
    private List<Neighbors> neighbors;
    private List<Betriebsstelle> betriebsstellen;
    private List<ObjectAttribute> objectAttributes;
    private List<ObjectObjectAttribute> objectObjectAttributes;
    private List<Vertex> vertices;
    private List<DBEdge> edges;
    private List<ABSExportable> exportableElements;
    private ABSExporter exporter;
    private DatabaseUser user;

    /**
     * Creates a database connection with the given URI. Tries to load the {@link DatabaseUser
     * database user} via {@link DatabaseUser#fromConfig}. If that fails, a {@link DatabaseUser
     * database user} without credentials is used.
     *
     * @param uri URI to create the connection to
     */
    public Database(URI uri) {
        this(uri, DatabaseUser.fromConfig().orElse(new DatabaseUser("", "")));
    }

    /**
     * Creates a database connection with the given <tt>uri</tt> and {@link DatabaseUser database
     * user}.
     *
     * @param uri URI to create the connection to
     * @param user Credentials for the database access
     */
    public Database(URI uri, DatabaseUser user) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.user = user;
        this.uri = uri;
        init();
    }

    /**
     * Try to connect to the database
     *
     * @throws PoolInitializationException if the connection wasn't successful.
     */
    private void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(uri.toString().replace("http://", "jdbc:mysql://"));
        if (user.getUser().trim().isEmpty()) {
            config.setUsername("");
        } else {
            config.setUsername(user.getUser());
        }
        if (user.getPassword().trim().isEmpty()) {
            config.setPassword(null);
        } else {
            config.setPassword(user.getPassword());
        }

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);

        exportableElements = new ArrayList<>();
        getAll();
        merge();
        exporter = new ABSExporter(exportableElements);
    }

    /**
     * Retrieve all {@link Tables} from the database.
     * Add the {@Link ABSExportable exportable elements} to {@link #exportableElements}
     */
    private void getAll() {
        getAttributes();
        getObjectAttributes();
        exportableElements.addAll(getVertices());
        exportableElements.addAll(getEdges());
        exportableElements.addAll(getBetriebsstellen());
        getNeighbors();
        getObjectObjectAtributes();
    }

    /**
     * <p>Associate the objects from the database with each other.</p> <p>e.g.:</p> <p>Set all
     * {@link Attribute attributes} which correspond to a {@link Vertex} via the {@link
     * ObjectAttribute} table.</p>
     */
    private void merge() {
        for (Vertex vertex : vertices) {
            for (DBEdge edge : edges) {
                if (edge.setVertex(vertex)) {
                    vertex.setEdge(edge);
                }
            }
            for (Betriebsstelle betriebsstelle : betriebsstellen) {
                if (vertex.getBetriebsstelleID() == betriebsstelle.getId()) {
                    vertex.setBetriebsstelle(betriebsstelle);
                    betriebsstelle.addVertex(vertex);
                }
            }
            for (ObjectAttribute mapping : objectAttributes) {
                if (mapping.getVertexID() == vertex.getId()) {
                    for (Attribute attribute : attributes) {
                        if (mapping.getAttributeID() == attribute.getId()) {
                            Attribute attr = attribute.getClone();
                            vertex.addAttribute(attr);
                            attr.setVertex(vertex);
                        }
                    }
                }
            }
        }

        edges.addAll(createFreeEdges());
    }

    /**
     * <p>Free edges will begin with the highest ID possible which fits in an integer, i.e.
     * <tt>Integer.MAX_VALUE - #freeEdges</tt></p>
     *
     * <p> Free edges are edges which aren't in a {@link Betriebsstelle}, these edges are described
     * through {@link Neighbors} which hold 2 vertices where the edge is connected to. </p>
     *
     * <p> The wayNumber for these edges is -1. </p>
     *
     * @return List of free edges (created via {@link Neighbors}).
     */
    public List<DBEdge> createFreeEdges() {
        List<Neighbors> neighbors = getNeighbors();
        List<DBEdge> edges = new LinkedList<>();
        int idStart = Integer.MAX_VALUE - neighbors.size();
        for (Neighbors neighbor : neighbors) {
            DBEdge edge = new DBEdge(idStart, neighbor.getVertex1(), neighbor.getVertex2(), -1);
            edges.add(edge);
            idStart--;
        }

        return edges;
    }

    /**
     * Tests whether the database can be accessed over the given URI with the provided {@link
     * DatabaseUser database user}
     *
     * @return Whether a connection can be established
     */
    public boolean testConnection() {
        try {
            return dataSource.getConnection().isValid(CONNECTION_TIMEOUT);
        } catch (SQLException ignored) {
            return false;
        }
    }

    /**
     * Returns the connection for accessing the database
     *
     * @return Connection for the database
     */
    public Optional<Connection> getConnection() {
        try {
            return Optional.of(dataSource.getConnection());
        } catch (SQLException ignored) {

        }

        return Optional.empty();
    }

    /**
     * Returns a complete list of {@link Neighbors} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link Neighbors}
     */
    public List<Neighbors> getNeighbors() {
        /*
        TODO
         Neighbors is currently not useable as is. It is being changed in the database.
         The representation in this program already assumes the new database structure.
         Hence we can't retrieve/use the `Neighbors` table at the moment.
         */
        neighbors = new LinkedList<>();
        if (neighbors == null) {
            neighbors = getTableElements(Tables.NEIGHBORS, Neighbors.class);
        }

        return neighbors;
    }

    /**
     * Returns a complete list of {@link Attribute attributes} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link org.w3c.dom.Attr}
     */
    public List<Attribute> getAttributes() {
        if (attributes == null) {
            attributes = getTableElements(Tables.ATTRIBUTES, Attribute.class);
        }
        return attributes;
    }

    /**
     * Returns a complete list of {@link Betriebsstelle Betriebsstellen} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link Betriebsstelle Betriebsstellen}
     */
    public List<Betriebsstelle> getBetriebsstellen() {
        if (betriebsstellen == null) {
            betriebsstellen = getTableElements(Tables.BETRIEBSSTELLEN, Betriebsstelle.class);
        }
        return betriebsstellen;
    }

    /**
     * Returns a complete list of {@link ObjectAttribute object attributes} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link ObjectAttribute object attributes}
     */
    public List<ObjectAttribute> getObjectAttributes() {
        if (objectAttributes == null) {
            objectAttributes = getTableElements(Tables.OBJECT_ATTRIBUTES, ObjectAttribute.class);
        }
        return objectAttributes;
    }

    /**
     * Returns a complete list of {@link ObjectObjectAttribute ObjectObjectAttributes} from the
     * database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link ObjectObjectAttribute ObjectObjectAttributes}
     */
    public List<ObjectObjectAttribute> getObjectObjectAtributes() {
        if (objectObjectAttributes == null) {
            objectObjectAttributes = getTableElements(Tables.OBJECT_OBJECT_ATTRIBUTES,
                ObjectObjectAttribute.class);
        }
        return objectObjectAttributes;
    }

    /**
     * Returns a complete list of {@link Vertex vertices} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link Vertex vertices}
     */
    public List<Vertex> getVertices() {
        if (vertices == null) {
            vertices = getTableElements(Tables.VERTICES, Vertex.class);
        }
        return vertices;
    }

    /**
     * Returns a complete list of {@link DBEdge edges} from the database
     * Calls {@link #getTableElements(Tables, Class)}
     *
     * @return Complete list of {@link DBEdge edges}
     */
    public List<DBEdge> getEdges() {
        if (edges == null) {
            edges = getTableElements(Tables.EDGES, DBEdge.class);
        }
        return edges;
    }

    private <T> List<T> getTableElements(Tables table, Class<T> clazz) {
        List<T> elements = new LinkedList<>();
        try {
            DBTable tTable = new DBTable(
                getConnection().orElseThrow(IllegalStateException::new), table);
            ResultSet rs = tTable.select();
            while (rs.next()) {
                Constructor<T> construct = clazz.getConstructor(ResultSet.class);
                T element = construct.newInstance(rs);
                elements.add(element);
            }
        } catch (SQLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        dataSource.close();
    }

    /**
     * Returns the {@link ABSExporter exporter} for the database
     *
     * @return {@link ABSExporter exporter}
     */
    public ABSExporter getExporter() {
        return exporter;
    }

    /**
     * Returns the {@link DatabaseUser user} the database is connected with.
     *
     * @return The current database user
     */
    public DatabaseUser getUser() {
        return user;
    }

    /**
     * Set the {@link DatabaseUser user} and re-initialize the database connection via {@link
     * #init()}
     *
     * @param user New database user
     */
    public void setUser(DatabaseUser user) {
        this.user = user;
        init();
    }
}
