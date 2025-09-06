package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    private static final String URL = "jdbc:mariadb://localhost:6666/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<Persona> obtenerPersonas() throws SQLException {
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM personas")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                Persona p = new Persona(id, nombre);
                try (PreparedStatement psDir = conn.prepareStatement(
                        "SELECT d.id, td.tipo, d.direccion " +
                                "FROM direcciones d " +
                                "JOIN tipos_direccion td ON d.tipo_id = td.id " +
                                "WHERE d.persona_id = ?")) {
                    psDir.setInt(1, id);
                    try (ResultSet rsDir = psDir.executeQuery()) {
                        while (rsDir.next()) {
                            String tipo = rsDir.getString("tipo");
                            String direccion = rsDir.getString("direccion");
                            p.addDireccion(new Direccion(rsDir.getInt("id"), tipo, direccion));
                        }
                    }
                }

                try (PreparedStatement psTel = conn.prepareStatement(
                        "SELECT telefono FROM telefono WHERE persona_id = ?")) {
                    psTel.setInt(1, id);
                    try (ResultSet rsTel = psTel.executeQuery()) {
                        while (rsTel.next()) {
                            p.addTelefono(rsTel.getString("telefono"));
                        }
                    }
                }

                personas.add(p);
            }
        }
        return personas;
    }

    public static void insertarPersona(String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO personas(nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);

                    for (Direccion dir : direcciones) {
                        try (PreparedStatement psDir = conn.prepareStatement(
                                "INSERT INTO direcciones(persona_id, tipo_id, direccion) VALUES (?, ?, ?)")) {
                            psDir.setInt(1, id);
                            psDir.setInt(2, obtenerTipoId(conn, dir.getTipo()));
                            psDir.setString(3, dir.getDireccion());
                            psDir.executeUpdate();
                        }
                    }
                    for (String tel : telefonos) {
                        try (PreparedStatement psTel = conn.prepareStatement(
                                "INSERT INTO telefono(persona_id, telefono) VALUES (?, ?)")) {
                            psTel.setInt(1, id);
                            psTel.setString(2, tel);
                            psTel.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public static void actualizarPersona(int id, String nombre, List<Direccion> direcciones, List<String> telefonos) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE personas SET nombre = ? WHERE id = ?")) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();

            try (PreparedStatement psDelDir = conn.prepareStatement("DELETE FROM direcciones WHERE persona_id = ?")) {
                psDelDir.setInt(1, id);
                psDelDir.executeUpdate();
            }

            for (Direccion dir : direcciones) {
                try (PreparedStatement psDir = conn.prepareStatement(
                        "INSERT INTO direcciones(persona_id, tipo_id, direccion) VALUES (?, ?, ?)")) {
                    psDir.setInt(1, id);
                    psDir.setInt(2, obtenerTipoId(conn, dir.getTipo()));
                    psDir.setString(3, dir.getDireccion());
                    psDir.executeUpdate();
                }
            }

            try (PreparedStatement psDelTel = conn.prepareStatement("DELETE FROM telefono WHERE persona_id = ?")) {
                psDelTel.setInt(1, id);
                psDelTel.executeUpdate();
            }

            for (String tel : telefonos) {
                try (PreparedStatement psTel = conn.prepareStatement(
                        "INSERT INTO telefono(persona_id, telefono) VALUES (?, ?)")) {
                    psTel.setInt(1, id);
                    psTel.setString(2, tel);
                    psTel.executeUpdate();
                }
            }
        }
    }

    public static void eliminarPersona(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM personas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static List<String> obtenerTiposDireccion() throws SQLException {
        List<String> tipos = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT tipo FROM tipos_direccion")) {

            while (rs.next()) {
                tipos.add(rs.getString("tipo"));
            }
        }
        return tipos;
    }

    private static int obtenerTipoId(Connection conn, String tipo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM tipos_direccion WHERE tipo = ?")) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Tipo de dirección no encontrado: " + tipo);
                }
            }
        }
    }

    public static Persona obtenerPersonaPorId(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM personas WHERE id = ?")) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    Persona p = new Persona(id, nombre);

                    try (PreparedStatement psDir = conn.prepareStatement(
                            "SELECT d.id, td.tipo, d.direccion " +
                                    "FROM direcciones d " +
                                    "JOIN tipos_direccion td ON d.tipo_id = td.id " +
                                    "WHERE d.persona_id = ?")) {
                        psDir.setInt(1, id);
                        try (ResultSet rsDir = psDir.executeQuery()) {
                            while (rsDir.next()) {
                                String tipo = rsDir.getString("tipo");
                                String direccion = rsDir.getString("direccion");
                                p.addDireccion(new Direccion(rsDir.getInt("id"), tipo, direccion));
                            }
                        }
                    }

                    try (PreparedStatement psTel = conn.prepareStatement(
                            "SELECT telefono FROM telefono WHERE persona_id = ?")) {
                        psTel.setInt(1, id);
                        try (ResultSet rsTel = psTel.executeQuery()) {
                            while (rsTel.next()) {
                                p.addTelefono(rsTel.getString("telefono"));
                            }
                        }
                    }

                    return p;
                } else {
                    return null;
                }
            }
        }
    }
}