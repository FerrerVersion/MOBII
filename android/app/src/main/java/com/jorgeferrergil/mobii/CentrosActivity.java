package com.jorgeferrergil.mobii;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CentrosActivity extends AppCompatActivity {

    private WebView webView;

    private static final String DS_GENERAL    = "discapacitat-discapacidad";
    private static final String DS_INTELECTUAL = "discapacitat-intellectual-discapacidad-intelectual";
    private static final String DS_SENSORIAL   = "discapacitat-sensorial-discapacidad-sensorial";
    private static final String DS_FISICA      = "discapacitat-fisica-discapacidad-fisica";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centros);

        webView = findViewById(R.id.webViewCentros);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new CentrosBridge(), "Android");

        Spinner spinner = findViewById(R.id.spinnerDiscapacidad);
        String[] tipos    = {"Discapacidad General", "Discapacidad Intelectual", "Discapacidad Sensorial", "Discapacidad Física"};
        String[] datasets = {DS_GENERAL, DS_INTELECTUAL, DS_SENSORIAL, DS_FISICA};
        double[] lats     = {39.46415, 39.46415, 39.47899, 39.47814};
        double[] lngs     = {-0.36853, -0.36853, -0.37696, -0.37133};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                cargarMapa(datasets[pos], lats[pos], lngs[pos]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextView tvLogo = findViewById(R.id.tvLogoHeader);
        tvLogo.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        cargarMapa(DS_GENERAL, 39.46415, -0.36853);
    }

    private void cargarMapa(String dataset, double lat, double lng) {
        // Se necesita una baseURL real para que fetch() funcione sin bloqueo CORS
        webView.loadDataWithBaseURL(
            "https://appassets.androidplatform.net",
            buildHtml(dataset, lat, lng),
            "text/html", "UTF-8", null
        );
    }

    private String buildHtml(String dataset, double lat, double lng) {
        // API v2.1 — la v1 (records/1.0) está deprecada y ya no devuelve datos
        String apiUrl = "https://valencia.opendatasoft.com/api/explore/v2.1/catalog/datasets/"
                + dataset + "/records?limit=100";

        return "<!DOCTYPE html><html><head>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1.0'>"
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
            + "<style>"
            + "html,body{margin:0;padding:0;height:100%}"
            + "#map{width:100%;height:100vh}"
            + "#loading{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);"
            + "background:white;padding:20px;border-radius:10px;font-size:14px;z-index:999;"
            + "box-shadow:0 2px 8px rgba(0,0,0,0.2);text-align:center}"
            + ".llegar-btn{margin-top:10px;padding:9px 12px;border:none;border-radius:6px;"
            + "background:#007ACC;color:#fff;font-size:13px;cursor:pointer;width:100%;font-weight:bold}"
            + "</style>"
            + "</head><body>"
            + "<div id='loading'>\uD83D\uDCCD Cargando centros...</div>"
            + "<div id='map'></div>"
            + "<script>"
            + "var map=L.map('map').setView([" + lat + "," + lng + "],13);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',"
            + "{attribution:'&copy; OpenStreetMap contributors'}).addTo(map);"

            // Obtiene el nombre del centro probando varios campos posibles
            + "function getNombre(f){"
            + "  var campos=['nombre_establecimiento','nombre_entidad','nom_entitat','nombre','nom',"
            + "    'name','entidad','entitat','denominacion','denominacio','centro','centre',"
            + "    'equipamiento','equipament','instalacion','installacio'];"
            + "  for(var i=0;i<campos.length;i++){"
            + "    if(f[campos[i]]&&String(f[campos[i]]).trim().length>0){"
            + "      return String(f[campos[i]]).trim();"
            + "    }"
            + "  }"
            + "  var keys=Object.keys(f);"
            + "  for(var j=0;j<keys.length;j++){"
            + "    var k=keys[j].toLowerCase();"
            + "    if((k.indexOf('nombre')>=0||k.indexOf('nom')>=0||k.indexOf('name')>=0"
            + "      ||k.indexOf('entidad')>=0||k.indexOf('centro')>=0)&&f[keys[j]]){"
            + "      return String(f[keys[j]]).trim();"
            + "    }"
            + "  }"
            + "  return null;"
            + "}"

            // Fetch con la API v2.1 — los registros llegan en data.results[]
            // Las coordenadas están en result.geo_point_2d = {lat, lon}
            + "fetch('" + apiUrl + "')"
            + ".then(function(r){"
            + "  if(!r.ok) throw new Error('HTTP '+r.status);"
            + "  return r.json();"
            + "})"
            + ".then(function(data){"
            + "  document.getElementById('loading').style.display='none';"
            + "  var results=data.results||[];"
            + "  if(results.length===0){"
            + "    document.getElementById('loading').style.display='block';"
            + "    document.getElementById('loading').innerText='No se encontraron centros.';"
            + "    return;"
            + "  }"
            + "  var markers=[];"
            + "  results.forEach(function(rec,idx){"
            // geo_point_2d puede ser objeto {lat,lon} o array [lon,lat] según el dataset
            + "    var geo=rec.geo_point_2d;"
            + "    if(!geo) return;"
            + "    var cLat,cLng;"
            + "    if(typeof geo==='object'&&!Array.isArray(geo)){"
            + "      cLat=geo.lat; cLng=geo.lon;"
            + "    } else if(Array.isArray(geo)){"
            + "      cLng=geo[0]; cLat=geo[1];"
            + "    } else return;"
            + "    if(!cLat||!cLng) return;"
            + "    var nombre=getNombre(rec)||'Centro adaptado';"
            + "    var dir=rec.direccion||rec.adrecae||rec.adresa||rec.address||rec.domicilio||'';"
            + "    var tel=rec.telefono||rec.telefon||rec.phone||'';"
            + "    var tipo=rec.tipo_discapacidad||rec.tipo||rec.tipus||rec.especialidad||'';"
            // Construimos popup sin onclick inline — usamos popup.on('add') igual que en parking
            + "    var btnId='btn_'+idx;"
            + "    var popupHtml='<div style=\"min-width:180px\"><b>'+nombre+'</b>';"
            + "    if(dir) popupHtml+='<br>\uD83D\uDCCD '+dir;"
            + "    if(tipo) popupHtml+='<br><small>'+tipo+'</small>';"
            + "    if(tel) popupHtml+='<br>\uD83D\uDCDE '+tel;"
            + "    popupHtml+='<button class=\"llegar-btn\" id=\"'+btnId+'\">\uD83D\uDDFA\uFE0F C\u00F3mo llegar</button></div>';"
            + "    var popup=L.popup({maxWidth:260}).setContent(popupHtml);"
            + "    (function(lat,lng,bid){"
            + "      popup.on('add',function(){"
            + "        var btn=document.getElementById(bid);"
            + "        if(btn) btn.onclick=function(){Android.onComoLlegar(lat,lng);};"
            + "      });"
            + "    })(cLat,cLng,btnId);"
            + "    var marker=L.circleMarker([cLat,cLng],"
            + "      {radius:9,fillColor:'#005F99',color:'#003366',weight:2,fillOpacity:0.85})"
            + "    .addTo(map).bindPopup(popup);"
            + "    markers.push([cLat,cLng]);"
            + "  });"
            + "  if(markers.length>0) map.fitBounds(markers);"
            + "})"
            + ".catch(function(err){"
            + "  document.getElementById('loading').style.display='block';"
            + "  document.getElementById('loading').innerText='Error al cargar. Comprueba tu conex\u00F3n.';"
            + "});"
            + "</script></body></html>";
    }

    public class CentrosBridge {
        @JavascriptInterface
        public void onComoLlegar(double lat, double lng) {
            runOnUiThread(() -> {
                Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
