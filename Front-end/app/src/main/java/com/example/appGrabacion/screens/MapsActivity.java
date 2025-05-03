package com.example.appGrabacion.screens;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.example.appGrabacion.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private double lat = 0, lon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 1) Extrae lat/lon del Intent
        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", 0);
            lon = getIntent().getDoubleExtra("lon", 0);
        }

        // 2) Obtén el fragmento y pide el mapa asincrónicamente
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Se invoca cuando el mapa ya está listo.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 3) Crea un LatLng, añade marcador y centra la cámara
        LatLng posicion = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions()
                .position(posicion)
                .title("Ubicación en vivo"));
        // zoom entre 10–20 (más alto = más cerca)
        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(posicion, 15f));
    }
}
