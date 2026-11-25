package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class QuienesSomosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quienes_somos);
        Button btnContacto = findViewById(R.id.btnContacto);
        btnContacto.setOnClickListener(v -> mostrarDialogoContacto());
        Button btnEquipo = findViewById(R.id.btnEquipo);
        btnEquipo.setOnClickListener(v -> mostrarDialogoEquipo());
        Button btnFeedback = findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(v -> mostrarDialogoFeedback());
        TextView tvLogo = findViewById(R.id.tvLogoHeader);
        tvLogo.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
    }

    private void mostrarDialogoContacto() {
        new AlertDialog.Builder(this)
            .setTitle("Contacto")
            .setMessage("Correo: contacto@mobii.com\nTeléfono: +34 123 456 789\n\nRedes Sociales:\n• Twitter: @MOBII_app\n• Facebook: /MOBIIapp\n• LinkedIn: /company/mobii")
            .setPositiveButton("Cerrar", null)
            .show();
    }

    private void mostrarDialogoEquipo() {
        new AlertDialog.Builder(this)
            .setTitle("Nuestro Equipo")
            .setMessage("👤 Jorge Ferrer Gil\n\nDesarrollador del proyecto MOBII\nDAM 2 — PMDM\n2025-2026")
            .setPositiveButton("Cerrar", null)
            .show();
    }

    private void mostrarDialogoFeedback() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);
        EditText etFeedback = dialogView.findViewById(R.id.etFeedbackTexto);
        TextView tvContador = dialogView.findViewById(R.id.tvContadorCaracteres);
        etFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len = s.length();
                tvContador.setText(len + "/300");
                tvContador.setTextColor(len > 280 ? Color.RED : Color.parseColor("#90A4AE"));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Enviar", null)
            .setNegativeButton("Cancelar", null)
            .create();
        dialog.setOnShowListener(d -> {
            Button btnEnviar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnEnviar.setOnClickListener(v -> {
                String mensaje = etFeedback.getText().toString().trim();
                if (mensaje.isEmpty()) {
                    etFeedback.setError("Escribe un mensaje");
                    return;
                }
                getSharedPreferences("feedback_prefs", MODE_PRIVATE).edit().putString("ultimo_feedback", mensaje).apply();
                dialog.dismiss();
                mostrarConfirmacionFeedback();
            });
        });
        dialog.show();
    }

    private void mostrarConfirmacionFeedback() {
        new AlertDialog.Builder(this)
            .setTitle("¡Gracias por tu feedback!")
            .setMessage("Tu opinión nos ayuda a mejorar MOBII para todos. ¡Valoramos mucho tu tiempo!")
            .setPositiveButton("Cerrar", null)
            .show();
    }
}
