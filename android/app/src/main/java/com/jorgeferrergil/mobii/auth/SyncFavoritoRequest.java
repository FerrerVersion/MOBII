package com.jorgeferrergil.mobii.auth;

public class SyncFavoritoRequest {
    private String recordId;
    private String direccion;

    public SyncFavoritoRequest(String recordId, String direccion) {
        this.recordId = recordId;
        this.direccion = direccion;
    }

    public String getRecordId() { return recordId; }
    public String getDireccion() { return direccion; }
}
