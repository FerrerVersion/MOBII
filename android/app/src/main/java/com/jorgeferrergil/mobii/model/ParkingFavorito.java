package com.jorgeferrergil.mobii.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "parking_favoritos")
public class ParkingFavorito {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String userId;      // email del usuario

    @DatabaseField
    private String recordId;    // id del parking spot

    @DatabaseField
    private String direccion;

    public ParkingFavorito() {}

    public ParkingFavorito(String userId, String recordId, String direccion) {
        this.userId = userId;
        this.recordId = recordId;
        this.direccion = direccion;
    }

    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getRecordId() { return recordId; }
    public String getDireccion() { return direccion; }

    public void setId(int id) { this.id = id; }
    public void setUserId(String u) { this.userId = u; }
    public void setRecordId(String r) { this.recordId = r; }
    public void setDireccion(String d) { this.direccion = d; }
}
