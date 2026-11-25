package com.jorgeferrergil.mobii.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.jorgeferrergil.mobii.R;

public class InicioFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("mobii_session", 0);
        String nombre = prefs.getString("nombre", "Usuario");

        TextView tvSaludo = view.findViewById(R.id.tvSaludo);
        tvSaludo.setText("¡Hola, " + nombre + "!");

        return view;
    }
}
