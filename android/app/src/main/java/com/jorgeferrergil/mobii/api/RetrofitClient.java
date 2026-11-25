package com.jorgeferrergil.mobii.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://valencia.opendatasoft.com/api/records/1.0/";
    private static ApiService instance;

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
        }
        return instance;
    }
}
