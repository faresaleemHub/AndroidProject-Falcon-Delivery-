package com.aqsa.graduation_project_app.ui.adminSide.entityOperations;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Vehicle;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.distributorSide.DistributorDashboard;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmployeeOperations extends GroupFunctions {

    ProgressBar progressBar;
    FirebaseDatabase db;
    FirebaseAuth myAuth;
    Employee auth_employee, shared_employee;

    EditText ed_username, ed_phone, ed_email, ed_password, ed_salary, ed_dateregister,ed_num_orders_text;
    Spinner sp_country, sp_jobType;
    Button btn_save_employee;
    TextInputLayout input_date, input_email,input_num_orders,input_employee_salary;
    CollapsingToolbarLayout ctl;
    TextView tv_jobtype, tv_country;
    ImageView img_employee_profile;

    String username, phone, email, password, country, salary, jobType, vehicle_id, pastPhoneNumberInUpdate;
    FloatingActionButton fab;

    boolean DuplicateNumber = false;
    boolean state = true;
    int j = 0,numorders=0;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee__admin);

        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();
        auth_employee = selectSharedAccount();

        this.initToolBar("", R.drawable.ic_back);

        progressBar = findViewById(R.id.progressBar_addEmployee);
        btn_save_employee = findViewById(R.id.btn_save_employee);
        ed_username = findViewById(R.id.input_employee_username_text);
        ed_email = findViewById(R.id.input_employee_email_text);
        ed_password = findViewById(R.id.input_employee_Password_text);
        ed_phone = findViewById(R.id.input_employee_phone_number_text);
        ed_salary = findViewById(R.id.input_employee_salary_text);
        sp_country = findViewById(R.id.spinner_country_emloyee);
        sp_jobType = findViewById(R.id.spinner_jobType);
        ed_dateregister = findViewById(R.id.input_employee_date_register_text);
        ctl = findViewById(R.id.collabsing_toolbar_employee);
        input_date = findViewById(R.id.input_employee_date_register);
        input_email = findViewById(R.id.input_employee_email);
        tv_jobtype = findViewById(R.id.tv_jobtype);
        tv_country = findViewById(R.id.tv_country);
        fab = findViewById(R.id.fab1);
        img_employee_profile=findViewById(R.id.img_employee_profile);
        input_num_orders=findViewById(R.id.input_employee_num_orders);
        ed_num_orders_text=findViewById(R.id.tv_employee_num_orders_text);
        input_employee_salary=findViewById(R.id.input_employee_salary);

        sp_jobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 1) {
                    findViewById(R.id.tv_country).setVisibility(View.GONE);
                    sp_country.setVisibility(View.GONE);
                    vehicle_id = null;
                    country = null;
                } else {
                    findViewById(R.id.tv_country).setVisibility(View.VISIBLE);
                    sp_country.setVisibility(View.VISIBLE);
                    vehicle_id = "-1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_save_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = ed_username.getText().toString();
                email = ed_email.getText().toString();
                phone = ed_phone.getText().toString();
                password = ed_password.getText().toString();
                salary = ed_salary.getText().toString();
                country = sp_country.getSelectedItem().toString();
                jobType = sp_jobType.getSelectedItem().toString();
                TestCredentials("Add");
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_employee")) {
                input_date.setVisibility(View.VISIBLE);

                Intent i = getIntent();
                shared_employee = (Employee) i.getExtras().getSerializable("details_object_employee");

                fab.setImageResource(R.drawable.ic_person3);

                ctl.setTitle("Employee Details");

                if (getIntent().hasExtra("order")){
                    input_num_orders.setVisibility(View.VISIBLE);
                    getNumOrders(shared_employee,ed_num_orders_text);
                    input_email.setVisibility(View.GONE);
                    input_date.setVisibility(View.GONE);
                    findViewById(R.id.input_employee_salary).setVisibility(View.GONE);
                    findViewById(R.id.spinner_country_emloyee).setVisibility(View.GONE);
                    findViewById(R.id.input_employee_Password).setVisibility(View.GONE);
                    findViewById(R.id.tv_jobtype).setVisibility(View.GONE);
                }

                ed_username.setText(shared_employee.getUsername());
                ed_phone.setText(shared_employee.getPhone());
                ed_password.setText(shared_employee.getPassword());
                ed_email.setText(shared_employee.getEmail());
                ed_dateregister.setText(shared_employee.getDateRegister());
                sp_jobType.setVisibility(View.GONE);
                tv_jobtype.setText("Type : " + shared_employee.getJobType());
                ed_salary.setText(shared_employee.getSalary());

                if (shared_employee.getJobType().equalsIgnoreCase("Distributor")) {
                    tv_country.setText("Governorate : " + shared_employee.getCountry());
                } else {
                    //Supervisor, Admin
                    tv_country.setVisibility(View.GONE);
                }

                // for profile's image
                if (shared_employee.getProfileImgURI() != null) {
                    img_employee_profile.setVisibility(View.VISIBLE);
                    File localFile = new File(getFilesDir(), shared_employee.getProfileImgURI());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                                .child(shared_employee.getProfileImgURI()).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    img_employee_profile.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        img_employee_profile.setImageURI(Uri.fromFile(localFile));
                    }
                }

                sp_country.setVisibility(View.GONE);

                ed_username.setEnabled(false);
                ed_phone.setEnabled(false);
                ed_password.setEnabled(false);
                ed_dateregister.setEnabled(false);
                ed_salary.setEnabled(false);
                ed_email.setEnabled(false);

                btn_save_employee.setText("Done");
                btn_save_employee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_employee")) {

                input_date.setVisibility(View.GONE);
                input_email.setVisibility(View.GONE);

                Intent i = getIntent();
                shared_employee = (Employee) i.getExtras().getSerializable("update_object_employee");
                String type = shared_employee.getJobType();

                if (type.equals("Distributor")) {
                    tv_country.setVisibility(View.VISIBLE);
                    sp_country.setVisibility(View.VISIBLE);
                } else {
                    //Admin,Supervisor
                    tv_country.setVisibility(View.GONE);
                    sp_country.setVisibility(View.GONE);
                }

                if (auth_employee.getJobType().equals("Distributor")) {
                    input_employee_salary.setVisibility(View.GONE);
                    tv_country.setVisibility(View.GONE);
                    sp_country.setVisibility(View.GONE);
                    country=shared_employee.getCountry();
                }

                ctl.setTitle("Update Employee");
                btn_save_employee.setText("Update");

                ed_username.setText(shared_employee.getUsername());
                ed_phone.setText(shared_employee.getPhone());
                ed_password.setText(shared_employee.getPassword());
                ed_salary.setText(shared_employee.getSalary());

                if (auth_employee.getJobType().equalsIgnoreCase("Admin") &&
                        !shared_employee.getJobType().equals("Admin")) {
                    sp_jobType.setVisibility(View.VISIBLE);
                    tv_jobtype.setVisibility(View.VISIBLE);
                } else {
                    sp_jobType.setVisibility(View.GONE);
                    tv_jobtype.setVisibility(View.GONE);
                }

                sp_jobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {//supervisor
                            findViewById(R.id.tv_country).setVisibility(View.GONE);
                            sp_country.setVisibility(View.GONE);
                        } else if (position == 1) {
                            findViewById(R.id.tv_country).setVisibility(View.VISIBLE);
                            sp_country.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                pastPhoneNumberInUpdate = shared_employee.getPhone();

                btn_save_employee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        username = ed_username.getText().toString();
                        phone = ed_phone.getText().toString();
                        password = ed_password.getText().toString();
                        salary = ed_salary.getText().toString();

                        if (!auth_employee.getJobType().equals("Admin") || shared_employee.getJobType().equals("Admin"))
                            jobType = auth_employee.getJobType();
                        else {
                            jobType = sp_jobType.getSelectedItem().toString();
                        }

                        if (shared_employee.getJobType().equalsIgnoreCase("Distributor")
                                && jobType.equalsIgnoreCase("Distributor")) {
                            if (auth_employee.getJobType().equals("Admin"))
                                country = sp_country.getSelectedItem().toString();
                            vehicle_id = shared_employee.getvehicle_id();
                        } else if (shared_employee.getJobType().equalsIgnoreCase("Distributor")
                                && jobType.equalsIgnoreCase("Supervisor")) {
                            vehicle_id = null;
                            country = null;
                            updateVehicles(shared_employee.getvehicle_id());
                        } else if (shared_employee.getJobType().equalsIgnoreCase("Supervisor")
                                && jobType.equalsIgnoreCase("Supervisor")) {
                            vehicle_id = null;
                            country = null;
                        } else if (shared_employee.getJobType().equalsIgnoreCase("Supervisor")
                                && jobType.equalsIgnoreCase("Distributor")) {
                            vehicle_id = "-1";
                            country = sp_country.getSelectedItem().toString();
                        } else if (shared_employee.getJobType().equalsIgnoreCase("Admin")) {
                            jobType = "Admin";
                            country = null;
                            vehicle_id = null;
                        }
                        TestCredentials("Update");
                    }
                });
            }
    }

    private void TestCredentials(String ActionState) {

        progressBar.setVisibility(View.VISIBLE);

        //conditions
        //A- username
        //1- check f the field is empty
        if (username.isEmpty()) {
            Toast.makeText(EmployeeOperations.this, "fill the empty username space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2- to check if the username contains numbers or any other symbols
        String test_username = username.toLowerCase();
        state = true;
        j = 0;
        while (state && j < username.length()) {
            if ((test_username.charAt(j) < (char) 97 &&
                    test_username.charAt(j) != (char) 32)
                    || test_username.charAt(j) > (char) 122)
                state = false;
            j++;
        }
        if (!state) {
            Toast.makeText(EmployeeOperations.this,
                    "the name must contain letters only in English", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        //B-email
        if (ActionState.equalsIgnoreCase("Add")) {
            if (email.isEmpty()) {
                Toast.makeText(EmployeeOperations.this, "fill the empty email space", Toast.LENGTH_SHORT)
                        .show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
        }

        //C-phone
        //1-
        if (phone.isEmpty()) {
            Toast.makeText(EmployeeOperations.this, "fill the empty phone number space",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        String startNumber = "" + phone.charAt(0) + phone.charAt(1) + phone.charAt(2);
        if (!startNumber.equals("059") && !startNumber.equals("056")) {
            Toast.makeText(EmployeeOperations.this, "the number must begin with 059 or 056", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-if Phone number hase been used
        db.getReference().child("Employees").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (ActionState.equals("Update")) {
                        if (snapshot1.getValue(Employee.class).getPhone().equals(phone) &&
                                !snapshot1.getValue(Employee.class).getPhone().equals(pastPhoneNumberInUpdate)) {
                            DuplicateNumber = true;
                            break;
                        }
                    } else {
                        if (snapshot1.getValue(Employee.class).getPhone().equals(phone)) {
                            DuplicateNumber = true;
                            break;
                        }
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
                if (DuplicateNumber == true) {
                    Toast.makeText(getApplicationContext(), "this phone number has been used",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    DuplicateNumber = false;
                    state = true;
                    j = 0;
                    countDownTimer.cancel();
                    return;
                }
            }

            @Override
            public void onFinish() {
                if (DuplicateNumber == true) {
                    Toast.makeText(getApplicationContext(), "this phone number has been used",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    DuplicateNumber = false;
                    state = true;
                    j = 0;
                    return;
                } else {
                    continueChecking(ActionState);
                }
            }
        }.start();

    }

    private void createNewAccount() {
        myAuth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        completion(task);
                    }
                });
    }

    public void continueChecking(String ActionState) {

        //password
        if (!ActionState.equalsIgnoreCase("Update")) {
            //1-
            if (password.isEmpty()) {
                Toast.makeText(EmployeeOperations.this, "fill the empty password space", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
        }

        //2-
        if (password.length() < 8) {
            Toast.makeText(EmployeeOperations.this,
                    "The Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-
        String pass = password;
        state = false;
        for (int i = 0; i < pass.length() - 1; i++)
            for (j = i + 1; j < pass.length(); j++)
                if (pass.charAt(i) == pass.charAt(j)) {
                    state = true;
                    break;
                }
        if (state == true) {
            Toast.makeText(EmployeeOperations.this, "for Secret issues\n" +
                    "The password should not contain duplicate characters", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //4-
        j = 0;
        state = true;
        while (state && j < pass.length()) {
            if (pass.charAt(j) < (char) 97 && pass.charAt(j) != (char) 32 ||
                    pass.charAt(j) > (char) 121)
                state = false;
            j++;
        }
        if (state) {
            Toast.makeText(EmployeeOperations.this, "for Secret issues\n" +
                    "the password must contain at least 1 symbol", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (salary.isEmpty()) {
            Toast.makeText(this, "fill the empty salary space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (ActionState.equalsIgnoreCase("Update")) {
            shared_employee.setPassword(password);
            shared_employee.setPhone(phone);
            shared_employee.setUsername(username);
            shared_employee.setSalary(salary);
            shared_employee.setJobType(jobType);
            shared_employee.setCountry(country);
            shared_employee.setvehicle_id(vehicle_id);

            db.getReference().child("Employees")
                    .child(shared_employee.getId()).setValue(shared_employee).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EmployeeOperations.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
            finish();
            GroupFunctions.finishTransitionStyle(EmployeeOperations.this);
        }else
        if (ActionState.equalsIgnoreCase("Add"))
            createNewAccount();

    }

    public void completion(Task<AuthResult> task) {
        if (task.isSuccessful()) {

            //save the user into firebase
            Employee employee = new Employee();
            employee.setEmail(email);
            employee.setPassword(password);
            employee.setPhone(phone);
            employee.setUsername(username);
            employee.setSalary(salary);
            employee.setJobType(jobType);
            employee.setCountry(country);
            employee.setvehicle_id(vehicle_id);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String date = sdf.format(new Date());
            employee.setDateRegister(date);

            String employee_Id = db.getReference().child("Employees").push().getKey();
            employee.setId(employee_Id);
            db.getReference().child("Employees").child(employee_Id).setValue(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SendVerification();
                }
            });
        } else {
            Toast.makeText(EmployeeOperations.this, "something wrong ! -> " +
                    task.getException().toString(), Toast.LENGTH_LONG).
                    show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void SendVerification() {

        myAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EmployeeOperations.this,
                            "We Send a Verification Message For This Email", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EmployeeOperations.this,
                            "failed with sending verification", Toast.LENGTH_SHORT).show();
                }

                //change the login account to Admin
                myAuth.signOut();
                myAuth.signInWithEmailAndPassword(auth_employee.getEmail(),
                        auth_employee.getPassword())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(EmployeeOperations.this, "" +
                                        e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //بالطبع ما رح يعمل قصة انه يبعت تاكيد حساب غير الآدمن عشان هيك آدمن وليس "موظف"
                        setSharedAccount(auth_employee);
                        finish();
                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (selectSharedAccount().getJobType().equalsIgnoreCase("Supervisor"))
            startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        else if (selectSharedAccount().getJobType().equalsIgnoreCase("Distributor"))
            startActivity(new Intent(getApplicationContext(), DistributorDashboard.class));
        else
            startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
    }

    public void updateVehicles(String vehicle_id) {
        db.getReference().child("Vehicles").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(Vehicle.class).getId().equalsIgnoreCase(vehicle_id)) {
                    Vehicle vehicle = snapshot.getValue(Vehicle.class);
                    vehicle.setDistributorID("-1");
                    db.getReference().child("Vehicles").child(vehicle_id).setValue(vehicle);
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

    public Employee selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        Employee employee = gson.fromJson(json, Employee.class);
        return employee;
    }

    private void getNumOrders(Employee employee, TextView tv_numOrders) {
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ClientOrderReceipt receipt = snapshot1.getValue(ClientOrderReceipt.class);
                    if (receipt.getResponsibleDistributorID() != null) {
                        if (receipt.getResponsibleDistributorID().equals(employee.getId())) {
                            if (!receipt.isDeliveredByDistributorToClient()) {
                                numorders++;
                            }
                        }
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
                if (numorders!=0){
                    tv_numOrders.setText(numorders + "");
                    numorders = 0;
                    countDownTimer.cancel();
                }

            }

            @Override
            public void onFinish() {
                tv_numOrders.setText(numorders + "");
                numorders = 0;
            }
        }.start();
    }

    public void setSharedAccount(Employee employee) {
        SharedPreferences sp = getSharedPreferences("LoginCredentials"
                , MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(employee);
        editor.putString(LoginActivity.Shared_Login_Object, json);
        editor.putString("Type", "Employee");
        editor.commit();
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }

}