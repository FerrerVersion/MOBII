package com.jorgeferrergil.mobii.auth;

public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;

    public RegisterRequest(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
