package com.jorgeferrergil.mobii.model;

import com.google.gson.annotations.SerializedName;

public class ParkingRecord {

    @SerializedName("recordid")
    private String recordId;

    @SerializedName("fields")
    private ParkingFields fields;

    @SerializedName("geometry")
    private ParkingGeometry geometry;

    public String getRecordId() { return recordId; }
    public ParkingFields getFields() { return fields; }
    public ParkingGeometry getGeometry() { return geometry; }

    public static class ParkingFields {
        @SerializedName("nom_via")
        private String nomVia;

        @SerializedName("numero")
        private String numero;

        @SerializedName("geo_point_2d")
        private double[] geoPoint;

        public String getNomVia() { return nomVia; }
        public String getNumero() { return numero; }
        public double[] getGeoPoint() { return geoPoint; }
    }

    public static class ParkingGeometry {
        @SerializedName("type")
        private String type;

        @SerializedName("coordinates")
        private double[] coordinates;

        public double getLat() {
            return (coordinates != null && coordinates.length >= 2) ? coordinates[1] : 0;
        }

        public double getLng() {
            return (coordinates != null && coordinates.length >= 2) ? coordinates[0] : 0;
        }
    }

    public ParkingSpot toParkingSpot() {
        double lat = 0, lng = 0;
        if (geometry != null) {
            lat = geometry.getLat();
            lng = geometry.getLng();
        } else if (fields != null && fields.getGeoPoint() != null && fields.getGeoPoint().length >= 2) {
            lat = fields.getGeoPoint()[0];
            lng = fields.getGeoPoint()[1];
        }
        String nomVia = (fields != null && fields.getNomVia() != null) ? fields.getNomVia() : "Aparcamiento adaptado";
        String numero = (fields != null && fields.getNumero() != null) ? fields.getNumero() : "";
        return new ParkingSpot(recordId, nomVia, numero, lat, lng);
    }
}
