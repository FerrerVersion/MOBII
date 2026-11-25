package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jorgeferrergil.mobii.auth.AuthResponse;
import com.jorgeferrergil.mobii.auth.AuthRetrofitClient;
import com.jorgeferrergil.mobii.auth.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword;
    private TextView tvError;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre    = findViewById(R.id.etNombre);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        tvError     = findViewById(R.id.tvError);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> intentarRegistro());

        TextView tvIrLogin = findViewById(R.id.tvIrLogin);
        tvIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void intentarRegistro() {
        String nombre   = etNombre.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrarError("Rellena todos los campos");
            return;
        }
        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Creando cuenta...");
        tvError.setVisibility(View.GONE);

        RegisterRequest request = new RegisterRequest(nombre, email, password);
        AuthRetrofitClient.getInstance().getService().register(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Crear cuenta");
                        if (response.isSuccessful() && response.body() != null) {
                            guardarSesion(response.body());
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String msg = response.code() == 409
                                    ? "El email ya está registrado"
                                    : "Error al crear la cuenta";
                            mostrarError(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Crear cuenta");
                        mostrarError("No se puede conectar al servidor");
                    }
                });
    }

    private void guardarSesion(AuthResponse auth) {
        SharedPreferences.Editor editor =
                getSharedPreferences("mobii_session", MODE_PRIVATE).edit();
        editor.putString("token", auth.getToken());
        editor.putString("nombre", auth.getUsuario().getNombre());
        editor.putString("email", auth.getUsuario().getEmail());
        editor.putString("rol", auth.getUsuario().getRol());
        editor.apply();
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
