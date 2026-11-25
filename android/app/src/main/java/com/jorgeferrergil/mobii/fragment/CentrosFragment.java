package com.jorgeferrergil.mobii.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jorgeferrergil.mobii.R;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CentrosFragment extends Fragment {

    private WebView webView;
    private TextView tvEstado;

    // Capas del geoportal ArcGIS de Valencia (SociedadBienestar MapServer)
    private static final int LAYER_GENERAL     = 17;
    private static final int LAYER_FISICA      = 18;
    private static final int LAYER_INTELECTUAL = 19;
    private static final int LAYER_SENSORIAL   = 20;

    private static final String BASE_URL =
            "https://geoportal.valencia.es/server/rest/services/OPENDATA/SociedadBienestar/MapServer/";

    private final OkHttpClient httpClient = new OkHttpClient();
    private Call peticionActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_centros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.webViewCentros);
        tvEstado = view.findViewById(R.id.tvEstadoCentros);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new CentrosBridge(), "Android");

        String[] tipos  = {"Discapacidad General", "Discapacidad Física",
                           "Discapacidad Intelectual", "Discapacidad Sensorial"};
        int[]    layers = {LAYER_GENERAL, LAYER_FISICA, LAYER_INTELECTUAL, LAYER_SENSORIAL};

        Spinner spinner = view.findViewById(R.id.spinnerDiscapacidad);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item_centros, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                cargarCentros(layers[pos]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void mostrarEstado(String texto) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (tvEstado == null) return;
            tvEstado.setText(texto);
            tvEstado.setVisibility(texto.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void cargarCentros(int layerId) {
        mostrarEstado("Cargando centros...");

        if (peticionActual != null) peticionActual.cancel();

        // ArcGIS REST API: GeoJSON, solo registros con coordenadas
        String url = BASE_URL + layerId
                + "/query?where=x+IS+NOT+NULL&outFields=*&f=geojson&resultRecordCount=100";

        Request request = new Request.Builder().url(url).build();
        peticionActual = httpClient.newCall(request);
        peticionActual.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled())
                    mostrarEstado("Sin conexión. Comprueba internet.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    mostrarEstado("Error del servidor (" + response.code() + ").");
                    return;
                }
                String body = response.body().string();
                try {
                    JsonObject json = new JsonParser().parse(body).getAsJsonObject();
                    JsonArray features = json.getAsJsonArray("features");
                    if (features == null || features.size() == 0) {
                        mostrarEstado("No se encontraron centros.");
                        return;
                    }
                    String html = buildHtml(features);
                    requireActivity().runOnUiThread(() -> {
                        mostrarEstado("");
                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    });
                } catch (Exception e) {
                    mostrarEstado("Error al procesar los datos.");
                }
            }
        });
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("'", "\\'").replace("\n", " ");
    }

    private String buildHtml(JsonArray features) {
        StringBuilder markers = new StringBuilder("[");
        int count = 0;
        double firstLat = 39.4699, firstLng = -0.3763;

        for (JsonElement el : features) {
            JsonObject feat = el.getAsJsonObject();

            // Coordenadas desde geometry.coordinates [lng, lat]
            JsonElement geomEl = feat.get("geometry");
            if (geomEl == null || geomEl.isJsonNull()) continue;
            JsonObject geom = geomEl.getAsJsonObject();
            JsonElement coordsEl = geom.get("coordinates");
            if (coordsEl == null || coordsEl.isJsonNull()) continue;
            JsonArray coords = coordsEl.getAsJsonArray();
            if (coords.size() < 2) continue;

            double lng = coords.get(0).getAsDouble();
            double lat = coords.get(1).getAsDouble();
            if (lat == 0 || lng == 0) continue;

            // Campos de propiedades
            JsonObject props = feat.getAsJsonObject("properties");
            String nombre = getStr(props, "equipamien", "nombre", "name");
            if (nombre.isEmpty()) nombre = "Centro adaptado";

            // Teléfono puede ser número o string
            String tel = "";
            JsonElement telEl = props.get("telefono");
            if (telEl != null && !telEl.isJsonNull()) tel = telEl.getAsString();

            if (count == 0) { firstLat = lat; firstLng = lng; }
            if (count > 0) markers.append(",");
            markers.append("{")
                    .append("\"lat\":").append(lat).append(",")
                    .append("\"lng\":").append(lng).append(",")
                    .append("\"nombre\":\"").append(esc(nombre)).append("\",")
                    .append("\"tel\":\"").append(esc(tel)).append("\"")
                    .append("}");
            count++;
        }
        markers.append("]");

        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>"
                + "html,body{margin:0;padding:0;height:100%}"
                + "#map{width:100%;height:100vh}"
                + ".llegar-btn{margin-top:10px;padding:9px 12px;border:none;border-radius:6px;"
                + "background:#5B6EE1;color:#fff;font-size:13px;cursor:pointer;width:100%;font-weight:bold}"
                + "</style>"
                + "</head><body><div id='map'></div><script>"
                + "var map=L.map('map').setView([" + firstLat + "," + firstLng + "],12);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',"
                + "{attribution:'&copy; OpenStreetMap contributors'}).addTo(map);"
                + "var data=" + markers + ";"
                + "var bounds=[];"
                + "data.forEach(function(r,idx){"
                + "  var btnId='btn_'+idx;"
                + "  var html='<div style=\"min-width:160px\"><b>'+r.nombre+'</b>';"
                + "  if(r.tel) html+='<br>📞 '+r.tel;"
                + "  html+='<button class=\"llegar-btn\" id=\"'+btnId+'\">"
                + "🗺️ Cómo llegar</button></div>';"
                + "  var popup=L.popup({maxWidth:240}).setContent(html);"
                + "  (function(lat,lng,bid){"
                + "    popup.on('add',function(){"
                + "      var b=document.getElementById(bid);"
                + "      if(b) b.onclick=function(){Android.onComoLlegar(lat,lng);};"
                + "    });"
                + "  })(r.lat,r.lng,btnId);"
                + "  L.circleMarker([r.lat,r.lng],"
                + "    {radius:9,fillColor:'#5B6EE1',color:'#4A5CC8',weight:2,fillOpacity:0.85})"
                + "  .addTo(map).bindPopup(popup);"
                + "  bounds.push([r.lat,r.lng]);"
                + "});"
                + "if(bounds.length>0) map.fitBounds(bounds);"
                + "</script></body></html>";
    }

    private String getStr(JsonObject obj, String... keys) {
        for (String k : keys) {
            JsonElement el = obj.get(k);
            if (el != null && !el.isJsonNull()) {
                String v = el.getAsString().trim();
                if (!v.isEmpty()) return v;
            }
        }
        return "";
    }

    public class CentrosBridge {
        @JavascriptInterface
        public void onComoLlegar(double lat, double lng) {
            requireActivity().runOnUiThread(() -> {
                Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                        + lat + "," + lng);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (peticionActual != null) peticionActual.cancel();
    }
}
