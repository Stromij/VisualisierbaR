package com.github.bachelorpraktikum.dbvisualization.database;

import com.github.bachelorpraktikum.dbvisualization.database.model.Tables;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

class DBTable {

    private Tables table;
    private String select_query_string = "SELECT %s FROM %s%s;";
    private PreparedStatement select_query_statement;
    private ResultSet select_result;

    /**
     * Creates a database table with the given connection and {@link Tables table}.
     *
     * @param connection Connection to use for queries.
     * @param table {@link Tables table} to get information from
     * @throws SQLException In case of a failed statement creation.
     */
    public DBTable(Connection connection, Tables table) throws SQLException {
        this.table = table;
        String where = "";
        if (table.getWhereCondition().isPresent()) {
            where = String.format(" WHERE %s", table.getWhereCondition().get());
        }
        select_query_string = String
            .format(select_query_string, getColumnNamesAsString(), getName(), where);
        createSelectStatement(connection);
    }

    DBTable() {
    }

    private void createSelectStatement(Connection connection) throws SQLException {
        select_query_statement = connection.prepareStatement(select_query_string);
    }

    /**
     * Executes a select query over all column names (`getColumnNamesAsString`)
     *
     * @return ResultSet for 'SELECT `getColumnNamesAsString` FROM `getName()`'
     * @throws SQLException SQL Exception if fault during query execution occurs
     */
    public ResultSet select() throws SQLException {
        return select(false);
    }

    ResultSet select(boolean update) throws SQLException {
        if (select_result != null && !update) {
            return select_result;
        }
        Logger.getLogger(getClass().getName())
            .finest(String.format("Executing SQL Query: %s", select_query_statement.toString()));
        select_result = select_query_statement.executeQuery();
        return select_result;
    }

    List<String> getColumnNames() {
        return table.getColumnNames();
    }

    /**
     * Returns a comma seperated list of column names.
     * Column names are retrieved by `getColumnNames`.
     *
     * @return Column names (comma seperated)
     */
    String getColumnNamesAsString() {
        return getColumnNames().stream().reduce((s, s1) -> s + ", " + s1).get();
    }

    /**
     * Returns the name
     *
     * @return Name
     */
    String getName() {
        return table.getName();
    }
}
