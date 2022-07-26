package com.aqsa.graduation_project_app.ui.distributorSide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.DistributorLocation;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.clientSide.RecieptDetails;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MonitoringDistributorOrders extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Employee employee;
    ArrayList<ClientOrderReceipt> orders_receipt_data_list;
    ProgressBar progressBar;
    DistributorLocation distributorLocation;
    Marker MyLocationMarker;
    public static String source = "MonitoringDistributorOrders";
    Polyline shortest_polyline = null;
    HashMap<String, ClientOrderReceipt> hashMap_receipts;

    double shortest_path = 0;
    double order_lat_x = 0, order_long_y = 0;
    double distributor_lat_x, distributor_long_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_distributor_orders);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initMapToolBar();
        progressBar = findViewById(R.id.progressBar_previewReceivedOrdersDistributor_map);
        orders_receipt_data_list = new ArrayList<>();
        hashMap_receipts = new HashMap<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        employee = selectSharedAccount();

        //here we need to add a marker for my location
        getDistributorLocation();

        //here I need to show the place of Distributor's Clients/Orders
        selectOrders();
    }

    public DistributorLocation getDistributorLocation() {
        FirebaseDatabase.getInstance().getReference().child("DistributorsLocations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(DistributorLocation.class).getId().equals(employee.getId())) {
                    //here will get the location and راح يستمع للتغيرات على موقعه
                    distributorLocation = snapshot.getValue(DistributorLocation.class);

                    if (MyLocationMarker != null)
                        MyLocationMarker.remove();

                    //Add my real-time location a marker
                    MyLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(distributorLocation.getLatPoint(), distributorLocation.getLongPoint()))
                            .title(employee.getUsername())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    MyLocationMarker.setTag("" + distributorLocation.getId());
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                distributorLocation = snapshot.getValue(DistributorLocation.class);
                MyLocationMarker.setPosition(new LatLng(distributorLocation.getLatPoint(), distributorLocation.getLongPoint()));
                if (!getIntent().hasExtra("show_Received") && !getIntent().hasExtra("show_done"))
                    DrawShortestPath();
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

        return distributorLocation;
    }

    private void selectOrders() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                if (getIntent().hasExtra("show_Received")) {
                    if (cor1.getResponsibleDistributorID() != null)
                        if (cor1.getResponsibleDistributorID().equals(employee.getId()) &&
                                !cor1.isDistributorReceivedTheOrderFromTheStore()) {
                            orders_receipt_data_list.add(cor1);
                            hashMap_receipts.put(cor1.getId(), cor1);
                        }
                } else if (getIntent().hasExtra("show_Loaded")) {
                    if (cor1.getResponsibleDistributorID() != null &&
                            cor1.getResponsibleDistributorID().equals(employee.getId()) &&
                            cor1.isDistributorReceivedTheOrderFromTheStore() &&
                            !cor1.isDeliveredByDistributorToClient()) {
                        orders_receipt_data_list.add(cor1);
                        hashMap_receipts.put(cor1.getId(), cor1);
                    }
                } else if (getIntent().hasExtra("show_done")) {
                    if (cor1.getResponsibleDistributorID() != null &&
                            cor1.getResponsibleDistributorID().equals(employee.getId()) &&
                            cor1.isDeliveredByDistributorToClient()) {
                        orders_receipt_data_list.add(cor1);
                        hashMap_receipts.put(cor1.getId(), cor1);
                    }
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

        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (orders_receipt_data_list.size() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "No Available Orders or there's a problem with Internet connection",
                            Toast.LENGTH_SHORT).show();
                } else {
                    inflateOrdersOnMap();
                    if (!getIntent().hasExtra("show_Received") && !getIntent().hasExtra("show_done"))
                        DrawShortestPath();
                }
                progressBar.setVisibility(View.GONE);

                //go to the position of Distributor after loading orders
                if (distributorLocation != null) {
                    CameraPosition.Builder b = new CameraPosition.Builder();
                    b.target(new LatLng(distributorLocation.getLatPoint(), distributorLocation.getLongPoint()));
                    b.zoom(12);
                    b.tilt(45);//زاوية العرض
                    b.bearing(360);//راح يلف 180 درجة عند الانتقال
                    CameraPosition cameraPosition = b.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory
                            .newCameraPosition(cameraPosition);
                    mMap.animateCamera(cameraUpdate, 4000, null);
                }
            }
        }.start();
    }

    public void inflateOrdersOnMap() {

        for (int i = 0; i < orders_receipt_data_list.size(); i++) {
            ClientOrderReceipt receipt1 = orders_receipt_data_list.get(i);
            LatLng position = new LatLng(Double.parseDouble(receipt1.getLat_location()), Double.parseDouble(receipt1.getLong_location()));
            Marker marker1 = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("order" + (i + 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            marker1.setTag("" + receipt1.getId());
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (!marker.getTag().equals(distributorLocation.getId())) {
                    ClientOrderReceipt receipt = hashMap_receipts.get(marker.getTag());
                    Intent i = new Intent(getApplicationContext(), RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source", source);
                    startActivity(i);
                    GroupFunctions.openTransitionStyle(MonitoringDistributorOrders.this);
                }
                return false;
            }
        });
    }

    public Employee selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        employee = gson.fromJson(json, Employee.class);
        return employee;
    }

    public void initMapToolBar() {
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setTitle("Monitor Map");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            } else if (item.getItemId() == R.id.distributorMyLocation) {
                Marker marker1 = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(distributorLocation.getLatPoint(), distributorLocation.getLongPoint()))
                        .title("" + employee.getUsername())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                marker1.setTag("" + distributorLocation.getId());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(distributorLocation.getLatPoint(), distributorLocation.getLongPoint()), 12));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void DrawShortestPath() {
        if (shortest_polyline != null)
            shortest_polyline.remove();
        distributor_lat_x = distributorLocation.getLatPoint();
        distributor_long_y = distributorLocation.getLongPoint();

        if (orders_receipt_data_list.size() > 0) {
            for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                double test_order_lat_x =
                        Double.parseDouble(orders_receipt_data_list.get(i).getLat_location());
                double test_order_long_y =
                        Double.parseDouble(orders_receipt_data_list.get(i).getLong_location());
                double distance = Math.abs(distributor_lat_x - test_order_lat_x) +
                        Math.abs(distributor_long_y - test_order_long_y);
                if (shortest_path == 0) {
                    shortest_path = distance;
                    order_lat_x = test_order_lat_x;
                    order_long_y = test_order_long_y;
                } else if (shortest_path > distance) {
                    shortest_path = distance;
                    order_lat_x = test_order_lat_x;
                    order_long_y = test_order_long_y;
                }
            }

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(10);
            polylineOptions.color(Color.GREEN);
            polylineOptions.add(new LatLng(distributor_lat_x, distributor_long_y));
            polylineOptions.add(new LatLng(order_lat_x, order_long_y));
            shortest_polyline = mMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}