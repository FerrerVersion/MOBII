package com.jorgeferrergil.mobii.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.model.Actividad;
import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    private List<Actividad> lista;

    public ActividadAdapter(List<Actividad> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad a = lista.get(position);
        holder.tvNombre.setText(a.getNombre());
        holder.tvCategoria.setText(a.getCategoriaLabel());
        holder.tvDireccion.setText("📍 " + a.getDireccion());
        if (!a.getHorario().isEmpty()) {
            holder.tvHorario.setVisibility(View.VISIBLE);
            holder.tvHorario.setText("🕐 " + a.getHorario());
        } else {
            holder.tvHorario.setVisibility(View.GONE);
        }
        if (!a.getTelefono().isEmpty()) {
            holder.tvTelefono.setVisibility(View.VISIBLE);
            holder.tvTelefono.setText("📞 " + a.getTelefono());
        } else {
            holder.tvTelefono.setVisibility(View.GONE);
        }
        holder.btnComoLlegar.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + a.getLat() + "," + a.getLng());
            v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvDireccion;
        TextView tvHorario;
        TextView tvTelefono;
        Button btnComoLlegar;

        public ActividadViewHolder(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNombreActividad);
            tvCategoria = v.findViewById(R.id.tvCategoriaActividad);
            tvDireccion = v.findViewById(R.id.tvDireccionActividad);
            tvHorario = v.findViewById(R.id.tvHorarioActividad);
            tvTelefono = v.findViewById(R.id.tvTelefonoActividad);
            btnComoLlegar = v.findViewById(R.id.btnComoLlegarActividad);
        }
    }
}
