package com.aqsa.graduation_project_app.ui.clientSide;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.DistributorLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mapClientSelectLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker ReceivingLocationMarker;
    private static final int MARKER_TAG = 1;
    private static final int RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_client_select_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (getIntent().hasExtra("dist_location")){
            initMapToolBar("Distributor Location");
            String id_dist=getIntent().getStringExtra("dist_location");
            if (id_dist!=null) {
                FirebaseDatabase.getInstance().getReference().child("DistributorsLocations").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.getValue(DistributorLocation.class).getId().equals(id_dist)){
                                DistributorLocation location= snapshot1.getValue(DistributorLocation.class);

                                CameraPosition.Builder b = new CameraPosition.Builder();
                                b.target(new LatLng(location.getLatPoint(), location.getLongPoint()));
                                b.zoom(15);
                                b.tilt(45);//زاوية العرض
                                b.bearing(360);//راح يلف 180 درجة عند الانتقال
                                CameraPosition cameraPosition = b.build();
                                CameraUpdate cameraUpdate = CameraUpdateFactory
                                        .newCameraPosition(cameraPosition);
                                mMap.animateCamera(cameraUpdate, 4000, null);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.getLatPoint(),location.getLongPoint()))
                                        .title("Distributor Location")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }else {
            initMapToolBar("Select Location");
            CameraPosition.Builder b = new CameraPosition.Builder();
            b.target(new LatLng(31.5, 34.46667));
            b.zoom(15);
            b.tilt(45);//زاوية العرض
            b.bearing(360);//راح يلف 180 درجة عند الانتقال
            CameraPosition cameraPosition = b.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate, 4000, null);

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    if (ReceivingLocationMarker != null)
                        ReceivingLocationMarker.remove();
                    ReceivingLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("receive Here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    ReceivingLocationMarker.setTag(MARKER_TAG);
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    if (marker.getTag().equals(MARKER_TAG)) {
                        Intent i = new Intent();
                        i.putExtra("lat", marker.getPosition().latitude + "");
                        i.putExtra("long", marker.getPosition().longitude + "");
                        setResult(RESULT_CODE, i);
                        finish();
                    }
                    return true;
                }
            });
        }
    }

    public void initMapToolBar(String title) {
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mMap != null) {
            if (item.getItemId() == R.id.normalMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (item.getItemId() == R.id.satelliteMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (item.getItemId() == R.id.zoomIn) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomIn();
                mMap.moveCamera(cameraUpdate);
            } else if (item.getItemId() == R.id.zoomOut) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomOut();
                mMap.moveCamera(cameraUpdate);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}