package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_SETTINGS = "mobii_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar tema antes de inflar
        SharedPreferences settings = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean darkMode = settings.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Switch modo oscuro
        SwitchCompat switchDark = findViewById(R.id.switchDarkMode);
        switchDark.setChecked(darkMode);
        switchDark.setOnCheckedChangeListener((btn, checked) -> {
            settings.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Switch recordatorios de citas
        SwitchCompat switchNotif = findViewById(R.id.switchNotificaciones);
        switchNotif.setChecked(settings.getBoolean("notificaciones", true));
        switchNotif.setOnCheckedChangeListener((btn, checked) ->
                settings.edit().putBoolean("notificaciones", checked).apply());

        // Switch historial de rutas
        SwitchCompat switchHistorial = findViewById(R.id.switchHistorial);
        switchHistorial.setChecked(settings.getBoolean("historial", true));
        switchHistorial.setOnCheckedChangeListener((btn, checked) ->
                settings.edit().putBoolean("historial", checked).apply());

        // Fila Mi Perfil
        LinearLayout rowPerfil = findViewById(R.id.rowMiPerfil);
        rowPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));
    }
}
