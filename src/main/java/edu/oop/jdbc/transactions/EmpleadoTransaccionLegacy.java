package edu.oop.jdbc.transactions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Ejemplo de transacción JDBC utilizando el esquema clásico try-catch-finally,
 * con manejo manual de rollback y cierre de recursos en orden inverso.
 */
public class EmpleadoTransaccionLegacy {

    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_comercio_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static void main(String[] args) {

        String actualizarDireccion = "UPDATE empleados SET direccion = ? WHERE nombre = ?";
        String actualizarEmail = "UPDATE empleados SET email = ? WHERE nombre = ?";

        // 1. Declaramos las referencias afuera del bloque try para que sean visibles en catch y finally
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;

        try {
            // 2. Abrimos la conexión y desactivamos el auto-commit
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);

            // Operación 1: Actualizar dirección
            stmt1 = conn.prepareStatement(actualizarDireccion);
            stmt1.setString(1, "Lamadrid 123");
            stmt1.setString(2, "Maria");
            stmt1.executeUpdate();

            // Operación 2: Actualizar email
            stmt2 = conn.prepareStatement(actualizarEmail);
            stmt2.setString(1, "maria@gmail.com");
            stmt2.setString(2, "Maria");
            stmt2.executeUpdate();

            // 3. Si todo salió bien, confirmamos la transacción
            conn.commit();
            System.out.println("Transacción confirmada con éxito.");

        } catch (SQLException e) {
            System.err.println("Ocurrió un error en la transacción. Revirtiendo cambios: " + e.getMessage());

            // 4. Hacemos el rollback sobre la MISMA conexión si llegó a abrirse
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Rollback realizado con éxito.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error crítico al intentar revertir los cambios: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();

        } finally {
            // 5. Cerramos los recursos manualmente en orden inverso a su apertura:
            //    PreparedStatement -> Connection
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restauramos el estado por defecto antes de cerrar
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
