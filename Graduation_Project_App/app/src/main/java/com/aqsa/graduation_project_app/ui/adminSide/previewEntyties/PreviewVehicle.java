package com.aqsa.graduation_project_app.ui.adminSide.previewEntyties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewVehicleAdapter;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Vehicle;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PreviewVehicle extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    ArrayList<Vehicle> vehicles_data_list;
    PreviewVehicleAdapter adapter;
    CountDownTimer countDownTimer, countDownTimer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_cars__admin);

        this.initToolBar( "", R.drawable.ic_back);
        db = FirebaseDatabase.getInstance();

        vehicles_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewVehicle);
        rv = findViewById(R.id.rv_previewVehicles);

        adapter = new PreviewVehicleAdapter(vehicles_data_list,
                PreviewVehicle.this);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        selectVehicles();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //when item swipe
                showAlertDialog(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(rv);

    }


    private void selectVehicles() {
        db.getReference().child("Vehicles").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Vehicle vehicle = snapshot.getValue(Vehicle.class);
                vehicles_data_list.add(vehicle);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i=0;i<vehicles_data_list.size();i++) {
                    if (vehicles_data_list.get(i).getId().equals(snapshot.getValue(Vehicle.class).getId())){
                        vehicles_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                    if (vehicles_data_list.size() == 0) {
                        finish();
                        GroupFunctions.finishTransitionStyle(PreviewVehicle.this);
                    }
                }
                deleteDistributorVehicleLink(snapshot.getValue(Vehicle.class).getDistributorID());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(4500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (vehicles_data_list.size()>0){
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (vehicles_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewVehicle.this,"No Vehicles Yet");
                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewVehicle.this);
        Vehicle vehicle1 = vehicles_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Vehicles").child(vehicle1.getId()).removeValue();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        adapter.notifyItemChanged(position);
                    }
                })
                .setMessage("this item will be deleted");
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void deleteDistributorVehicleLink(String distributor_id) {
        FirebaseDatabase.getInstance().getReference().child("Employees").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(Employee.class).getId().equalsIgnoreCase(distributor_id)) {
                    Employee employee = snapshot.getValue(Employee.class);
                    employee.setvehicle_id("-1");
                    FirebaseDatabase.getInstance().getReference().child("Employees").child(distributor_id).setValue(employee);
                    if (vehicles_data_list.size() == 0) {
                        finish();
                        GroupFunctions.finishTransitionStyle(PreviewVehicle.this);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void TimerClose() {
        countDownTimer2 = new CountDownTimer(2500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
        startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
    }
    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}