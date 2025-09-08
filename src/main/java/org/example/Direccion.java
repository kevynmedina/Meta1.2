package org.example;

public class Direccion {
    private int id;
    private String tipo;
    private String direccion;

    public Direccion(int id, String tipo, String direccion) {
        this.id = id;
        this.tipo = tipo;
        this.direccion = direccion;
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDireccion() { return direccion; }

    @Override
    public String toString() {
        return tipo + ": " + direccion;
    }
}