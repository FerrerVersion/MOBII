package com.jorgeferrergil.mobii.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.model.Reserva;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private List<Reserva> lista;
    private OnCancelarClickListener listener;

    public interface OnCancelarClickListener {
        void onCancelarClick(Reserva reserva);
    }

    public ReservaAdapter(List<Reserva> lista, OnCancelarClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva r = lista.get(position);
        holder.tvNombre.setText("Reserva: " + r.getNombre());
        holder.tvFechaHora.setText(r.getFecha() + " a las " + r.getHora());
        holder.tvRecogida.setText("Recogida: " + r.getRecogidaCompleta());
        holder.tvDestino.setText("Destino: " + r.getDestinoCompleto());
        holder.tvPlazas.setText("Plazas: " + r.getPlazas());
        holder.tvTelefono.setText("Tel: " + r.getTelefono());
        holder.btnCancelar.setOnClickListener(v -> {
            if (listener != null) listener.onCancelarClick(r);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvFechaHora;
        TextView tvRecogida;
        TextView tvDestino;
        TextView tvPlazas;
        TextView tvTelefono;
        Button btnCancelar;

        public ReservaViewHolder(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvFechaHora = v.findViewById(R.id.tvFechaHora);
            tvRecogida = v.findViewById(R.id.tvRecogida);
            tvDestino = v.findViewById(R.id.tvDestino);
            tvPlazas = v.findViewById(R.id.tvPlazas);
            tvTelefono = v.findViewById(R.id.tvTelefono);
            btnCancelar = v.findViewById(R.id.btnCancelar);
        }
    }
}
