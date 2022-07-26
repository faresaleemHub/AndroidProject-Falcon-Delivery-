package com.aqsa.graduation_project_app.ui.adminSide.entityOperations;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewDistributorAdapter;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Vehicle;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VehicleOperations extends GroupFunctions {

    FirebaseDatabase db;
    Vehicle vehicle;
    TextInputLayout input_date;

    ProgressBar progressBar;
    TextView tv_tv_DistributorName,tv_DistributorName, tv_DistributorID;
    EditText ed_name, ed_vehicleNumber, ed_dateregister;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    Button btn_save_vehicle;
    CollapsingToolbarLayout ctl;
    TextInputLayout input_vehicle_number;

    String name, VNumber, DistributorName, DistributorID, oldDistributorID;
    boolean DuplicateNumber=false;

    ArrayList<Employee> distributors_data_list;
    PreviewDistributorAdapter adapter;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle__admin);

        this.initToolBar("",R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();
        vehicle = new Vehicle();

        distributors_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_addVehicle);
        rv = findViewById(R.id.rv_distributors);
        ed_name = findViewById(R.id.input_vehicle_name_text);
        ed_vehicleNumber = findViewById(R.id.input_vehicle_number_text);
        tv_DistributorID = findViewById(R.id.tv_DistributorID_V);
        tv_tv_DistributorName = findViewById(R.id.tv_tv_DistributorName);
        tv_DistributorName=findViewById(R.id.tv_DistributorName);
        btn_save_vehicle = findViewById(R.id.byn_save_vehicle);
        input_date = findViewById(R.id.input_vehicle_date_register);
        ed_dateregister = findViewById(R.id.input_vehicle_date_register_text);
        ctl = findViewById(R.id.collabsing_toolbar_vehicle);
        input_vehicle_number=findViewById(R.id.input_vehicle_number);

        adapter = new PreviewDistributorAdapter(distributors_data_list,
                VehicleOperations.this);
        layoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        if (getIntent().hasExtra("update_object_vehicle"))
            selectDistributors("Update");
        else if (!getIntent().hasExtra("details_object_vehicle"))
            selectDistributors("Add");

        btn_save_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                name = ed_name.getText().toString();
                DistributorName = tv_DistributorName.getText().toString();
                VNumber = ed_vehicleNumber.getText().toString();
                DistributorID = tv_DistributorID.getText().toString();
                TestValues("Add");
            }
        });

        //for view details
        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_vehicle")) {
                input_date.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);

                Intent i = getIntent();
                vehicle = (Vehicle) i.getExtras().getSerializable("details_object_vehicle");

                ctl.setTitle("Vehicle Details");

                ed_name.setText(vehicle.getName());
                ed_vehicleNumber.setText(vehicle.getVNumber());
                selectDistributorName(vehicle.getDistributorID());
                ed_dateregister.setText(vehicle.getDateRegister());


                ed_name.setEnabled(false);
                ed_vehicleNumber.setEnabled(false);
                ed_dateregister.setEnabled(false);

                btn_save_vehicle.setText("Done");
                btn_save_vehicle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }


        //for updating
        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_vehicle")) {
                input_date.setVisibility(View.GONE);

                Intent i = getIntent();
                vehicle = (Vehicle) i.getExtras().getSerializable("update_object_vehicle");

                oldDistributorID = vehicle.getDistributorID();

                ctl.setTitle("Update Vehicle");

                ed_name.setText(vehicle.getName());
                selectDistributorName(vehicle.getDistributorID());

                tv_DistributorID.setText(vehicle.getDistributorID());
                ed_vehicleNumber.setText(vehicle.getVNumber());
                input_vehicle_number.setVisibility(View.GONE);//car ID not Allowed to be updated

                btn_save_vehicle.setText("Update");
                btn_save_vehicle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        name = ed_name.getText().toString();
                        DistributorName = tv_DistributorName.getText().toString();
                        VNumber = ed_vehicleNumber.getText().toString();
                        DistributorID = tv_DistributorID.getText().toString();
                        TestValues("Update");
                    }
                });
            }
    }

    private void selectDistributors(String ActionState) {
        //select Available Distributors from Firebase
        db.getReference().child("Employees").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Employee employee = snapshot.getValue(Employee.class);
                if (employee.getJobType().equals("Distributor") &&
                        employee.getvehicle_id().equals("-1")) {
                    distributors_data_list.add(employee);
                    adapter.notifyDataSetChanged();
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
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (distributors_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (distributors_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(VehicleOperations.this,"No Available Free Distributors");
                    if (ActionState.equals("Add")){
                        tv_tv_DistributorName.setVisibility(View.GONE);
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void TestValues(String ActionState) {

        if (name.isEmpty()) {
            Toast.makeText(this, "fill the empty name space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (VNumber.isEmpty()) {
            Toast.makeText(this, "fill the empty Vehicle Number space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        //***
        db.getReference().child("Vehicles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        if (snapshot1.getValue(Vehicle.class).getVNumber().equals(VNumber)) {
                            DuplicateNumber = true;
                            break;
                        }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer=new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (ActionState.equals("Add")) {
                    if (DuplicateNumber == true) {
                        Toast.makeText(getApplicationContext(), "this Vehicle number has been used",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        DuplicateNumber = false;
                        countDownTimer.cancel();
                        return;
                    }
                }
            }

            @Override
            public void onFinish() {
                if (ActionState.equals("Add")) {
                    if (DuplicateNumber == true) {
                        Toast.makeText(getApplicationContext(), "this Vehicle number has been used",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        DuplicateNumber = false;
                        return;
                    } else {
                        continueChecking(ActionState);
                    }
                }else{
                    continueChecking(ActionState);
                }
            }
        }.start();
    }

    public void continueChecking(String ActionState){

        vehicle.setDistributorID(DistributorID);
        vehicle.setVNumber(VNumber);
        vehicle.setName(name);

        if (ActionState.equalsIgnoreCase("Add")) {
            //create vehicle object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String date = sdf.format(new Date());
            vehicle.setDateRegister(date);

            //to save the car in firebase
            String v_Id = db.getReference().child("Vehicles").push().getKey();
            vehicle.setId(v_Id);
            db.getReference().child("Vehicles").child(v_Id).setValue(vehicle).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        UpdateDistributors("Add");
                        finish();
                    } else
                        Toast.makeText(VehicleOperations.this, "something wrong ! -> " +
                                task.getException().toString(), Toast.LENGTH_LONG).
                                show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
            });
        } else if (ActionState.equalsIgnoreCase("Update")) {
            db.getReference().child("Vehicles")
                    .child(vehicle.getId()).setValue(vehicle).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.INVISIBLE);
                    UpdateDistributors("Update");
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VehicleOperations.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    //to bind the distributor with his car
    private void UpdateDistributors(String ActionState) {

        db.getReference().child("Employees").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Employee employee = snapshot.getValue(Employee.class);
                if (employee.getId().equals(DistributorID)) {
                    employee.setvehicle_id(vehicle.getId());
                    FirebaseDatabase.getInstance().getReference()
                            .child("Employees").child(DistributorID).setValue(employee);
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
        if (ActionState.equalsIgnoreCase("Update") &&
                !oldDistributorID.equalsIgnoreCase(DistributorID)) {
            db.getReference().child("Employees").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Employee employee = snapshot.getValue(Employee.class);
                    if (employee.getId().equals(oldDistributorID)) {
                        employee.setvehicle_id("-1");
                        FirebaseDatabase.getInstance().getReference()
                                .child("Employees").child(oldDistributorID).setValue(employee);
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

    }

    @Override
    public void onBackPressed() {
        countDownTimer.cancel();
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
    }

    public void selectDistributorName(String D_ID){
        db.getReference().child("Employees").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()) {
                    Employee  employee = snapshot1.getValue(Employee.class);
                    if (D_ID.equals(employee.getId())) {
                        tv_DistributorName.setText(employee.getUsername());
                    }
                }

                if (tv_DistributorName.getText().toString().length()==0){
                    tv_DistributorName.setText("No one");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}