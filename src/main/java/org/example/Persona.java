package org.example;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private List<Direccion> direcciones = new ArrayList<>();
    private List<String> telefonos = new ArrayList<>();

    public Persona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Direccion> getDirecciones() { return direcciones; }
    public List<String> getTelefonos() { return telefonos; }
    public void addDireccion(Direccion direccion) { direcciones.add(direccion); }
    public void addTelefono(String telefono) { telefonos.add(telefono); }
}