package com.jorgeferrergil.mobii.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AuthApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("api/auth/perfil")
    Call<UsuarioAuth> getPerfil(@Header("Authorization") String token);

    @PUT("api/auth/perfil")
    Call<UsuarioAuth> actualizarPerfil(
            @Header("Authorization") String token,
            @Body PerfilUpdateRequest request);

    @DELETE("api/auth/cuenta")
    Call<Void> eliminarCuenta(@Header("Authorization") String token);

    // sync de favoritos
    @POST("api/sync/favoritos")
    Call<SyncIdResponse> syncAddFavorito(
            @Header("Authorization") String token,
            @Body SyncFavoritoRequest request);

    @DELETE("api/sync/favoritos/{recordId}")
    Call<Void> syncDeleteFavorito(
            @Header("Authorization") String token,
            @Path("recordId") String recordId);

    // sync de reservas de taxi
    @POST("api/sync/reservas")
    Call<SyncIdResponse> syncAddReserva(
            @Header("Authorization") String token,
            @Body SyncReservaRequest request);

    @DELETE("api/sync/reservas/{id}")
    Call<Void> syncDeleteReserva(
            @Header("Authorization") String token,
            @Path("id") int id);
}
