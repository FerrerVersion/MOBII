package com.jorgeferrergil.mobii.auth;

public class PerfilUpdateRequest {
    private String nombre;
    private String discapacidad;

    public PerfilUpdateRequest(String nombre, String discapacidad) {
        this.nombre = nombre;
        this.discapacidad = discapacidad;
    }

    public String getNombre() { return nombre; }
    public String getDiscapacidad() { return discapacidad; }
}
