package com.aqsa.graduation_project_app.ui.subscribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.Services.MyWorker;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.clientSide.ClientMainActivity;
import com.aqsa.graduation_project_app.ui.distributorSide.DistributorDashboard;
import com.aqsa.graduation_project_app.ui.merchantSide.MerchantDashboard;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_login;
    TextView tv_register;
    EditText ed_email, ed_password;
    String email, password;
    ProgressBar progressBar;
    FirebaseDatabase db;
    FirebaseAuth myAuth;

    public static final String Shared_Login_Object = "MyObject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progressBar);
        tv_register = findViewById(R.id.tv_login_rigisterNow);
        btn_login = findViewById(R.id.btn_Login);
        ed_email = findViewById(R.id.ed_login_email);
        ed_password = findViewById(R.id.ed_login_password);
        tv_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (myAuth.getCurrentUser() != null) {
            if (isInternetConnected()) {
                if (myAuth.getCurrentUser().isEmailVerified()) {
                    progressBar.setVisibility(View.VISIBLE);
                    decideEmailOwner();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_login_rigisterNow:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                GroupFunctions.openTransitionStyle(this);
                break;

            case R.id.btn_Login:
                if (isInternetConnected()) {
                    progressBar.setVisibility(View.VISIBLE);
                    email = ed_email.getText().toString();
                    password = ed_password.getText().toString();

                    if (!email.isEmpty() && !password.isEmpty()) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).
                                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            verify();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this,"something is wrong", Toast.LENGTH_SHORT).show();

                                            YoYo.with(Techniques.Pulse)
                                                    .duration(1000)
                                                    .repeat(1)
                                                    .playOn(tv_register);
                                        }
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "fill the empty spaces", Toast.LENGTH_SHORT).show();

                        if (email.isEmpty())
                            YoYo.with(Techniques.Shake)
                                    .duration(2000)
                                    .repeat(1)
                                    .playOn(ed_email);
                        if (password.isEmpty())
                            YoYo.with(Techniques.Shake)
                                    .duration(2000)
                                    .repeat(1)
                                    .playOn(ed_password);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    public void verify() {
        if (!myAuth.getCurrentUser().isEmailVerified()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "please verify your account ...",
                    Toast.LENGTH_SHORT).show();
        } else {
            decideEmailOwner();
        }
    }

    public void decideEmailOwner() {
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Merchant merchant = snapshot1.getValue(Merchant.class);
                    if (merchant.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        SharedPreferences sp = getSharedPreferences("LoginCredentials"
                                , MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(merchant);
                        editor.putString(Shared_Login_Object, json);
                        editor.putString("Type", "Merchant");
                        editor.commit();
                        startActivity(new Intent(LoginActivity.this,
                                MerchantDashboard.class));
                        GroupFunctions.loginTransitionStyle(LoginActivity.this);
                        progressBar.setVisibility(View.GONE);

                        //trigger the notifications
                        sendWorkerServiceRequest();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        db.getReference().child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Client client = snapshot1.getValue(Client.class);
                    if (client.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

                        //share the Account along the application
                        SharedPreferences sp = getSharedPreferences("LoginCredentials"
                                , MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(client);
                        editor.putString(Shared_Login_Object, json);
                        editor.putString("Type", "Client");
                        editor.commit();

                        startActivity(new Intent(LoginActivity.this,
                                ClientMainActivity.class));
                        GroupFunctions.loginTransitionStyle(LoginActivity.this);
                        progressBar.setVisibility(View.GONE);

                        //trigger the notifications
                        sendWorkerServiceRequest();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        db.getReference().child("Employees").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Employee employee = snapshot1.getValue(Employee.class);

                    if (employee.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

                        //share the Account along the application
                        SharedPreferences sp = getSharedPreferences("LoginCredentials"
                                , MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(employee);
                        editor.putString(Shared_Login_Object, json);
                        editor.putString("Type", "Employee");
                        editor.commit();

                        if (employee.getJobType().equals("Admin")) {
                            startActivity(new Intent(LoginActivity.this,
                                    AdminDashboard.class));
                            GroupFunctions.loginTransitionStyle(LoginActivity.this);
                        }
                        else if (employee.getJobType().equals("Supervisor")) {
                            startActivity(new Intent(LoginActivity.this,
                                    SupervsorDashboard.class));
                            GroupFunctions.loginTransitionStyle(LoginActivity.this);
                        }
                        else if (employee.getJobType().equalsIgnoreCase("Distributor")) {
                            startActivity(new Intent(LoginActivity.this,
                                    DistributorDashboard.class));
                            Toast.makeText(LoginActivity.this, "turn ON GPS service", Toast.LENGTH_SHORT).show();
                            GroupFunctions.loginTransitionStyle(LoginActivity.this);
                        }
                        progressBar.setVisibility(View.GONE);

                        //trigger the notifications
                        sendWorkerServiceRequest();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        // ...
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void sendWorkerServiceRequest(){
        // to run my service
        Constraints constraints=new
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest.Builder builder=new OneTimeWorkRequest.Builder(MyWorker.class);
        builder.setConstraints(constraints);
        OneTimeWorkRequest request=builder.build();
        WorkManager.getInstance(getApplicationContext()).enqueue(request);
    }
}