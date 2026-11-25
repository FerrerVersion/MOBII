package com.jorgeferrergil.mobii.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.model.ParkingSpot;
import java.util.List;
import java.util.Set;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<ParkingSpot> lista;
    private Set<String> favoritosIds;
    private OnEstrellaClikListener listener;

    public interface OnEstrellaClikListener {
        void onEstrellaClik(ParkingSpot spot, boolean esFavorito);
    }

    public ParkingAdapter(List<ParkingSpot> lista, Set<String> favoritosIds, OnEstrellaClikListener listener) {
        this.lista = lista;
        this.favoritosIds = favoritosIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingSpot spot = lista.get(position);
        holder.tvDireccion.setText(spot.getDireccionCompleta());
        boolean esFav = favoritosIds.contains(spot.getRecordId());
        holder.tvEstrella.setText(esFav ? "⭐" : "☆");
        holder.tvEstrella.setOnClickListener(v -> {
            boolean nuevoEstado = !favoritosIds.contains(spot.getRecordId());
            if (nuevoEstado) {
                favoritosIds.add(spot.getRecordId());
            } else {
                favoritosIds.remove(spot.getRecordId());
            }
            holder.tvEstrella.setText(nuevoEstado ? "⭐" : "☆");
            if (listener != null) listener.onEstrellaClik(spot, nuevoEstado);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvDireccion;
        TextView tvEstrella;

        public ParkingViewHolder(@NonNull View v) {
            super(v);
            tvDireccion = v.findViewById(R.id.tvDireccion);
            tvEstrella = v.findViewById(R.id.tvEstrella);
        }
    }
}
