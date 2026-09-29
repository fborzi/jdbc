package edu.oop.jdbc.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Clase de ejemplo con las operaciones más comunes de ResultSet en JDBC:
 * 1. Recorrido estándar (Forward-Only), lectura por nombre/índice y control de NULLs (wasNull).
 * 2. Navegación libre con cursores desplazables (Scrollable ResultSet).
 * 3. Modificación, inserción y borrado desde el cursor (Updatable ResultSet).
 * 4. Inspección de columnas de una consulta con ResultSetMetaData.
 */
public class ResultSetEjemplo {

    private static final String URL = "jdbc:h2:mem:resultset_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            inicializarDatosDePrueba(conn);

            ejemplo1RecorridoEstandarYNulls(conn);
            ejemplo2CursorDesplazable(conn);
            ejemplo3CursorActualizable(conn);
            ejemplo4ResultSetMetaData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 1. Recorrido estándar (TYPE_FORWARD_ONLY), getters tipados y wasNull()
    private static void ejemplo1RecorridoEstandarYNulls(Connection conn) throws SQLException {
        System.out.println("=== 1. RECORRIDO ESTÁNDAR Y CONTROL DE NULL (wasNull) ===");
        String sql = "SELECT id, nombre, salario, bono, activo, fecha_ingreso FROM empleados";

        // Por defecto, prepareStatement crea un ResultSet TYPE_FORWARD_ONLY y CONCUR_READ_ONLY
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Lectura por nombre de columna (recomendado por legibilidad)
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double salario = rs.getDouble("salario");

                // Lectura de una columna numérica que puede ser NULL en la BD:
                // getDouble devuelve 0.0 si el valor SQL es NULL; wasNull() nos confirma si era NULL realmente.
                double bono = rs.getDouble("bono");
                String bonoTexto = rs.wasNull() ? "Sin bono (NULL)" : String.format("$%.2f", bono);

                // También se puede leer por índice de columna (en JDBC empieza en 1, NO en 0)
                boolean activo = rs.getBoolean(5);
                Date fechaIngreso = rs.getDate("fecha_ingreso");

                System.out.printf("ID: %d | Nombre: %-12s | Salario: $%8.2f | Bono: %-15s | Activo: %-5b | Ingreso: %s%n",
                        id, nombre, salario, bonoTexto, activo, fechaIngreso);
            }
        }
        System.out.println();
    }


    // 2. Navegación bidireccional (TYPE_SCROLL_INSENSITIVE)
    private static void ejemplo2CursorDesplazable(Connection conn) throws SQLException {
        System.out.println("=== 2. CURSOR DESPLAZABLE (SCROLLABLE RESULTSET) ===");
        String sql = "SELECT id, nombre, salario FROM empleados ORDER BY id";

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE, // Permite moverse hacia adelante, atrás y a filas específicas
                ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = ps.executeQuery()) {

            // Ir directamente a la última fila
            if (rs.last()) {
                System.out.println("Último registro (fila " + rs.getRow() + "): " + rs.getString("nombre"));
            }

            // Ir directamente a la primera fila
            if (rs.first()) {
                System.out.println("Primer registro (fila " + rs.getRow() + "): " + rs.getString("nombre"));
            }

            // Saltar a una fila específica (ej. fila 2)
            if (rs.absolute(2)) {
                System.out.println("Acceso directo a fila 2 (absolute): " + rs.getString("nombre"));
            }

            // Moverse relativamente desde la posición actual (+1 avanza una fila, -1 retrocede una fila)
            if (rs.relative(1)) {
                System.out.println("Avanzando 1 fila desde la 2 (relative +1 -> fila " + rs.getRow() + "): " + rs.getString("nombre"));
            }

            // Recorrer de atrás hacia adelante: nos posicionamos después del último y usamos previous()
            System.out.println("Recorrido en orden inverso con previous():");
            rs.afterLast();
            while (rs.previous()) {
                System.out.println("  <- Fila " + rs.getRow() + ": " + rs.getString("nombre"));
            }
        }
        System.out.println();
    }


    // 3. Modificación e inserción desde el cursor (CONCUR_UPDATABLE)
    private static void ejemplo3CursorActualizable(Connection conn) throws SQLException {
        System.out.println("=== 3. CURSOR ACTUALIZABLE (UPDATABLE RESULTSET) ===");
        // Nota: Para que sea actualizable, la consulta debe seleccionar la clave primaria (id)
        String sql = "SELECT id, nombre, salario, bono, activo, fecha_ingreso FROM empleados";

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE); // Habilita modificaciones directas sobre el ResultSet
             ResultSet rs = ps.executeQuery()) {

            // A) Actualizar una fila existente mientras recorremos
            while (rs.next()) {
                if ("Carlos Gómez".equals(rs.getString("nombre"))) {
                    double salarioActual = rs.getDouble("salario");
                    rs.updateDouble("salario", salarioActual + 50000.0); // Modifica el buffer del cursor
                    rs.updateRow(); // Impacta el cambio en la base de datos
                    System.out.println("Salario de Carlos Gómez actualizado a: $" + rs.getDouble("salario"));
                }
            }

            // B) Insertar una nueva fila directamente desde el ResultSet
            rs.moveToInsertRow(); // Mueve el cursor a un buffer especial de inserción
            rs.updateInt("id", 4);
            rs.updateString("nombre", "Lucía Fernández");
            rs.updateDouble("salario", 920000.0);
            rs.updateDouble("bono", 45000.0);
            rs.updateBoolean("activo", true);
            rs.updateDate("fecha_ingreso", Date.valueOf("2026-03-15"));
            rs.insertRow(); // Inserta la fila en la BD
            rs.moveToCurrentRow(); // Vuelve el cursor a la fila donde estaba parado antes
            System.out.println("Nueva empleada 'Lucía Fernández' insertada mediante el ResultSet.");
        }
        System.out.println();
    }


    // 4. Metadatos de la consulta (ResultSetMetaData)
    private static void ejemplo4ResultSetMetaData(Connection conn) throws SQLException {
        System.out.println("=== 4. METADATOS DEL RESULTSET (ResultSetMetaData) ===");
        String sql = "SELECT id AS codigo_empleado, nombre, salario FROM empleados";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int totalColumnas = meta.getColumnCount();
            System.out.println("Cantidad de columnas devueltas por el SELECT: " + totalColumnas);

            for (int i = 1; i <= totalColumnas; i++) {
                System.out.printf("  Columna %d -> Nombre real: %-10s | Alias (Label): %-16s | Tipo SQL: %-10s | Clase Java: %s%n",
                        i,
                        meta.getColumnName(i),
                        meta.getColumnLabel(i),
                        meta.getColumnTypeName(i),
                        meta.getColumnClassName(i));
            }
        }
    }

    // Inicialización de tabla y datos en memoria para la demostración
    private static void inicializarDatosDePrueba(Connection conn) throws SQLException {
        String createTable = """
                CREATE TABLE empleados (
                    id INT PRIMARY KEY,
                    nombre VARCHAR(100) NOT NULL,
                    salario DECIMAL(12, 2) NOT NULL,
                    bono DECIMAL(12, 2),
                    activo BOOLEAN NOT NULL,
                    fecha_ingreso DATE NOT NULL
                );
                """;

        String insertData = """
                INSERT INTO empleados VALUES (1, 'Ana Pérez', 850000.00, 50000.00, true, '2023-05-10');
                INSERT INTO empleados VALUES (2, 'Carlos Gómez', 780000.00, NULL, true, '2024-01-20');
                INSERT INTO empleados VALUES (3, 'María López', 910000.00, 75000.00, false, '2022-11-01');
                """;

        try (PreparedStatement psCreate = conn.prepareStatement(createTable)) {
            psCreate.executeUpdate();
        }
        try (PreparedStatement psInsert = conn.prepareStatement(insertData)) {
            psInsert.executeUpdate();
        }
    }
}
