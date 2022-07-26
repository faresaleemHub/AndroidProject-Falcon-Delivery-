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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.S_D_ClientPendingReceiptsAdapter;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DistributorOrders extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    ArrayList<ClientOrderReceipt> orders_receipt_data_list;
    S_D_ClientPendingReceiptsAdapter adapter;
    Employee employee;
    CountDownTimer countDownTimer, countDownTimer2;
    CollapsingToolbarLayout ctl;
    FloatingActionButton fab;
    public static final String source = "DistributorOrders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_orders);

        selectSharedAccount();
        checkAuthenticity();

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        orders_receipt_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewReceivedOrdersDistributor);
        rv = findViewById(R.id.rv_previewReceivedOrders_distributor);
        ctl = findViewById(R.id.collabsing_toolbar_distributor);
        fab = findViewById(R.id.fab11);

        adapter = new S_D_ClientPendingReceiptsAdapter(
                DistributorOrders.this, orders_receipt_data_list, employee, source);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        if (getIntent().hasExtra("show_Received")) {
            ctl.setTitle("Received Orders");
            fab.setImageResource(R.drawable.ic_telegram);
//            Toast.makeText(DistributorOrders.this, "Received", Toast.LENGTH_SHORT).show();
        } else if (getIntent().hasExtra("show_Loaded")) {
            ctl.setTitle("Loaded Orders");
            fab.setImageResource(R.drawable.ic_load);
//            Toast.makeText(DistributorOrders.this, "Loaded", Toast.LENGTH_SHORT).show();
        } else if (getIntent().hasExtra("show_done")) {
            ctl.setTitle("Done Orders");
            fab.setImageResource(R.drawable.ic_done);
            fab.setColorFilter(Color.BLACK);
//            Toast.makeText(DistributorOrders.this, "Done", Toast.LENGTH_SHORT).show();
        }

        selectOrders();
    }

    private void selectOrders() {
        progressBar.setVisibility(View.VISIBLE);
        db.getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                if (cor1.getResponsibleDistributorID() != null) {
                    if (cor1.getResponsibleDistributorID().equals(employee.getId())) {
                        if (!cor1.isDistributorReceivedTheOrderFromTheStore()) {
                            if (getIntent().hasExtra("show_Received"))
                                orders_receipt_data_list.add(cor1);
                        } else if (!cor1.isDeliveredByDistributorToClient()) {
                            if (getIntent().hasExtra("show_Loaded"))
                                orders_receipt_data_list.add(cor1);
                        } else if (cor1.isDeliveredByDistributorToClient()) {
                            if (getIntent().hasExtra("show_done"))
                                orders_receipt_data_list.add(cor1);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {
                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                if (employee.getJobType().equals("Distributor")) {
                    if (cor1.getResponsibleDistributorID() != null) {
                        if (cor1.getResponsibleDistributorID().equals(employee.getId())) {
                            for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                                if (orders_receipt_data_list.get(i).getId().equals(snapshot.getValue(ClientOrderReceipt.class).getId())) {
                                    orders_receipt_data_list.remove(i);
                                    if (!cor1.isDistributorReceivedTheOrderFromTheStore()) {
                                        if (getIntent().hasExtra("show_Received"))
                                            orders_receipt_data_list.add(i, cor1);
                                    } else if (!cor1.isDeliveredByDistributorToClient()) {
                                        if (getIntent().hasExtra("show_Loaded"))
                                            orders_receipt_data_list.add(i, cor1);
                                    } else if (cor1.isDeliveredByDistributorToClient()) {
                                        if (getIntent().hasExtra("show_done"))
                                            orders_receipt_data_list.add(i, cor1);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

                if (orders_receipt_data_list.size() == 0) {
                    finish();
                }
            }


            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (employee.getJobType().equals("Distributor")) {
                    for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                        if (orders_receipt_data_list.get(i).getId().equals(snapshot.getValue(ClientOrderReceipt.class).getId())) {
                            orders_receipt_data_list.remove(i);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    if (orders_receipt_data_list.size() == 0) {
                        startActivity(new Intent(DistributorOrders.this, DistributorDashboard.class));
                        GroupFunctions.finishTransitionStyle(DistributorOrders.this);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (orders_receipt_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (orders_receipt_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(DistributorOrders.this, "No Orders Yet");
                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    public void TimerClose() {
        countDownTimer2 = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu23, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            GroupFunctions.finishTransitionStyle(this);
            GroupFunctions.closeWorkerServiceRequest();
        } else if (id == R.id.menu_monitor_distributors) {
            Intent i = new Intent(getApplicationContext(), MonitoringDistributorOrders.class);
            if (getIntent().hasExtra("show_Received")) {
                i.putExtra("show_Received", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(this);
            } else if (getIntent().hasExtra("show_Loaded")) {
                i.putExtra("show_Loaded", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(this);
            } else if (getIntent().hasExtra("show_done")) {
                i.putExtra("show_done", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(this);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
    }

    public void selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        employee = gson.fromJson(json, Employee.class);
    }

    public void checkAuthenticity() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            finish();
        } else if ((!employee.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}