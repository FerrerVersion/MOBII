package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jorgeferrergil.mobii.fragment.ActividadesFragment;
import com.jorgeferrergil.mobii.fragment.CentrosFragment;
import com.jorgeferrergil.mobii.fragment.InicioFragment;
import com.jorgeferrergil.mobii.fragment.ParkingFragment;
import com.jorgeferrergil.mobii.fragment.TaxisFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar tema guardado antes de inflar la vista
        SharedPreferences settings = getSharedPreferences("mobii_settings", MODE_PRIVATE);
        boolean darkMode = settings.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Redirigir al login si no hay sesión activa
        SharedPreferences prefs = getSharedPreferences("mobii_session", MODE_PRIVATE);
        if (!prefs.contains("token")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Top bar: nombre del usuario
        String nombre = prefs.getString("nombre", "Usuario");
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(nombre);

        // Logo → inicio
        ImageView imgLogo = findViewById(R.id.imgLogo);
        imgLogo.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_inicio);
            mostrarFragment(new InicioFragment());
        });

        // Ajustes → SettingsActivity
        ImageView btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                mostrarFragment(new InicioFragment());
            } else if (id == R.id.nav_parking) {
                mostrarFragment(new ParkingFragment());
            } else if (id == R.id.nav_actividades) {
                mostrarFragment(new ActividadesFragment());
            } else if (id == R.id.nav_taxis) {
                mostrarFragment(new TaxisFragment());
            } else if (id == R.id.nav_centros) {
                mostrarFragment(new CentrosFragment());
            }
            return true;
        });

        // Fragment inicial
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_inicio);
            mostrarFragment(new InicioFragment());
        }
    }

    private void mostrarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar nombre si vuelve de PerfilActivity
        SharedPreferences prefs = getSharedPreferences("mobii_session", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "Usuario");
        TextView tvUserName = findViewById(R.id.tvUserName);
        if (tvUserName != null) tvUserName.setText(nombre);
    }
}
