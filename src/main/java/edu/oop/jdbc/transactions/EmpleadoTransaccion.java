package edu.oop.jdbc.transactions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Ejemplo de transacción JDBC utilizando try-with-resources,
 * realizando el rollback sobre la misma conexión antes de que se cierre.
 */
public class EmpleadoTransaccion {

    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_comercio_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static void main(String[] args) {

        String actualizarDireccion = "UPDATE empleados SET direccion = ? WHERE nombre = ?";
        String actualizarEmail = "UPDATE empleados SET email = ? WHERE nombre = ?";

        // 1. El try-with-resources EXTERNO abre la conexión y garantiza su cierre al salir del bloque.
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // 2. Desactivamos el auto-commit en esta conexión para iniciar la transacción.
            conn.setAutoCommit(false);

            // 3. El try-with-resources INTERNO gestiona los PreparedStatements.
            //    Como estamos DENTRO del bloque de 'conn', la conexión sigue abierta y en alcance (scope).
            try (PreparedStatement stmt1 = conn.prepareStatement(actualizarDireccion);
                 PreparedStatement stmt2 = conn.prepareStatement(actualizarEmail)) {

                // Operación 1: Actualizar dirección
                stmt1.setString(1, "Lamadrid 123");
                stmt1.setString(2, "Maria");
                stmt1.executeUpdate();

                // Operación 2: Actualizar email
                stmt2.setString(1, "maria@gmail.com");
                stmt2.setString(2, "Maria");
                stmt2.executeUpdate();

                // 4. Si ambas operaciones fueron exitosas, confirmamos los cambios en la BD.
                conn.commit();
                System.out.println("Transacción confirmada con éxito.");

            } catch (SQLException e) {
                // 5. Si falla cualquier PreparedStatement, caemos en este catch interno.
                //    Aca 'conn' sigue ABIERTA y es la MISMA sesión donde se hicieron los UPDATE,
                //    por lo que podemos hacer rollback real de los cambios pendientes.
                System.err.println("Ocurrió un error en la transacción. Revirtiendo cambios: " + e.getMessage());
                conn.rollback();
                System.out.println("Rollback realizado con éxito sobre la misma conexión.");
            } finally {
                // Buena práctica: restaurar el auto-commit a su estado por defecto antes de que se cierre/devuelva al pool.
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            // Este catch externo captura errores al intentar conectar a la BD o si falló el propio rollback().
            System.err.println("Error crítico de conexión o al revertir la transacción: " + e.getMessage());
            e.printStackTrace();
        }
        // Al finalizar este bloque, el try-with-resources externo cierra 'conn' automáticamente.
    }
}
