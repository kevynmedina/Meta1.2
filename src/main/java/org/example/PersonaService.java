package org.example;

import java.sql.SQLException;
import java.util.List;

public class PersonaService {
    private final IPersonaDAO personaDAO;

    public PersonaService(IPersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    public List<Persona> obtenerPersonas() throws SQLException {
        return personaDAO.obtenerPersonas();
    }

    public void agregarPersona(String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException {
        personaDAO.insertarPersona(nombre, direcciones, telefonos);
    }

    public void modificarPersona(int id, String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException {
        personaDAO.actualizarPersona(id, nombre, direcciones, telefonos);
    }

    public void eliminarPersona(int id) throws SQLException {
        personaDAO.eliminarPersona(id);
    }

    public Persona obtenerPersonaPorId(int id) throws SQLException {
        return personaDAO.obtenerPersonaPorId(id);
    }

    public List<String> obtenerTiposDireccion() throws SQLException {
        return personaDAO.obtenerTiposDireccion();
    }
}
