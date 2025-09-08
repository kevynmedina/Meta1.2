package org.example;

import org.example.Persona;
import org.example.Direccion;
import java.sql.SQLException;
import java.util.List;

public interface IPersonaDAO {
    List<Persona> obtenerPersonas() throws SQLException;
    void insertarPersona(String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException;
    void actualizarPersona(int id, String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException;
    void eliminarPersona(int id) throws SQLException;
    Persona obtenerPersonaPorId(int id) throws SQLException;
    List<String> obtenerTiposDireccion() throws SQLException;
}
