package com.jorgeferrergil.mobii.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "parking_spots")
public class ParkingSpot {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(unique = true)
    private String recordId;

    @DatabaseField
    private String nombreVia;

    @DatabaseField
    private String numero;

    @DatabaseField
    private double lat;

    @DatabaseField
    private double lng;

    public ParkingSpot() {}

    public ParkingSpot(String recordId, String nombreVia, String numero, double lat, double lng) {
        this.recordId = recordId;
        this.nombreVia = nombreVia;
        this.numero = numero;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() { return id; }
    public String getRecordId() { return recordId; }
    public String getNombreVia() { return nombreVia != null ? nombreVia : "Aparcamiento adaptado"; }
    public String getNumero() { return numero != null ? numero : ""; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

    public void setId(int id) { this.id = id; }
    public void setRecordId(String r) { this.recordId = r; }
    public void setNombreVia(String n) { this.nombreVia = n; }
    public void setNumero(String n) { this.numero = n; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }

    public String getDireccionCompleta() {
        String via = getNombreVia();
        String num = getNumero();
        return num.isEmpty() ? via : via + ", " + num;
    }
}
