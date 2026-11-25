package com.jorgeferrergil.mobii.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "reservas")
public class Reserva {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String userId;          // email del usuario propietario

    @DatabaseField
    private String nombre;

    @DatabaseField
    private String calleRecogida;

    @DatabaseField
    private String numRecogida;

    @DatabaseField
    private String cpRecogida;

    @DatabaseField
    private String calleDestino;

    @DatabaseField
    private String numDestino;

    @DatabaseField
    private String cpDestino;

    @DatabaseField
    private String fecha;

    @DatabaseField
    private String hora;

    @DatabaseField
    private String telefono;

    @DatabaseField
    private int plazas;

    @DatabaseField(defaultValue = "-1")
    private int syncId = -1;   // id en el backend; -1 = no sincronizado

    public Reserva() {}

    public Reserva(String userId, String nombre,
                   String calleRecogida, String numRecogida, String cpRecogida,
                   String calleDestino, String numDestino, String cpDestino,
                   String fecha, String hora, String telefono, int plazas) {
        this.userId = userId;
        this.nombre = nombre;
        this.calleRecogida = calleRecogida;
        this.numRecogida = numRecogida;
        this.cpRecogida = cpRecogida;
        this.calleDestino = calleDestino;
        this.numDestino = numDestino;
        this.cpDestino = cpDestino;
        this.fecha = fecha;
        this.hora = hora;
        this.telefono = telefono;
        this.plazas = plazas;
    }

    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getNombre() { return nombre; }
    public String getCalleRecogida() { return calleRecogida; }
    public String getNumRecogida() { return numRecogida; }
    public String getCpRecogida() { return cpRecogida; }
    public String getCalleDestino() { return calleDestino; }
    public String getNumDestino() { return numDestino; }
    public String getCpDestino() { return cpDestino; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getTelefono() { return telefono; }
    public int getPlazas() { return plazas; }

    public void setId(int id) { this.id = id; }
    public void setUserId(String u) { this.userId = u; }
    public void setNombre(String n) { this.nombre = n; }
    public void setCalleRecogida(String c) { this.calleRecogida = c; }
    public void setNumRecogida(String n) { this.numRecogida = n; }
    public void setCpRecogida(String cp) { this.cpRecogida = cp; }
    public void setCalleDestino(String c) { this.calleDestino = c; }
    public void setNumDestino(String n) { this.numDestino = n; }
    public void setCpDestino(String cp) { this.cpDestino = cp; }
    public void setFecha(String f) { this.fecha = f; }
    public void setHora(String h) { this.hora = h; }
    public void setTelefono(String t) { this.telefono = t; }
    public void setPlazas(int p) { this.plazas = p; }
    public int getSyncId() { return syncId; }
    public void setSyncId(int s) { this.syncId = s; }

    public String getRecogidaCompleta() {
        String n = numRecogida != null && !numRecogida.isEmpty() ? ", " + numRecogida : "";
        String cp = cpRecogida != null && !cpRecogida.isEmpty() ? " CP " + cpRecogida : "";
        return calleRecogida + n + cp;
    }

    public String getDestinoCompleto() {
        String n = numDestino != null && !numDestino.isEmpty() ? ", " + numDestino : "";
        String cp = cpDestino != null && !cpDestino.isEmpty() ? " CP " + cpDestino : "";
        return calleDestino + n + cp;
    }
}
