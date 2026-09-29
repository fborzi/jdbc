package edu.oop.jdbc.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase demostrativa con las operaciones más comunes de DatabaseMetaData en JDBC:
 * 1. Información general del Motor de Base de Datos y del Driver JDBC.
 * 2. Verificación de capacidades soportadas por el motor (Transacciones, Cursores, Batch).
 * 3. Descubrimiento de Tablas existentes en la base de datos (getTables).
 * 4. Inspección de Columnas de una tabla específica (getColumns).
 * 5. Consulta de Claves Primarias (getPrimaryKeys) y Claves Foráneas (getImportedKeys).
 */
public class DatabaseMetaDataEjemplo {

    private static final String URL = "jdbc:h2:mem:metadata_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            inicializarEsquemaDePrueba(conn);

            // Obtenemos el objeto DatabaseMetaData a partir de la conexión activa
            DatabaseMetaData metaData = conn.getMetaData();

            ejemplo1InfoMotorYDriver(metaData);
            ejemplo2CapacidadesDelMotor(metaData);
            ejemplo3ListarTablas(metaData);
            ejemplo4ListarColumnasDeTabla(metaData, "EMPLEADOS");
            ejemplo5ClavesPrimariasYForaneas(metaData, "EMPLEADOS");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 1. Información general del Motor de Base de Datos y del Driver JDBC
    private static void ejemplo1InfoMotorYDriver(DatabaseMetaData metaData) throws SQLException {
        System.out.println("=== 1. INFORMACIÓN DEL MOTOR Y DEL DRIVER ===");
        System.out.println("Motor de BD (Producto): " + metaData.getDatabaseProductName());
        System.out.println("Versión del Motor:      " + metaData.getDatabaseProductVersion());
        System.out.println("Nombre del Driver JDBC: " + metaData.getDriverName());
        System.out.println("Versión del Driver:     " + metaData.getDriverVersion());
        System.out.println("URL de Conexión:        " + metaData.getURL());
        System.out.println("Usuario conectado:      " + metaData.getUserName());
        System.out.println();
    }


    // 2. Consulta de capacidades y soporte técnico del motor
    private static void ejemplo2CapacidadesDelMotor(DatabaseMetaData metaData) throws SQLException {
        System.out.println("=== 2. CAPACIDADES SOPORTADAS POR EL MOTOR ===");
        System.out.println("¿Soporta transacciones (ACID)?          " + metaData.supportsTransactions());
        System.out.println("¿Soporta actualizaciones en lote (Batch)? " + metaData.supportsBatchUpdates());
        System.out.println("¿Soporta ResultSet desplazable (Scroll)? " +
                metaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        System.out.println("¿Soporta ResultSet actualizable?        " +
                metaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
        System.out.println("Máximo de conexiones simultáneas (0=sin límite): " + metaData.getMaxConnections());
        System.out.println();
    }


    // 3. Descubrimiento de tablas en el esquema (getTables)
    private static void ejemplo3ListarTablas(DatabaseMetaData metaData) throws SQLException {
        System.out.println("=== 3. TABLAS DEFINIDAS EN LA BASE DE DATOS (getTables) ===");

        // Parámetros: (catalog, schemaPattern, tableNamePattern, types)
        try (ResultSet rsTablas = metaData.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
            while (rsTablas.next()) {
                String esquema = rsTablas.getString("TABLE_SCHEM");
                String nombreTabla = rsTablas.getString("TABLE_NAME");
                String tipo = rsTablas.getString("TABLE_TYPE");
                System.out.printf("  Esquema: %-8s | Tabla: %-15s | Tipo: %s%n", esquema, nombreTabla, tipo);
            }
        }
        System.out.println();
    }


    // 4. Inspección de las columnas de una tabla (getColumns)
    private static void ejemplo4ListarColumnasDeTabla(DatabaseMetaData metaData, String nombreTabla) throws SQLException {
        System.out.println("=== 4. COLUMNAS DE LA TABLA '" + nombreTabla + "' (getColumns) ===");

        // Parámetros: (catalog, schemaPattern, tableNamePattern, columnNamePattern)
        try (ResultSet rsColumnas = metaData.getColumns(null, "PUBLIC", nombreTabla, "%")) {
            while (rsColumnas.next()) {
                String nombreCol = rsColumnas.getString("COLUMN_NAME");
                String tipoSql = rsColumnas.getString("TYPE_NAME");
                int tamano = rsColumnas.getInt("COLUMN_SIZE");
                String aceptaNull = rsColumnas.getString("IS_NULLABLE"); // "YES" o "NO"
                String autoIncremento = rsColumnas.getString("IS_AUTOINCREMENT"); // "YES" o "NO"

                System.out.printf("  Columna: %-16s | Tipo: %-18s | Tamaño: %-6d | ¿Null?: %-3s | ¿AutoInc?: %s%n",
                        nombreCol, tipoSql, tamano, aceptaNull, autoIncremento);
            }
        }
        System.out.println();
    }


    // 5. Inspección de Claves Primarias y Foráneas (getPrimaryKeys / getImportedKeys)
    private static void ejemplo5ClavesPrimariasYForaneas(DatabaseMetaData metaData, String nombreTabla) throws SQLException {
        System.out.println("=== 5. CLAVES PRIMARIAS Y FORÁNEAS DE '" + nombreTabla + "' ===");

        // A) Claves Primarias (Primary Keys)
        try (ResultSet rsPk = metaData.getPrimaryKeys(null, "PUBLIC", nombreTabla)) {
            while (rsPk.next()) {
                String columnaPk = rsPk.getString("COLUMN_NAME");
                String nombreConstraint = rsPk.getString("PK_NAME");
                System.out.println("  [PK] Columna: " + columnaPk + " (Constraint: " + nombreConstraint + ")");
            }
        }

        // B) Claves Foráneas (Foreign Keys importadas por la tabla)
        try (ResultSet rsFk = metaData.getImportedKeys(null, "PUBLIC", nombreTabla)) {
            while (rsFk.next()) {
                String columnaFk = rsFk.getString("FKCOLUMN_NAME");
                String tablaReferenciada = rsFk.getString("PKTABLE_NAME");
                String columnaReferenciada = rsFk.getString("PKCOLUMN_NAME");
                System.out.printf("  [FK] Columna local '%s' referencia a -> %s(%s)%n",
                        columnaFk, tablaReferenciada, columnaReferenciada);
            }
        }
    }


    // Inicialización de tablas relacionadas para la demostración
    private static void inicializarEsquemaDePrueba(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE departamentos (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(80) NOT NULL
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE empleados (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        email VARCHAR(120) UNIQUE,
                        salario DECIMAL(12, 2) NOT NULL,
                        departamento_id INT NOT NULL,
                        CONSTRAINT fk_empleado_depto FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
                    );
                    """);
        }
    }
}
