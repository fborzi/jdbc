package edu.oop.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Ejemplo de transacción coordinada desde afuera del DAO usando try-with-resources
 * y haciendo rollback sobre la misma conexión compartida.
 */
public class EmpleadoTransaccionAfuera {

    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_comercio_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static void main(String[] args) {
        // Instanciamos el DAO que contiene las consultas individuales
        EmpleadoDAO empleadoDao = new EmpleadoDAO();

        // 1. La conexión se abre en el try-with-resources externo
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // 2. Iniciamos la transacción desactivando el auto-commit
            conn.setAutoCommit(false);

            // 3. Bloque try-catch interno para las operaciones de la transacción.
            //    Así mantenemos 'conn' viva y accesible en el catch si algo falla.
            try {
                // Ejecutamos las operaciones pasando la MISMA conexión por parámetro
                empleadoDao.actualizarDireccion(conn, "Maria", "Lamadrid 123");
                empleadoDao.actualizarEmail(conn, "Maria", "maria@gmail.com");

                // 4. Si todas las operaciones del DAO salieron bien, confirmamos los cambios
                conn.commit();
                System.out.println("Transacción confirmada con éxito.");

            } catch (SQLException e) {
                // 5. Si alguna operación del DAO lanzó SQLException, revertimos en la MISMA conexión
                System.err.println("Error durante las operaciones del DAO. Ejecutando rollback: " + e.getMessage());
                conn.rollback();
                System.out.println("Rollback realizado con éxito sobre la misma conexión.");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error al abrir/cerrar la conexión o al intentar hacer rollback: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
