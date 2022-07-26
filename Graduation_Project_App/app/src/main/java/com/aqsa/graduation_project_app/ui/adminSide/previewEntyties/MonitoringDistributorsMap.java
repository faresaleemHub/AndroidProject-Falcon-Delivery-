package com.aqsa.graduation_project_app.ui.adminSide.previewEntyties;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.InfoWindowAdapterDistributrsLocations;
import com.aqsa.graduation_project_app.model.DistributorLocation;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MonitoringDistributorsMap extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ProgressBar progressBar;
    ArrayList<Marker> mMarkerList;
    HashMap<String, Employee> hashMapLocations;
    public static String COMPANY_MARKER_TAG = "FalconDelivery_CompanyLocation";
    CountDownTimer countDownTimer;
    /*
    ملاحظة مهمة:
    Employee.id==marker.tag==distributorLocation.id فانا كنت اتتعامل معهم عوضاً عن الباقي
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_distributors_map);
        initMapToolBar();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        progressBar = findViewById(R.id.progressBar_previewDistributorsPlaces_map);
        mMarkerList = new ArrayList<>();//to save the markers which added to the map
        hashMapLocations = new HashMap<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        defaultCompanyPosition();
        selectDistributorsPlaces();
    }

    public void initMapToolBar() {
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setTitle("Monitor Map");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void defaultCompanyPosition() {
        LatLng companyPosition = new LatLng(31.3547, 34.3088);
        Marker companyMarker = mMap.addMarker(new MarkerOptions()
                .position(companyPosition)
                .title("FalconDelivery")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        companyMarker.setTag(COMPANY_MARKER_TAG);
        mMarkerList.add(companyMarker);

        //to save the id of company marker.
        hashMapLocations.put(COMPANY_MARKER_TAG, null); // null for Employee cell.

        //to move the camera to the company position
        CameraPosition.Builder b = new CameraPosition.Builder();
        b.target(new LatLng(companyPosition.latitude, companyPosition.longitude));
        b.zoom(13);
        b.tilt(45);//زاوية العرض
        b.bearing(360);//راح يلف 180 درجة عند الانتقال
        CameraPosition cameraPosition = b.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        mMap.animateCamera(cameraUpdate, 4000, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu_monitoring_distribtor_orders, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mMap != null) {
            if (item.getItemId() == R.id.normalMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (item.getItemId() == R.id.satelliteMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (item.getItemId() == R.id.hybridMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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

    private void selectDistributorsPlaces() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("DistributorsLocations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DistributorLocation location = snapshot.getValue(DistributorLocation.class);
                LatLng position = new LatLng(location.getLatPoint(), location.getLongPoint());
                Marker marker1 = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                marker1.setTag(location.getId());

                mMarkerList.add(marker1);

                hashMapLocations.put(snapshot.getValue(DistributorLocation.class).getId(), null);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DistributorLocation location = snapshot.getValue(DistributorLocation.class);
                for (Marker marker : mMarkerList) {
                    if (marker.getTag().equals(location.getId())) {
                        marker.setPosition(new LatLng(location.getLatPoint(), location.getLongPoint()));
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Toast.makeText(this, "wait a moment for loading Distributors", Toast.LENGTH_SHORT).show();
        countDownTimer=new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (hashMapLocations.size()>1){
//                    progressBar.setVisibility(View.GONE);
                    inflateDetailsOfMarkersOnMap();//inflate even if there's one entity - a company- or more.
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (hashMapLocations.size() == 1) {//only one for company marker
                    Toast.makeText(getApplicationContext(),
                            "No Available Distributors or there's a problem with Internet connection",
                            Toast.LENGTH_SHORT).show();

                    //even if there's no Distributors , there's a company marker still
//                    progressBar.setVisibility(View.GONE);
                }
                    inflateDetailsOfMarkersOnMap();//inflate even if there's one entity - a company- or more.
            }
        }.start();
    }

    //لعرض صاحب العلامة
    // to handle the owner of marker 'employee object'
    public void inflateDetailsOfMarkersOnMap() {

        if (hashMapLocations.size()!=1) {
            //here I'll collect Employees للي الرقم المعرف تاعهم بساوي الرقم المعرف للماركرز ,عشان اربطهم مع بعض في الهاش وابعتهم للادابتر فيقدر يصل للموظفنين كأوبجكتس وياخد بياناتهم
            FirebaseDatabase.getInstance().getReference().child("Employees").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Employee employee = snapshot.getValue(Employee.class);
                    if (hashMapLocations.containsKey(employee.getId())) {
                        hashMapLocations.remove(employee.getId());//to remove the older one Where have null Employee and replace it with not null one
                        hashMapLocations.put(employee.getId(), employee);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // counter for collect the employees"Distributors"
            new CountDownTimer(4000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.GONE);
                    mMap.setInfoWindowAdapter(new InfoWindowAdapterDistributrsLocations(getApplicationContext(),
                            hashMapLocations));
                }
            }.start();
        }else{
            mMap.setInfoWindowAdapter(new InfoWindowAdapterDistributrsLocations(getApplicationContext(),
                    hashMapLocations));
        }
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}
