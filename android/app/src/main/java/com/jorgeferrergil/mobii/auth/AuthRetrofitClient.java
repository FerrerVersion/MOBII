package com.jorgeferrergil.mobii.auth;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthRetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static AuthRetrofitClient instance;
    private final AuthApiService service;

    private AuthRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(AuthApiService.class);
    }

    public static synchronized AuthRetrofitClient getInstance() {
        if (instance == null) instance = new AuthRetrofitClient();
        return instance;
    }

    public AuthApiService getService() { return service; }
}
