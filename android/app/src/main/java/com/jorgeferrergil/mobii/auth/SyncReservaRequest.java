package com.jorgeferrergil.mobii.auth;

public class SyncReservaRequest {
    private String nombre;
    private String recogida;
    private String destino;
    private String fecha;
    private String hora;
    private int plazas;
    private String telefono;

    public SyncReservaRequest(String nombre, String recogida, String destino,
                              String fecha, String hora, int plazas, String telefono) {
        this.nombre = nombre;
        this.recogida = recogida;
        this.destino = destino;
        this.fecha = fecha;
        this.hora = hora;
        this.plazas = plazas;
        this.telefono = telefono;
    }

    public String getNombre()   { return nombre; }
    public String getRecogida() { return recogida; }
    public String getDestino()  { return destino; }
    public String getFecha()    { return fecha; }
    public String getHora()     { return hora; }
    public int    getPlazas()   { return plazas; }
    public String getTelefono() { return telefono; }
}
