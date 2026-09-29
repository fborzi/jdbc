package edu.oop.jdbc.transactions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejemplo completo del patrón DAO (Data Access Object) con las 4 operaciones CRUD
 * (Create, Read, Update, Delete) y mapeo de ResultSet a objetos de dominio.
 */
public class EmpleadoDAO {


    // CREATE: Insertar un nuevo registro y recuperar su ID autogenerado
    public EmpleadoModel guardar(Connection conn, EmpleadoModel empleado) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, direccion, email) VALUES (?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS permite obtener el ID autoincremental generado por la BD
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, empleado.getNombre());
            stmt.setString(2, empleado.getDireccion());
            stmt.setString(3, empleado.getEmail());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    empleado.setId(rs.getInt(1));
                }
            }
            return empleado;
        }
    }


    // READ: Buscar por ID (mapeando ResultSet -> Objeto)
    public EmpleadoModel buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, nombre, direccion, email FROM empleados WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
                return null;
            }
        }
    }

    // READ: Listar todos (mapeando ResultSet -> Objeto)
    public List<EmpleadoModel> listarTodos(Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, direccion, email FROM empleados";
        List<EmpleadoModel> empleados = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                empleados.add(mapearEmpleado(rs));
            }
        }
        return empleados;
    }


    // UPDATE: Actualizar entidad completa o campos específicos
    public boolean actualizar(Connection conn, EmpleadoModel empleado) throws SQLException {
        String sql = "UPDATE empleados SET nombre = ?, direccion = ?, email = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empleado.getNombre());
            stmt.setString(2, empleado.getDireccion());
            stmt.setString(3, empleado.getEmail());
            stmt.setInt(4, empleado.getId());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public void actualizarDireccion(Connection conn, String nombre, String direccion) throws SQLException {
        String query = "UPDATE empleados SET direccion = ? WHERE nombre = ?";
        // Usamos try-with-resources SOLO para el PreparedStatement. La conexión NO se cierra acá.
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, direccion);
            stmt.setString(2, nombre);
            stmt.executeUpdate();
        }
    }

    public void actualizarEmail(Connection conn, String nombre, String email) throws SQLException {
        String query = "UPDATE empleados SET email = ? WHERE nombre = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, nombre);
            stmt.executeUpdate();
        }
    }


    // DELETE: Eliminar un registro por su clave primaria
    public boolean eliminar(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }


    // Método auxiliar privado: convierte una fila del ResultSet en un objeto Java
    private EmpleadoModel mapearEmpleado(ResultSet rs) throws SQLException {
        return new EmpleadoModel(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("direccion"),
                rs.getString("email")
        );
    }
}
