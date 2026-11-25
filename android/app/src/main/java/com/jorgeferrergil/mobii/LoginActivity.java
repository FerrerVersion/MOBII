package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jorgeferrergil.mobii.auth.AuthResponse;
import com.jorgeferrergil.mobii.auth.AuthRetrofitClient;
import com.jorgeferrergil.mobii.auth.LoginRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay sesión activa, ir directo al menú
        SharedPreferences prefs = getSharedPreferences("mobii_session", MODE_PRIVATE);
        if (prefs.contains("token")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvError    = findViewById(R.id.tvError);
        btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> intentarLogin());

        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void intentarLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Rellena todos los campos");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Accediendo...");
        tvError.setVisibility(View.GONE);

        LoginRequest request = new LoginRequest(email, password);
        AuthRetrofitClient.getInstance().getService().login(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar sesión");
                        if (response.isSuccessful() && response.body() != null) {
                            guardarSesion(response.body());
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            mostrarError("Credenciales incorrectas");
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar sesión");
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
