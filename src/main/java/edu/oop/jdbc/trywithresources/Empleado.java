package edu.oop.jdbc.trywithresources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Empleado {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        // Inicialización de datos de prueba
        setupDatabase(url, user, password);
        String sql = "SELECT * FROM employee WHERE id = ?";

        // Try-with-resources gestiona el cierre de Connection y PreparedStatement
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, 101);
            // Anidamos el ResultSet para cerrar la lectura ni bien termina el procesamiento
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Empleado: " + rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase(String url, String user, String password) {
        String createDb = "CREATE TABLE IF NOT EXISTS employee (id INT PRIMARY KEY, name VARCHAR(100));";
        String insertData = "INSERT INTO employee VALUES (101, 'Ana Pérez');";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement ps = connection.prepareStatement(createDb)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(insertData)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
