package com.jorgeferrergil.mobii.api;

import com.jorgeferrergil.mobii.model.ParkingApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("search/")
    Call<ParkingApiResponse> getParkings(
        @Query("dataset") String dataset,
        @Query("rows") int rows,
        @Query("format") String format
    );
}
