package com.jorgeferrergil.mobii.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import com.jorgeferrergil.mobii.auth.AuthRetrofitClient;
import com.jorgeferrergil.mobii.auth.SyncIdResponse;
import com.jorgeferrergil.mobii.auth.SyncReservaRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.j256.ormlite.stmt.QueryBuilder;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.adapter.ReservaAdapter;
import com.jorgeferrergil.mobii.db.DatabaseHelper;
import com.jorgeferrergil.mobii.model.Reserva;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaxisFragment extends Fragment {

    private LinearLayout sectionMenu;
    private ScrollView sectionForm;
    private LinearLayout sectionMisCitas;
    private EditText etNombre, etCalleRecogida, etNumRecogida, etCpRecogida;
    private EditText etCalleDestino, etNumDestino, etCpDestino;
    private EditText etFecha, etHora, etTelefono;
    private Spinner spinnerPlazas;
    private RecyclerView recyclerCitas;
    private ReservaAdapter reservaAdapter;
    private final List<Reserva> listaReservas = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private String userId;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_taxis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences("mobii_session", 0);
        userId = prefs.getString("email", "");
        token  = prefs.getString("token", "");

        dbHelper = new DatabaseHelper(requireContext());

        sectionMenu = view.findViewById(R.id.sectionMenuTaxi);
        sectionForm = view.findViewById(R.id.sectionFormTaxi);
        sectionMisCitas = view.findViewById(R.id.sectionMisCitas);

        view.findViewById(R.id.btnReservarTaxi).setOnClickListener(v -> mostrarForm());
        view.findViewById(R.id.btnMisCitas).setOnClickListener(v -> mostrarMisCitas());

        etNombre = view.findViewById(R.id.etNombreReserva);
        etCalleRecogida = view.findViewById(R.id.etCalleRecogida);
        etNumRecogida = view.findViewById(R.id.etNumRecogida);
        etCpRecogida = view.findViewById(R.id.etCpRecogida);
        etCalleDestino = view.findViewById(R.id.etCalleDestino);
        etNumDestino = view.findViewById(R.id.etNumDestino);
        etCpDestino = view.findViewById(R.id.etCpDestino);
        etFecha = view.findViewById(R.id.etFecha);
        etHora = view.findViewById(R.id.etHora);
        etTelefono = view.findViewById(R.id.etTelefono);
        spinnerPlazas = view.findViewById(R.id.spinnerPlazas);

        etFecha.setFocusable(false);
        etFecha.setClickable(true);
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        etHora.setFocusable(false);
        etHora.setClickable(true);
        etHora.setOnClickListener(v -> mostrarTimePicker());

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.num_plazas, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlazas.setAdapter(spinnerAdapter);

        view.findViewById(R.id.btnEnviarReserva).setOnClickListener(v -> enviarReserva());
        view.findViewById(R.id.btnVolverForm).setOnClickListener(v -> mostrarMenu());
        view.findViewById(R.id.btnVolverCitas).setOnClickListener(v -> mostrarMenu());

        reservaAdapter = new ReservaAdapter(listaReservas, this::confirmarCancelar);
        recyclerCitas = view.findViewById(R.id.recyclerCitas);
        recyclerCitas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCitas.setAdapter(reservaAdapter);
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(),
                (v, y, m, d) -> etFecha.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(cal.getTimeInMillis());
        picker.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (v, h, m) ->
                etHora.setText(String.format("%02d:%02d", h, m)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
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
        String cDest = etCalleDestino.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String tel = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || cRecog.isEmpty() || cDest.isEmpty()
                || fecha.isEmpty() || hora.isEmpty() || tel.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int plazas = 1;
        try { plazas = Integer.parseInt(spinnerPlazas.getSelectedItem().toString()); }
        catch (Exception ignored) {}

        Reserva reserva = new Reserva(userId, nombre,
                cRecog, etNumRecogida.getText().toString().trim(),
                etCpRecogida.getText().toString().trim(),
                cDest, etNumDestino.getText().toString().trim(),
                etCpDestino.getText().toString().trim(),
                fecha, hora, tel, plazas);

        new Thread(() -> {
            try {
                dbHelper.getReservaDao().create(reserva);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "¡Reserva creada!", Toast.LENGTH_SHORT).show();
                    limpiarForm();
                    mostrarMenu();
                });
                // Sync al backend; si falla, syncId queda en -1 (no sincronizado)
                if (!token.isEmpty()) {
                    SyncReservaRequest req = new SyncReservaRequest(
                            reserva.getNombre(),
                            reserva.getRecogidaCompleta(),
                            reserva.getDestinoCompleto(),
                            reserva.getFecha(),
                            reserva.getHora(),
                            reserva.getPlazas(),
                            reserva.getTelefono());
                    AuthRetrofitClient.getInstance().getService()
                            .syncAddReserva("Bearer " + token, req)
                            .enqueue(new Callback<SyncIdResponse>() {
                                @Override
                                public void onResponse(Call<SyncIdResponse> c, Response<SyncIdResponse> r) {
                                    if (r.isSuccessful() && r.body() != null) {
                                        reserva.setSyncId(r.body().getId());
                                        new Thread(() -> {
                                            try { dbHelper.getReservaDao().update(reserva); }
                                            catch (SQLException ignored) {}
                                        }).start();
                                    }
                                }
                                @Override public void onFailure(Call<SyncIdResponse> c, Throwable t) {}
                            });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al guardar la reserva",
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void cargarReservas() {
        new Thread(() -> {
            try {
                QueryBuilder<Reserva, Integer> qb = dbHelper.getReservaDao().queryBuilder();
                qb.where().eq("userId", userId);
                List<Reserva> locales = qb.query();
                requireActivity().runOnUiThread(() -> {
                    listaReservas.clear();
                    listaReservas.addAll(locales);
                    reservaAdapter.notifyDataSetChanged();
                    View root = getView();
                    if (root != null) {
                        TextView tvSinCitas = root.findViewById(R.id.tvSinCitas);
                        if (tvSinCitas != null)
                            tvSinCitas.setVisibility(locales.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void confirmarCancelar(Reserva reserva) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar cita")
                .setMessage("¿Seguro que quieres cancelar la reserva \"" + reserva.getNombre() + "\"?")
                .setPositiveButton("Cancelar cita", (d, w) -> new Thread(() -> {
                    try {
                        dbHelper.getReservaDao().delete(reserva);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Cita cancelada", Toast.LENGTH_SHORT).show();
                            cargarReservas();
                        });
                        // Sync eliminación al backend
                        if (!token.isEmpty() && reserva.getSyncId() > 0) {
                            AuthRetrofitClient.getInstance().getService()
                                    .syncDeleteReserva("Bearer " + token, reserva.getSyncId())
                                    .enqueue(new Callback<Void>() {
                                        @Override public void onResponse(Call<Void> c, Response<Void> r) {}
                                        @Override public void onFailure(Call<Void> c, Throwable t) {}
                                    });
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).start())
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) dbHelper.close();
    }
}
