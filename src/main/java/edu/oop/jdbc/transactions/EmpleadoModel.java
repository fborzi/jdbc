package edu.oop.jdbc.transactions;

/**
 * Clase de dominio (Entidad / Modelo) que representa una fila de la tabla 'empleados'.
 */
public class EmpleadoModel {

    private Integer id;
    private String nombre;
    private String direccion;
    private String email;

    public EmpleadoModel() {
    }

    public EmpleadoModel(String nombre, String direccion, String email) {
        this(null, nombre, direccion, email);
    }

    public EmpleadoModel(Integer id, String nombre, String direccion, String email) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmpleadoModel{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
