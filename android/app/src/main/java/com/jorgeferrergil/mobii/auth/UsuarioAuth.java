package com.jorgeferrergil.mobii.auth;

public class UsuarioAuth {
    private int id;
    private String nombre;
    private String email;
    private String rol;
    private int activo;
    private String discapacidad;
    private String created_at;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public int getActivo() { return activo; }
    public String getDiscapacidad() { return discapacidad; }
    public String getCreatedAt() { return created_at; }
}
