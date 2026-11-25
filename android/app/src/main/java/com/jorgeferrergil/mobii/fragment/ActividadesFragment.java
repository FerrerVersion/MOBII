package com.jorgeferrergil.mobii.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.adapter.ActividadAdapter;
import com.jorgeferrergil.mobii.model.Actividad;
import java.util.ArrayList;
import java.util.List;

public class ActividadesFragment extends Fragment {

    private ActividadAdapter adapter;
    private final List<Actividad> listaFiltrada = new ArrayList<>();
    private List<Actividad> listaTodas;
    private RecyclerView recyclerActividades;
    private WebView webViewMapa;
    private Button btnLista;
    private Button btnMapa;
    private boolean mostrandoMapa = false;
    private String categoriaActual = "Todas";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actividades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listaTodas = obtenerTodasLasActividades();
        adapter = new ActividadAdapter(listaFiltrada);

        recyclerActividades = view.findViewById(R.id.recyclerActividades);
        recyclerActividades.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerActividades.setAdapter(adapter);

        webViewMapa = view.findViewById(R.id.webViewActividadesMapa);
        WebSettings ws = webViewMapa.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webViewMapa.setWebViewClient(new WebViewClient());
        webViewMapa.addJavascriptInterface(new MapaInterface(), "Android");

        btnLista = view.findViewById(R.id.btnVistLista);
        btnMapa = view.findViewById(R.id.btnVistaMapa);
        btnLista.setOnClickListener(v -> mostrarVista(false));
        btnMapa.setOnClickListener(v -> mostrarVista(true));

        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        String[] categorias = {"Todas", "Cafés", "Heladerías", "Pubs", "Restaurantes",
                "Museos", "Baños", "Farmacias", "Supermercados", "Estancos"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categorias);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(spinnerAdapter);
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                categoriaActual = categorias[pos];
                filtrarPorCategoria();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filtrarPorCategoria();
        mostrarVista(false);
    }

    private void mostrarVista(boolean mapa) {
        mostrandoMapa = mapa;
        if (mapa) {
            recyclerActividades.setVisibility(View.GONE);
            webViewMapa.setVisibility(View.VISIBLE);
            btnMapa.setBackgroundColor(0xFF4A5CC8);
            btnLista.setBackgroundColor(0xFF7B8EF5);
            cargarMapaLeaflet();
        } else {
            recyclerActividades.setVisibility(View.VISIBLE);
            webViewMapa.setVisibility(View.GONE);
            btnLista.setBackgroundColor(0xFF4A5CC8);
            btnMapa.setBackgroundColor(0xFF7B8EF5);
        }
    }

    private void filtrarPorCategoria() {
        listaFiltrada.clear();
        for (Actividad a : listaTodas) {
            if ("Todas".equals(categoriaActual) || a.getCategoriaLabel().equals(categoriaActual)) {
                listaFiltrada.add(a);
            }
        }
        adapter.notifyDataSetChanged();
        if (mostrandoMapa) cargarMapaLeaflet();
    }

    private void cargarMapaLeaflet() {
        webViewMapa.loadDataWithBaseURL(null, generarHtmlMapa(listaFiltrada), "text/html", "UTF-8", null);
    }

    private String jsStr(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ");
    }

    private String generarHtmlMapa(List<Actividad> actividades) {
        StringBuilder markers = new StringBuilder("[");
        for (int i = 0; i < actividades.size(); i++) {
            Actividad a = actividades.get(i);
            if (i > 0) markers.append(",");
            markers.append("{")
                    .append("\"nombre\":\"").append(jsStr(a.getNombre())).append("\",")
                    .append("\"address\":\"").append(jsStr(a.getDireccion())).append("\",")
                    .append("\"categoria\":\"").append(jsStr(a.getCategoriaLabel())).append("\",")
                    .append("\"lat\":").append(a.getLat()).append(",")
                    .append("\"lng\":").append(a.getLng())
                    .append("}");
        }
        markers.append("]");
        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>body{margin:0;padding:0}#map{width:100%;height:100vh}"
                + ".popup-btn{display:block;width:100%;margin-top:6px;padding:8px 10px;border:none;"
                + "border-radius:6px;color:white;font-size:13px;cursor:pointer;background:#5B6EE1}</style>"
                + "</head><body><div id='map'></div><script>"
                + "var map=L.map('map').setView([39.4699,-0.3763],13);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',"
                + "{attribution:'&copy; OSM contributors'}).addTo(map);"
                + "var data=" + markers + ";"
                + "data.forEach(function(a){"
                + "var popup='<b>'+a.nombre+'</b><br><small>'+a.categoria+'</small><br>'+a.address"
                + "+'<br><button class=\"popup-btn\" onclick=\"Android.onComoLlegar('+a.lat+','+a.lng+')\">"
                + "🗺️ Cómo llegar</button>';"
                + "L.circleMarker([a.lat,a.lng],"
                + "{radius:8,fillColor:'#00C9A7',color:'black',weight:1,fillOpacity:0.85})"
                + ".addTo(map).bindPopup(popup,{maxWidth:220});"
                + "});"
                + "</script></body></html>";
    }

    private class MapaInterface {
        @JavascriptInterface
        public void onComoLlegar(double lat, double lng) {
            requireActivity().runOnUiThread(() -> {
                Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });
        }
    }

    private List<Actividad> obtenerTodasLasActividades() {
        List<Actividad> lista = new ArrayList<>();
        lista.add(new Actividad("Bar Casa Paco", "cafe", "Cafés", 39.4812302, -0.3721439, "Carrer del Pintor Domingo, Valencia"));
        lista.add(new Actividad("Pablo's breads, food & drink", "cafe", "Cafés", 39.4814109, -0.3641957, "Carrer de Jaume Roig, 15, Valencia", "Mo-Th 07:00-20:00; Fr 07:00-19:00", "+34 962051731", ""));
        lista.add(new Actividad("Café Bertzal", "cafe", "Cafés", 39.4748723, -0.3757718, "Valencia"));
        lista.add(new Actividad("Coffee Of Day", "cafe", "Cafés", 39.4812203, -0.3743013, "Valencia"));
        lista.add(new Actividad("Cafeteria Suizo", "cafe", "Cafés", 39.4713686, -0.3763119, "Valencia"));
        lista.add(new Actividad("Brunch Corner La Virgen", "cafe", "Cafés", 39.4769318, -0.3756298, "Carrer de la Virgen, Valencia", "Mo-Fr 08:00-20:00; Sa 09:00-20:00", "+34 963 91 52 30", ""));
        lista.add(new Actividad("Horchatería Daniel", "cafe", "Cafés", 39.4688997, -0.3681802, "Carrer de la Pau, Valencia", "", "+34 963 519 891", "https://horchateria-daniel.es/"));
        lista.add(new Actividad("MESTIZO", "cafe", "Cafés", 39.4774481, -0.378304, "Carrer del Pintor Sorolla, Valencia", "Mo-Fr 09:30-15:00; Sa-Su 09:30-16:30", "", ""));
        lista.add(new Actividad("Café Bastard Kitchen", "cafe", "Cafés", 39.4693417, -0.3683759, "Carrer de Colón, Valencia"));
        lista.add(new Actividad("Horchatería Els Sariers", "heladeria", "Heladerías", 39.492677, -0.3572466, "Carrer de Sagunt, Valencia"));
        lista.add(new Actividad("Nevartes", "heladeria", "Heladerías", 39.4789348, -0.3762979, "Carrer del Marqués de Dos Aguas, Valencia"));
        lista.add(new Actividad("Pasos", "pub", "Pubs", 39.4703793, -0.3892442, "Avinguda del Regne de Valencia, Valencia"));
        lista.add(new Actividad("Tyris on Tap", "pub", "Pubs", 39.4749163, -0.3790514, "Plaça de la Reina, Valencia"));
        lista.add(new Actividad("DJango", "restaurante", "Restaurantes", 39.4742601, -0.3812275, "Carrer de Guillem de Castro, Valencia"));
        lista.add(new Actividad("Bar Mojacar", "restaurante", "Restaurantes", 39.4887068, -0.3752922, "Carrer de Sagunt, 45, Valencia", "", "+34 963680369", ""));
        lista.add(new Actividad("Copenhagen", "restaurante", "Restaurantes", 39.4607625, -0.373582, "Carrer del Doctor Sumsi, Valencia", "", "", "https://restaurantecopenhagen.es"));
        lista.add(new Actividad("Crêperie bretonne annaick", "restaurante", "Restaurantes", 39.4752723, -0.3760263, "Carrer dels Cavallers, Valencia"));
        lista.add(new Actividad("Arroz y Tartana", "restaurante", "Restaurantes", 39.4728888, -0.3788644, "Carrer de la Llibertat, Valencia"));
        lista.add(new Actividad("Gran Mercat", "restaurante", "Restaurantes", 39.4730529, -0.3773792, "Plaça del Mercat, Valencia"));
        lista.add(new Actividad("Miss Sushi", "restaurante", "Restaurantes", 39.4681079, -0.3651693, "Carrer de Jorge Juan, Valencia"));
        lista.add(new Actividad("Centro de Arte Hortensia Herrero", "museo", "Museos", 39.4739684, -0.373282, "Carrer del Museu, 3, Valencia"));
        lista.add(new Actividad("Aseo público accesible", "bano", "Baños", 39.4660094, -0.3771345, "Av. de la Constitució, Valencia"));
        lista.add(new Actividad("Aseo público accesible", "bano", "Baños", 39.4630054, -0.3594123, "Carrer de la Reina, Valencia"));
        lista.add(new Actividad("Aseo público accesible", "bano", "Baños", 39.4792353, -0.3682922, "Carrer de Sagunt, Valencia"));
        lista.add(new Actividad("Aseo público accesible", "bano", "Baños", 39.4707386, -0.3774272, "Plaça de la Reina, Valencia"));
        lista.add(new Actividad("Farmacia accesible", "farmacia", "Farmacias", 39.4742601, -0.3812275, "Carrer de Guillem de Castro, Valencia"));
        lista.add(new Actividad("Farmacia accesible", "farmacia", "Farmacias", 39.4605726, -0.3746047, "Carrer del Doctor Sumsi, Valencia"));
        lista.add(new Actividad("Mercadona", "supermercado", "Supermercados", 39.4766594, -0.3628127, "Carrer de Colón, Valencia", "Mo-Sa 09:00-21:30", "", "https://www.mercadona.es"));
        lista.add(new Actividad("Consum", "supermercado", "Supermercados", 39.4919261, -0.3965606, "Carrer de Sagunt, Valencia", "Mo-Sa 09:00-21:30", "", "https://www.consum.es"));
        lista.add(new Actividad("Lidl", "supermercado", "Supermercados", 39.4870005, -0.3749129, "Carrer de Sagunt, 200, Valencia", "Mo-Sa 09:00-21:30", "", "https://www.lidl.es"));
        lista.add(new Actividad("ALDI", "supermercado", "Supermercados", 39.4766124, -0.3507026, "Avinguda del Port, Valencia"));
        lista.add(new Actividad("Estanco accesible", "estanco", "Estancos", 39.4767251, -0.3769278, "Carrer de les Barques, Valencia"));
        lista.add(new Actividad("Tabacos Exp. No.77", "estanco", "Estancos", 39.4644866, -0.3658784, "Carrer de Jorge Juan, 77, Valencia", "Mo-Fr 07:00-22:00; Sa-Su 07:30-22:00", "", ""));
        return lista;
    }
}
