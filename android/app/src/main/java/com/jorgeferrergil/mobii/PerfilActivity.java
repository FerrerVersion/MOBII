package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.jorgeferrergil.mobii.auth.AuthRetrofitClient;
import com.jorgeferrergil.mobii.auth.PerfilUpdateRequest;
import com.jorgeferrergil.mobii.auth.UsuarioAuth;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvFecha, tvEmailDetalle, tvMensaje;
    private EditText etDiscapacidad;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        prefs = getSharedPreferences("mobii_session", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            irAlLogin();
            return;
        }

        tvNombre       = findViewById(R.id.tvNombrePerfil);
        tvEmail        = findViewById(R.id.tvEmailPerfil);
        tvFecha        = findViewById(R.id.tvFechaRegistro);
        tvEmailDetalle = findViewById(R.id.tvEmailDetalle);
        tvMensaje      = findViewById(R.id.tvDiscapacidadMensaje);
        etDiscapacidad = findViewById(R.id.etDiscapacidad);

        cargarPerfil(token);

        Button btnGuardar = findViewById(R.id.btnGuardarDiscapacidad);
        btnGuardar.setOnClickListener(v -> guardarDiscapacidad(token));

        LinearLayout btnCerrar = findViewById(R.id.btnCerrarSesion);
        btnCerrar.setOnClickListener(v -> confirmarCerrarSesion());

        LinearLayout btnEliminar = findViewById(R.id.btnEliminarCuenta);
        btnEliminar.setOnClickListener(v -> confirmarEliminarCuenta(token));
    }

    private void cargarPerfil(String token) {
        AuthRetrofitClient.getInstance().getService()
                .getPerfil("Bearer " + token)
                .enqueue(new Callback<UsuarioAuth>() {
                    @Override
                    public void onResponse(Call<UsuarioAuth> call, Response<UsuarioAuth> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarDatos(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<UsuarioAuth> call, Throwable t) {
                        // Muestra datos locales si no hay red
                        tvNombre.setText(prefs.getString("nombre", "—"));
                        tvEmail.setText(prefs.getString("email", "—"));
                        tvEmailDetalle.setText(prefs.getString("email", "—"));
                    }
                });
    }

    private void mostrarDatos(UsuarioAuth u) {
        tvNombre.setText(u.getNombre());
        tvEmail.setText(u.getEmail());
        tvEmailDetalle.setText(u.getEmail());

        String fecha = u.getCreatedAt() != null && u.getCreatedAt().length() >= 10
                ? u.getCreatedAt().substring(0, 10) : "—";
        tvFecha.setText(fecha);

        if (u.getDiscapacidad() != null && !u.getDiscapacidad().isEmpty()) {
            etDiscapacidad.setText(u.getDiscapacidad());
        }
    }

    private void guardarDiscapacidad(String token) {
        String nombre        = prefs.getString("nombre", "");
        String discapacidad  = etDiscapacidad.getText().toString().trim();

        tvMensaje.setVisibility(View.GONE);

        AuthRetrofitClient.getInstance().getService()
                .actualizarPerfil("Bearer " + token, new PerfilUpdateRequest(nombre, discapacidad))
                .enqueue(new Callback<UsuarioAuth>() {
                    @Override
                    public void onResponse(Call<UsuarioAuth> call, Response<UsuarioAuth> response) {
                        if (response.isSuccessful()) {
                            mostrarMensaje("✓ Guardado correctamente", "#2E7D32");
                        } else {
                            mostrarMensaje("Error al guardar", "#C0392B");
                        }
                    }
                    @Override
                    public void onFailure(Call<UsuarioAuth> call, Throwable t) {
                        mostrarMensaje("Sin conexión al servidor", "#C0392B");
                    }
                });
    }

    private void mostrarMensaje(String texto, String colorHex) {
        tvMensaje.setText(texto);
        tvMensaje.setTextColor(Color.parseColor(colorHex));
        tvMensaje.setVisibility(View.VISIBLE);
    }

    private void confirmarCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar tu sesión?")
                .setPositiveButton("Sí, salir", (d, w) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminarCuenta(String token) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Eliminar cuenta")
                .setMessage("Esta acción es irreversible. Se borrarán todos tus datos de MOBII.\n\n¿Deseas continuar?")
                .setPositiveButton("Eliminar definitivamente", (d, w) -> eliminarCuenta(token))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuenta(String token) {
        AuthRetrofitClient.getInstance().getService()
                .eliminarCuenta("Bearer " + token)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            prefs.edit().clear().apply();
                            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            mostrarMensaje("No se pudo eliminar la cuenta", "#C0392B");
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        mostrarMensaje("Sin conexión al servidor", "#C0392B");
                    }
                });
    }

    private void cerrarSesion() {
        prefs.edit().clear().apply();
        irAlLogin();
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
