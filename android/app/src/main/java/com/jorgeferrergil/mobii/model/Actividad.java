package com.jorgeferrergil.mobii.model;

public class Actividad {

    private String nombre;
    private String categoria;
    private String categoriaLabel;
    private double lat;
    private double lng;
    private String horario;
    private String telefono;
    private String web;
    private String direccion;

    public Actividad(String nombre, String categoria, String categoriaLabel,
                     double lat, double lng, String direccion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.categoriaLabel = categoriaLabel;
        this.lat = lat;
        this.lng = lng;
        this.direccion = direccion;
        this.horario = "";
        this.telefono = "";
        this.web = "";
    }

    public Actividad(String nombre, String categoria, String categoriaLabel,
                     double lat, double lng, String direccion,
                     String horario, String telefono, String web) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.categoriaLabel = categoriaLabel;
        this.lat = lat;
        this.lng = lng;
        this.direccion = direccion;
        this.horario = horario;
        this.telefono = telefono;
        this.web = web;
    }

    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getCategoriaLabel() { return categoriaLabel; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getHorario() { return horario; }
    public String getTelefono() { return telefono; }
    public String getWeb() { return web; }
    public String getDireccion() { return direccion != null ? direccion : "Valencia"; }
}
