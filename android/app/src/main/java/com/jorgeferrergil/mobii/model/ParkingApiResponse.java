package com.jorgeferrergil.mobii.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingApiResponse {

    @SerializedName("nhits")
    private int nhits;

    @SerializedName("records")
    private List<ParkingRecord> records;

    public int getNhits() { return nhits; }
    public List<ParkingRecord> getRecords() { return records; }
}
