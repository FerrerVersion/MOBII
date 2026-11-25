package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.j256.ormlite.dao.Dao;
import com.jorgeferrergil.mobii.api.RetrofitClient;
import com.jorgeferrergil.mobii.db.DatabaseHelper;
import com.jorgeferrergil.mobii.model.ParkingApiResponse;
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

public class ParkingActivity extends AppCompatActivity {

    private static final String DATASET = "aparcaments-persones-mobilitat-reduida-aparcamientos-personas-movilidad-reducida";
    private static final String PREFS_NAME = "parking_prefs";
    private static final String KEY_FAVORITOS = "favoritos_record_ids";

    private WebView webViewMapa;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private Button btnTodos;
    private Button btnFavoritos;

    private DatabaseHelper dbHelper;
    private final List<ParkingSpot> listaTodas = new ArrayList<>();
    private Set<String> favoritosIds;
    private boolean mostrandoFavoritos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);

        dbHelper = new DatabaseHelper(this);
        favoritosIds = cargarFavoritosDesdePrefs();

        progressBar = findViewById(R.id.progressBarParking);
        tvEstado = findViewById(R.id.tvEstadoParking);
        btnTodos = findViewById(R.id.btnVerTodos);
        btnFavoritos = findViewById(R.id.btnVerFavoritos);
        webViewMapa = findViewById(R.id.webViewParkingMapa);

        configurarWebView();
        webViewMapa.addJavascriptInterface(new ParkingBridge(), "Android");

        btnTodos.setOnClickListener(v -> cambiarVista(false));
        btnFavoritos.setOnClickListener(v -> cambiarVista(true));

        actualizarEstiloBotones();

        TextView tvLogo = findViewById(R.id.tvLogoHeader);
        tvLogo.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        cargarDatos();
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

    private void cambiarVista(boolean favoritos) {
        mostrandoFavoritos = favoritos;
        actualizarEstiloBotones();
        renderMapa();
    }

    private void actualizarEstiloBotones() {
        if (mostrandoFavoritos) {
            btnFavoritos.setBackgroundColor(0xFF004C7A);
            btnTodos.setBackgroundColor(0xFF007ACC);
        } else {
            btnTodos.setBackgroundColor(0xFF004C7A);
            btnFavoritos.setBackgroundColor(0xFF007ACC);
        }
    }

    private void cargarDatos() {
        progressBar.setVisibility(View.VISIBLE);
        tvEstado.setText("Cargando aparcamientos...");

        RetrofitClient.getInstance().getParkings(DATASET, 200, "json")
                .enqueue(new Callback<ParkingApiResponse>() {
                    @Override
                    public void onResponse(Call<ParkingApiResponse> call, Response<ParkingApiResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getRecords() != null) {
                            listaTodas.clear();
                            for (ParkingRecord r : response.body().getRecords()) {
                                ParkingSpot s = r.toParkingSpot();
                                if (s.getLat() != 0 && s.getLng() != 0) {
                                    listaTodas.add(s);
                                }
                            }
                            renderMapa();
                            guardarEnOrmlite();
                        } else {
                            cargarDeLocal();
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingApiResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        cargarDeLocal();
                    }
                });
    }

    private void cargarDeLocal() {
        new Thread(() -> {
            try {
                List<ParkingSpot> locales = dbHelper.getParkingDao().queryForAll();
                runOnUiThread(() -> {
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
                for (ParkingSpot s : listaTodas) {
                    dao.createOrUpdate(s);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderMapa() {
        List<ParkingSpot> vista = new ArrayList<>();
        for (ParkingSpot s : listaTodas) {
            if (!mostrandoFavoritos || favoritosIds.contains(s.getRecordId())) {
                vista.add(s);
            }
        }

        tvEstado.setText(mostrandoFavoritos
                ? "⭐ Favoritos: " + vista.size()
                : vista.size() + " aparcamientos");

        webViewMapa.loadDataWithBaseURL(
                "https://appassets.androidplatform.net",
                buildHtml(vista),
                "text/html",
                "UTF-8",
                null
        );
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
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
                + "background:#007ACC;color:#fff;border:none;border-radius:4px;"
                + "cursor:pointer;font-size:13px;}"
                + ".fav-btn:active{background:#004C7A;}"
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

                // Usar popup.on('add') para asignar el listener cuando el DOM ya existe,
                // evitando así todos los problemas de escapado de comillas en onclick.
                + "  m.on('click',function(){"
                + "    var isFav=favMap[s.id];"
                + "    var txt=isFav?'\uD83D\uDDD1\uFE0F Eliminar de favoritos':'\u2B50 A\u00F1adir a favoritos';"
                + "    var safeId=s.id.replace(/[^a-zA-Z0-9_-]/g,'_');"
                + "    var btnId='favbtn_'+safeId;"
                + "    var html='<div style=\"min-width:160px\"><b>'+s.dir+'</b>"
                + "<button class=\"fav-btn\" id=\"'+btnId+'\">'+txt+'</button></div>';"
                + "    var popup=L.popup().setContent(html);"
                + "    popup.on('add',function(){"
                + "      var btn=document.getElementById(btnId);"
                + "      if(btn){"
                + "        btn.onclick=function(){toggleFav(s.id,s.dir,btn);};"
                + "      }"
                + "    });"
                + "    m.bindPopup(popup).openPopup();"
                + "  });"
                + "});"

                + "if(bounds.length>0){map.fitBounds(bounds);}else{map.setView([39.4699,-0.3763],13);}"

                + "function toggleFav(id,dir,btn){"
                + "  var nuevoEstado=!favMap[id];"
                + "  favMap[id]=nuevoEstado;"
                + "  btn.innerText=nuevoEstado?'\uD83D\uDDD1\uFE0F Eliminar de favoritos':'\u2B50 A\u00F1adir a favoritos';"
                + "  Android.onToggleFav(id,dir,nuevoEstado?'true':'false');"
                + "}"

                + "</script></body></html>";
    }

    public class ParkingBridge {
        @JavascriptInterface
        public void onToggleFav(String id, String dir, String favStr) {
            boolean fav = "true".equals(favStr);

            if (fav) favoritosIds.add(id);
            else favoritosIds.remove(id);

            guardarFavoritosEnPrefs();

            runOnUiThread(() -> {
                Toast.makeText(ParkingActivity.this,
                        fav ? "⭐ Añadido a favoritos" : "Eliminado",
                        Toast.LENGTH_SHORT).show();

                if (mostrandoFavoritos) renderMapa();
            });
        }
    }

    private Set<String> cargarFavoritosDesdePrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITOS, new HashSet<>()));
    }

    private void guardarFavoritosEnPrefs() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_FAVORITOS, new HashSet<>(favoritosIds))
                .apply();
    }
}