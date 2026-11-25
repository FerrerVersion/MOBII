package com.jorgeferrergil.mobii;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.j256.ormlite.dao.Dao;
import com.jorgeferrergil.mobii.adapter.ReservaAdapter;
import com.jorgeferrergil.mobii.db.DatabaseHelper;
import com.jorgeferrergil.mobii.model.Reserva;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaxisActivity extends AppCompatActivity {

    private LinearLayout sectionMenu;
    private ScrollView sectionForm;
    private LinearLayout sectionMisCitas;
    private EditText etNombre;
    private EditText etCalleRecogida;
    private EditText etNumRecogida;
    private EditText etCpRecogida;
    private EditText etCalleDestino;
    private EditText etNumDestino;
    private EditText etCpDestino;
    private EditText etFecha;
    private EditText etHora;
    private EditText etTelefono;
    private Spinner spinnerPlazas;
    private RecyclerView recyclerCitas;
    private ReservaAdapter reservaAdapter;
    private List<Reserva> listaReservas;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxis);

        dbHelper = new DatabaseHelper(this);

        sectionMenu = findViewById(R.id.sectionMenuTaxi);
        sectionForm = findViewById(R.id.sectionFormTaxi);
        sectionMisCitas = findViewById(R.id.sectionMisCitas);

        Button btnReservar = findViewById(R.id.btnReservarTaxi);
        Button btnMisCitas = findViewById(R.id.btnMisCitas);
        btnReservar.setOnClickListener(v -> mostrarForm());
        btnMisCitas.setOnClickListener(v -> mostrarMisCitas());

        etNombre = findViewById(R.id.etNombreReserva);
        etCalleRecogida = findViewById(R.id.etCalleRecogida);
        etNumRecogida = findViewById(R.id.etNumRecogida);
        etCpRecogida = findViewById(R.id.etCpRecogida);
        etCalleDestino = findViewById(R.id.etCalleDestino);
        etNumDestino = findViewById(R.id.etNumDestino);
        etCpDestino = findViewById(R.id.etCpDestino);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etTelefono = findViewById(R.id.etTelefono);
        spinnerPlazas = findViewById(R.id.spinnerPlazas);

        etFecha.setFocusable(false);
        etFecha.setClickable(true);
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        etHora.setFocusable(false);
        etHora.setClickable(true);
        etHora.setOnClickListener(v -> mostrarTimePicker());

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.num_plazas, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlazas.setAdapter(spinnerAdapter);

        Button btnEnviar = findViewById(R.id.btnEnviarReserva);
        btnEnviar.setOnClickListener(v -> enviarReserva());

        Button btnVolverForm = findViewById(R.id.btnVolverForm);
        btnVolverForm.setOnClickListener(v -> mostrarMenu());

        listaReservas = new ArrayList<>();
        reservaAdapter = new ReservaAdapter(listaReservas, reserva -> confirmarCancelar(reserva));
        recyclerCitas = findViewById(R.id.recyclerCitas);
        recyclerCitas.setLayoutManager(new LinearLayoutManager(this));
        recyclerCitas.setAdapter(reservaAdapter);

        Button btnVolverCitas = findViewById(R.id.btnVolverCitas);
        btnVolverCitas.setOnClickListener(v -> mostrarMenu());

        TextView tvLogo = findViewById(R.id.tvLogoHeader);
        tvLogo.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        // Rellenar destino automáticamente si viene de Mapa
        String destinoCalle = getIntent().getStringExtra("destino_calle");
        if (destinoCalle != null && !destinoCalle.isEmpty()) {
            etCalleDestino.setText(destinoCalle);
            mostrarForm();
        }
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            String fecha = String.format("%02d/%02d/%04d", d, m + 1, y);
            etFecha.setText(fecha);
        }, anio, mes, dia);
        picker.getDatePicker().setMinDate(cal.getTimeInMillis());
        picker.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        int minuto = cal.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, h, m) -> {
            String tiempo = String.format("%02d:%02d", h, m);
            etHora.setText(tiempo);
        }, hora, minuto, true).show();
    }

    private void mostrarMenu() {
        sectionMenu.setVisibility(View.VISIBLE);
        sectionForm.setVisibility(View.GONE);
        sectionMisCitas.setVisibility(View.GONE);
    }

    private void mostrarForm() {
        sectionMenu.setVisibility(View.GONE);
        sectionForm.setVisibility(View.VISIBLE);
        sectionMisCitas.setVisibility(View.GONE);
    }

    private void mostrarMisCitas() {
        sectionMenu.setVisibility(View.GONE);
        sectionForm.setVisibility(View.GONE);
        sectionMisCitas.setVisibility(View.VISIBLE);
        cargarReservas();
    }

    private void enviarReserva() {
        String nombre = etNombre.getText().toString().trim();
        String cRecog = etCalleRecogida.getText().toString().trim();
        String nRecog = etNumRecogida.getText().toString().trim();
        String cpRecog = etCpRecogida.getText().toString().trim();
        String cDest = etCalleDestino.getText().toString().trim();
        String nDest = etNumDestino.getText().toString().trim();
        String cpDest = etCpDestino.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String tel = etTelefono.getText().toString().trim();

        int plazas = 1;
        try {
            plazas = Integer.parseInt(spinnerPlazas.getSelectedItem().toString());
        } catch (Exception e) {
            plazas = 1; // Seguridad si el spinner tiene texto no numérico
        }

        if (nombre.isEmpty() || cRecog.isEmpty() || cDest.isEmpty() || fecha.isEmpty() || hora.isEmpty() || tel.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("mobii_session", MODE_PRIVATE);
        String userId = prefs.getString("email", "");
        Reserva reserva = new Reserva(userId, nombre, cRecog, nRecog, cpRecog, cDest, nDest, cpDest, fecha, hora, tel, plazas);
        new Thread(() -> {
            try {
                dbHelper.getReservaDao().create(reserva);
                runOnUiThread(() -> {
                    Toast.makeText(this, "¡Reserva creada!", Toast.LENGTH_SHORT).show();
                    limpiarForm();
                    mostrarMenu();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void cargarReservas() {
        new Thread(() -> {
            try {
                List<Reserva> locales = dbHelper.getReservaDao().queryForAll();
                runOnUiThread(() -> {
                    listaReservas.clear();
                    listaReservas.addAll(locales);
                    reservaAdapter.notifyDataSetChanged();
                    TextView tvSinCitas = findViewById(R.id.tvSinCitas);
                    if (tvSinCitas != null) {
                        tvSinCitas.setVisibility(locales.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void confirmarCancelar(Reserva reserva) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("¿Seguro que quieres cancelar la reserva \"" + reserva.getNombre() + "\"?")
                .setPositiveButton("Cancelar cita", (d, w) -> {
                    new Thread(() -> {
                        try {
                            dbHelper.getReservaDao().delete(reserva);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                                cargarReservas();
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }).start();
                })
                .setNegativeButton("Volver", null)
                .show();
    }

    private void limpiarForm() {
        etNombre.setText("");
        etCalleRecogida.setText("");
        etNumRecogida.setText("");
        etCpRecogida.setText("");
        etCalleDestino.setText("");
        etNumDestino.setText("");
        etCpDestino.setText("");
        etFecha.setText("");
        etHora.setText("");
        etTelefono.setText("");
        spinnerPlazas.setSelection(0);
    }
}