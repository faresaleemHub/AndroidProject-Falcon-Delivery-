package com.aqsa.graduation_project_app.ui.adminSide.previewEntyties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewEmployeesAdapter;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Vehicle;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PreviewEmplloyees extends GroupFunctions {

    ProgressBar progressBar;
    FirebaseDatabase db;
    Spinner sp_jobType;
    RecyclerView rv;
    GridLayoutManager layoutManager;
    ArrayList<Employee> employees_data_list;
    CollapsingToolbarLayout ctl;
    PreviewEmployeesAdapter adapter;
    String jobType;
    CountDownTimer countDownTimer;
    FirebaseAuth auth;
    Employee employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_emplloyees__admin);

        this.initToolBar("", R.drawable.ic_back);
        db = FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        employee = selectSharedAccount();

        progressBar = findViewById(R.id.progressBar_previewEmployees);
        rv = findViewById(R.id.rv_previewEmployees);
        sp_jobType = findViewById(R.id.spinner_jobType2);
        ctl=findViewById(R.id.collabsing_toolbar);

        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rv.setLayoutManager(layoutManager);

        employees_data_list = new ArrayList<>();

        if (!getIntent().hasExtra("Order")) {//the default state
            adapter = new PreviewEmployeesAdapter(employees_data_list,
                    PreviewEmplloyees.this,employee);
            sp_jobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    jobType = sp_jobType.getSelectedItem().toString();
                    selectEmployees(jobType);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            selectEmployees("Supervisor");//default selected Item
            rv.setAdapter(adapter);

            //initialize item touch helper
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

        } else if (getIntent().hasExtra("Order")) {
            ctl.setTitle("Transfer");
            sp_jobType.setVisibility(View.GONE);
            findViewById(R.id.tv_jobtype).setVisibility(View.GONE);
            jobType = "Distributor";
//            if (jobType.equals("Distributor")) {
//                layoutManager = new GridLayoutManager(getApplicationContext(), 2);
//                rv.setLayoutManager(layoutManager);
//            }
            ClientOrderReceipt item = (ClientOrderReceipt) getIntent().getExtras().
                    getSerializable("Order");

            adapter = new PreviewEmployeesAdapter(employees_data_list
                    , PreviewEmplloyees.this, item);

            selectEmployees(jobType);
            rv.setAdapter(adapter);
        }
    }

    private void selectEmployees(String jobType) {
        employees_data_list.clear();
        adapter.notifyDataSetChanged();

        db.getReference().child("Employees").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Employee employee = snapshot.getValue(Employee.class);
                if (getIntent().hasExtra("Order")) {
                    if (employee.getJobType().equalsIgnoreCase(jobType) && employee.getvehicle_id() != null &&
                            !employee.getvehicle_id().equalsIgnoreCase("-1")){
                        employees_data_list.add(employee);
                        adapter.notifyDataSetChanged();
                    }
                } else if (employee.getJobType().equalsIgnoreCase(jobType)) {
                    employees_data_list.add(employee);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < employees_data_list.size(); i++) {
                    if (employees_data_list.get(i).getId().equals(snapshot.getValue(Employee.class).getId())) {
                        Employee employee = snapshot.getValue(Employee.class);
                        employees_data_list.remove(i);
                        employees_data_list.add(i, employee);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Employee.class).getJobType().equals("Supervisor")) {
                    for (int i = 0; i < employees_data_list.size(); i++) {
                        if (employees_data_list.get(i).getId().equals(snapshot.getValue(Employee.class).getId())) {
                            employees_data_list.remove(i);
                            adapter.notifyDataSetChanged();
                        }
                        if (employees_data_list.size() == 0) {
                            finish();
                            GroupFunctions.finishTransitionStyle(PreviewEmplloyees.this);
                        }
                    }
                } else if (snapshot.getValue(Employee.class).getJobType().equals("Distributor")) {
                    for (int i = 0; i < employees_data_list.size(); i++) {
                        if (employees_data_list.get(i).getId().equals(snapshot.getValue(Employee.class).getId())) {
                            employees_data_list.remove(i);
                            adapter.notifyDataSetChanged();
                        }
                        deleteDistributorVehicleLink(snapshot.getValue(Employee.class).getvehicle_id());
                        if (employees_data_list.size() == 0) {
                            finish();
                            GroupFunctions.finishTransitionStyle(PreviewEmplloyees.this);
                        }
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

        progressBar.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (employees_data_list.size()>0){
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (employees_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewEmplloyees.this,"No "+jobType+"s Yet");
                    if (getIntent().hasExtra("Order")) {
                        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                    } else
                        startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewEmplloyees.this);
        Employee employee1 = employees_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        auth.signOut();
                        auth.signInWithEmailAndPassword(employee1.getEmail(),employee1.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        auth.signInWithEmailAndPassword(employee.getEmail(),employee.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Employees").child(employee1.getId()).removeValue();
                                                Toast.makeText(PreviewEmplloyees.this, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        finish();
                        GroupFunctions.finishTransitionStyle(PreviewEmplloyees.this);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!getIntent().hasExtra("Order"))
            getMenuInflater().inflate(R.menu.popup_menu23, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            GroupFunctions.closeWorkerServiceRequest();
        } else if (id == R.id.menu_monitor_distributors) {
            startActivity(new Intent(getApplicationContext(), MonitoringDistributorsMap.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteDistributorVehicleLink(String vehicle_id) {

        FirebaseDatabase.getInstance().getReference().child("Vehicles").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (vehicle_id != null)
                    if (snapshot.getValue(Vehicle.class).getId().equalsIgnoreCase(vehicle_id)) {
                        Vehicle vehicle = snapshot.getValue(Vehicle.class);
                        vehicle.setDistributorID("-1");
                        FirebaseDatabase.getInstance().getReference().child("Vehicles").child(vehicle_id).setValue(vehicle);
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
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (getIntent().hasExtra("Order")) {
            startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        } else
            startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
    }

    public Employee selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        Employee employee = gson.fromJson(json, Employee.class);
        return employee;
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}