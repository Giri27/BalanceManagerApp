package com.bluetigers.balancemanagerapp.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Francesco Girardi
 * @version 1.0.0
 * @since 1.0.0
 */
public class DatabaseConnection {

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    /**
     * Create the Database Connection and Statement
     */
    public DatabaseConnection() {
        setConnection();
    }

    /**
     * Get Database Connection
     *
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get Database Statement
     *
     * @return Statement
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * @param sql: code to execute
     * @return a prepare statement for the sql
     */
    public PreparedStatement getPreparedStatement(String sql) {
        try {
            preparedStatement = getConnection().prepareStatement(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return preparedStatement;
    }

    /**
     * Close the Database Connection and Statement
     */
    public void closeConnection() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void setConnection() {
        String URL = "jdbc:postgresql://2.224.128.111:5432/balance_manager";
        try {
            connection = DriverManager.getConnection(URL, "francesco", "Rosita98!");
            Log.v("DatabaseConnection", "dbConnection: Connected!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Log.v("DatabaseConnection", "dbConnection: " + throwables);
        }

        setStatement();
    }

    private void setStatement() {
        try {
            if (connection != null)
                statement = connection.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
