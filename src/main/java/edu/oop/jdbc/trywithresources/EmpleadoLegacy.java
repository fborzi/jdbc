package edu.oop.jdbc.trywithresources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class EmpleadoLegacy {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = getConnection(url, user, password);
            String sql = "SELECT * FROM employee " +
                         "WHERE id = ?";

            statement = conn.prepareStatement(sql);
            statement.setInt(1, 101);

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Empleado: " + resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // El orden de cierre debe ser inverso a la apertura:
            // ResultSet -> PreparedStatement -> Connection
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
