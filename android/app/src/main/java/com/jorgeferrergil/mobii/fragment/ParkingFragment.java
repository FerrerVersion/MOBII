package com.jorgeferrergil.mobii.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.jorgeferrergil.mobii.R;
import com.jorgeferrergil.mobii.api.RetrofitClient;
import com.jorgeferrergil.mobii.auth.AuthRetrofitClient;
import com.jorgeferrergil.mobii.auth.SyncFavoritoRequest;
import com.jorgeferrergil.mobii.auth.SyncIdResponse;
import com.jorgeferrergil.mobii.db.DatabaseHelper;
import com.jorgeferrergil.mobii.model.ParkingApiResponse;
import com.jorgeferrergil.mobii.model.ParkingFavorito;
import com.jorgeferrergil.mobii.model.ParkingRecord;
import com.jorgeferrergil.mobii.model.ParkingSpot;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingFragment extends Fragment {

    private static final String DATASET = "aparcaments-persones-mobilitat-reduida-aparcamientos-personas-movilidad-reducida";

    private WebView webViewMapa;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private Button btnTodos;
    private Button btnFavoritos;

    private DatabaseHelper dbHelper;
    private String userId;
    private String token;
    private final List<ParkingSpot> listaTodas = new ArrayList<>();
    private Set<String> favoritosIds = new HashSet<>();
    private boolean mostrandoFavoritos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences("mobii_session", 0);
        userId = prefs.getString("email", "");
        token = prefs.getString("token", "");

        dbHelper = new DatabaseHelper(requireContext());

        progressBar = view.findViewById(R.id.progressBarParking);
        tvEstado = view.findViewById(R.id.tvEstadoParking);
        btnTodos = view.findViewById(R.id.btnVerTodos);
        btnFavoritos = view.findViewById(R.id.btnVerFavoritos);
        webViewMapa = view.findViewById(R.id.webViewParkingMapa);

        configurarWebView();
        webViewMapa.addJavascriptInterface(new ParkingBridge(), "Android");

        btnTodos.setOnClickListener(v -> cambiarVista(false));
        btnFavoritos.setOnClickListener(v -> cambiarVista(true));

        actualizarEstiloBotones();
        cargarFavoritosYDespuesDatos();
    }

    private void configurarWebView() {
        WebSettings ws = webViewMapa.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewMapa.setWebViewClient(new WebViewClient());
        webViewMapa.setWebChromeClient(new WebChromeClient());
        webViewMapa.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebView.setWebContentsDebuggingEnabled(true);
    }

    private void cargarFavoritosYDespuesDatos() {
        new Thread(() -> {
            try {
                List<ParkingFavorito> favs = dbHelper.getFavoritoDao()
                        .queryBuilder().where().eq("userId", userId).query();
                Set<String> ids = new HashSet<>();
                for (ParkingFavorito f : favs) ids.add(f.getRecordId());
                requireActivity().runOnUiThread(() -> {
                    favoritosIds = ids;
                    cargarDatos();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(this::cargarDatos);
            }
        }).start();
    }

    private void cambiarVista(boolean favoritos) {
        mostrandoFavoritos = favoritos;
        actualizarEstiloBotones();
        renderMapa();
    }

    private void actualizarEstiloBotones() {
        int activo = 0xFF4A5CC8;
        int inactivo = 0xFF7B8EF5;
        if (mostrandoFavoritos) {
            btnFavoritos.setBackgroundColor(activo);
            btnTodos.setBackgroundColor(inactivo);
        } else {
            btnTodos.setBackgroundColor(activo);
            btnFavoritos.setBackgroundColor(inactivo);
        }
    }

    private void cargarDatos() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        tvEstado.setText("Cargando aparcamientos...");

        RetrofitClient.getInstance().getParkings(DATASET, 200, "json")
                .enqueue(new Callback<ParkingApiResponse>() {
                    @Override
                    public void onResponse(Call<ParkingApiResponse> call, Response<ParkingApiResponse> response) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getRecords() != null) {
                            listaTodas.clear();
                            for (ParkingRecord r : response.body().getRecords()) {
                                ParkingSpot s = r.toParkingSpot();
                                if (s.getLat() != 0 && s.getLng() != 0) listaTodas.add(s);
                            }
                            renderMapa();
                            guardarEnOrmlite();
                        } else {
                            cargarDeLocal();
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingApiResponse> call, Throwable t) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        cargarDeLocal();
                    }
                });
    }

    private void cargarDeLocal() {
        new Thread(() -> {
            try {
                List<ParkingSpot> locales = dbHelper.getParkingDao().queryForAll();
                requireActivity().runOnUiThread(() -> {
                    listaTodas.clear();
                    listaTodas.addAll(locales);
                    renderMapa();
                    if (locales.isEmpty()) tvEstado.setText("Sin datos. Conéctate a internet.");
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void guardarEnOrmlite() {
        new Thread(() -> {
            try {
                Dao<ParkingSpot, Integer> dao = dbHelper.getParkingDao();
                for (ParkingSpot s : listaTodas) dao.createOrUpdate(s);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderMapa() {
        List<ParkingSpot> vista = new ArrayList<>();
        for (ParkingSpot s : listaTodas) {
            if (!mostrandoFavoritos || favoritosIds.contains(s.getRecordId())) vista.add(s);
        }

        tvEstado.setText(mostrandoFavoritos
                ? "⭐ Favoritos: " + vista.size()
                : vista.size() + " aparcamientos");

        webViewMapa.loadDataWithBaseURL(
                "https://appassets.androidplatform.net",
                buildHtml(vista),
                "text/html", "UTF-8", null
        );
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'")
                .replace("\n", " ").replace("\r", "");
    }

    private String buildHtml(List<ParkingSpot> spots) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < spots.size(); i++) {
            ParkingSpot s = spots.get(i);
            if (i > 0) json.append(",");
            boolean fav = favoritosIds.contains(s.getRecordId());
            json.append("{")
                    .append("\"id\":\"").append(esc(s.getRecordId())).append("\",")
                    .append("\"dir\":\"").append(esc(s.getDireccionCompleta())).append("\",")
                    .append("\"fav\":").append(fav).append(",")
                    .append("\"lat\":").append(s.getLat()).append(",")
                    .append("\"lng\":").append(s.getLng())
                    .append("}");
        }
        json.append("]");

        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>"
                + "html,body,#map{height:100%;margin:0}"
                + ".fav-btn{display:block;width:100%;margin-top:8px;padding:6px 10px;"
                + "background:#5B6EE1;color:#fff;border:none;border-radius:4px;"
                + "cursor:pointer;font-size:13px;}"
                + ".fav-btn:active{background:#4A5CC8;}"
                + "</style>"
                + "</head><body><div id='map'></div><script>"
                + "var favMap={};"
                + "var spots=" + json + ";"
                + "var map=L.map('map');"
                + "var bounds=[];"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);"
                + "spots.forEach(function(s){"
                + "  favMap[s.id]=s.fav;"
                + "  var m=L.marker([s.lat,s.lng]).addTo(map);"
                + "  bounds.push([s.lat,s.lng]);"
                + "  m.on('click',function(){"
                + "    var isFav=favMap[s.id];"
                + "    var txt=isFav?'🗑️ Eliminar de favoritos':'⭐ Añadir a favoritos';"
                + "    var safeId=s.id.replace(/[^a-zA-Z0-9_-]/g,'_');"
                + "    var btnId='favbtn_'+safeId;"
                + "    var html='<div style=\"min-width:160px\"><b>'+s.dir+'</b>"
                + "<button class=\"fav-btn\" id=\"'+btnId+'\">'+txt+'</button></div>';"
                + "    var popup=L.popup().setContent(html);"
                + "    popup.on('add',function(){"
                + "      var btn=document.getElementById(btnId);"
                + "      if(btn){btn.onclick=function(){toggleFav(s.id,s.dir,btn);};}"
                + "    });"
                + "    m.bindPopup(popup).openPopup();"
                + "  });"
                + "});"
                + "if(bounds.length>0){map.fitBounds(bounds);}else{map.setView([39.4699,-0.3763],13);}"
                + "function toggleFav(id,dir,btn){"
                + "  var nuevoEstado=!favMap[id];"
                + "  favMap[id]=nuevoEstado;"
                + "  btn.innerText=nuevoEstado?'🗑️ Eliminar de favoritos':'⭐ Añadir a favoritos';"
                + "  Android.onToggleFav(id,dir,nuevoEstado?'true':'false');"
                + "}"
                + "</script></body></html>";
    }

    public class ParkingBridge {
        @JavascriptInterface
        public void onToggleFav(String id, String dir, String favStr) {
            boolean fav = "true".equals(favStr);

            if (fav) {
                favoritosIds.add(id);
                guardarFavoritoEnDb(id, dir);
            } else {
                favoritosIds.remove(id);
                eliminarFavoritoDeDb(id);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        fav ? "⭐ Añadido a favoritos" : "Eliminado de favoritos",
                        Toast.LENGTH_SHORT).show();
                if (mostrandoFavoritos) renderMapa();
            });
        }
    }

    private void guardarFavoritoEnDb(String recordId, String dir) {
        new Thread(() -> {
            try {
                ParkingFavorito fav = new ParkingFavorito(userId, recordId, dir);
                dbHelper.getFavoritoDao().create(fav);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Sync al backend; fallo silencioso si no hay red
            if (!token.isEmpty()) {
                AuthRetrofitClient.getInstance().getService()
                        .syncAddFavorito("Bearer " + token, new SyncFavoritoRequest(recordId, dir))
                        .enqueue(new Callback<SyncIdResponse>() {
                            @Override public void onResponse(Call<SyncIdResponse> c, Response<SyncIdResponse> r) {}
                            @Override public void onFailure(Call<SyncIdResponse> c, Throwable t) {}
                        });
            }
        }).start();
    }

    private void eliminarFavoritoDeDb(String recordId) {
        new Thread(() -> {
            try {
                DeleteBuilder<ParkingFavorito, Integer> del = dbHelper.getFavoritoDao().deleteBuilder();
                del.where().eq("userId", userId).and().eq("recordId", recordId);
                del.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Sync al backend; fallo silencioso si no hay red
            if (!token.isEmpty()) {
                AuthRetrofitClient.getInstance().getService()
                        .syncDeleteFavorito("Bearer " + token, recordId)
                        .enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
                            @Override public void onFailure(Call<Void> c, Throwable t) {}
                        });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) dbHelper.close();
    }
}
