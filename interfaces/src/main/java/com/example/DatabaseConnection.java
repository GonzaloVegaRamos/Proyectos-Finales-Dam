package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL de conexión a la base de datos SQLite
    private static final String URL = "jdbc:sqlite:src/main/resources/almacen.db";

    // Método para obtener una conexión a la base de datos
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Método para ejecutar una consulta SELECT y devolver un ResultSet
    public ResultSet executeQuery(String sql) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.executeQuery();
    }

    // Método para ejecutar una consulta INSERT, UPDATE o DELETE
    public int executeUpdate(String sql) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.executeUpdate();
    }

    // Método para cerrar una conexión
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    // Método para cerrar un ResultSet
    public void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar el ResultSet: " + e.getMessage());
            }
        }
    }

    // Método para cerrar un PreparedStatement
    public void closeStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar el PreparedStatement: " + e.getMessage());
            }
        }
    }
}