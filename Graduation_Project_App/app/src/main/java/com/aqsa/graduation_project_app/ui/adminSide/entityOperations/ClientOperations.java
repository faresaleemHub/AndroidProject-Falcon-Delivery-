package com.aqsa.graduation_project_app.ui.adminSide.entityOperations;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.distributorSide.DistributorOrders;
import com.aqsa.graduation_project_app.ui.distributorSide.MonitoringDistributorOrders;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

public class ClientOperations extends GroupFunctions {

    ProgressBar progressBar;
    FirebaseDatabase db;
    FirebaseAuth myAuth;
    Client client;
    Employee employee;
    FloatingActionButton fab;
    EditText ed_username, ed_phone, ed_email, ed_password, ed_marketName, ed_dateregister;
    ImageView img_client_profile;
    Button btn_save_client;
    CollapsingToolbarLayout ctl;
    TextInputLayout input_date, input_email, input_password;
    String username, phone, email, password, marketName, pastPhoneNumberInUpdate;
    boolean DuplicateNumber = false;
    boolean state = true;
    int j = 0;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client__admin);

        this.initToolBar("", R.drawable.ic_back);
        employee = selectSharedAccount();

        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar_addClient);
        fab = findViewById(R.id.fab2);
        ed_username = findViewById(R.id.input_client_username_text);
        ed_email = findViewById(R.id.input_client_email_text);
        ed_password = findViewById(R.id.input_client_Password_text);
        ed_phone = findViewById(R.id.input_client_phone_number_text);
        ed_marketName = findViewById(R.id.input_client_marketName_text);
        btn_save_client = findViewById(R.id.btn_save_client);
        ed_dateregister = findViewById(R.id.input_client_date_register_text);
        ctl = findViewById(R.id.collabsing_toolbar_client);
        input_date = findViewById(R.id.input_client_date_register);
        input_email = findViewById(R.id.input_client_email);
        input_password = findViewById(R.id.input_client_Password);
        img_client_profile = findViewById(R.id.img_client_profile);

        btn_save_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = ed_username.getText().toString();
                email = ed_email.getText().toString();
                phone = ed_phone.getText().toString();
                password = ed_password.getText().toString();
                marketName = ed_marketName.getText().toString();
                TestCredentials("Add");
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_client")) {
                Intent i = getIntent();
                client = (Client) i.getExtras().getSerializable("details_object_client");

                if (getIntent().hasExtra("source"))
                    if (getIntent().getStringExtra("source").equals(MonitoringDistributorOrders.source)
                            || getIntent().getStringExtra("source").equals(DistributorOrders.source)
                            || getIntent().getStringExtra("source").equals(PreviewOrders.source)) {

                        input_email.setVisibility(View.GONE);
                        input_password.setVisibility(View.GONE);
                    }

                fab.setImageResource(R.drawable.ic_person3);

                ed_username.setText(client.getUsername());
                ed_phone.setText(client.getPhone());
                ed_marketName.setText(client.getMarketName());
                ed_email.setText(client.getEmail());
                ed_password.setText(client.getPassword());
                ed_dateregister.setText(client.getDateRegister());


                // for profile's image
                if (client.getProfileImgURI() != null) {
                    img_client_profile.setVisibility(View.VISIBLE);
                    File localFile = new File(getFilesDir(), client.getProfileImgURI());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                                .child(client.getProfileImgURI()).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    img_client_profile.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        img_client_profile.setImageURI(Uri.fromFile(localFile));
                    }
                }
                //
                ctl.setTitle("Client Details");
                btn_save_client.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                ed_username.setEnabled(false);
                ed_phone.setEnabled(false);
                ed_dateregister.setEnabled(false);
                ed_password.setEnabled(false);
                ed_marketName.setEnabled(false);
                ed_email.setEnabled(false);
                btn_save_client.setText("Done");
            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_client")) {
                input_date.setVisibility(View.GONE);
                input_email.setVisibility(View.GONE);

                Intent i = getIntent();
                client = (Client) i.getExtras().getSerializable("update_object_client");
                ed_username.setText(client.getUsername());
                ed_phone.setText(client.getPhone());
                ed_password.setText(client.getPassword());
                ed_marketName.setText(client.getMarketName());
                btn_save_client.setText("Update");
                ctl.setTitle("Update Client");
                pastPhoneNumberInUpdate = client.getPhone();

                btn_save_client.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        username = ed_username.getText().toString();
                        phone = ed_phone.getText().toString();
                        password = ed_password.getText().toString();
                        marketName = ed_marketName.getText().toString();
                        email = client.getEmail();
                        TestCredentials("Update");
                    }
                });
            }
    }

    private void TestCredentials(String ActionState) {

        progressBar.setVisibility(View.VISIBLE);
        state = true;
        j = 0;
        //conditions
        //A- username
        //1- check f the field is empty
        if (username.isEmpty()) {
            Toast.makeText(ClientOperations.this, "fill the empty username space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2- to check if the username contains numbers or any other symbols
        String test_username = username.toLowerCase();
        while (state && j < username.length()) {
            if ((test_username.charAt(j) < (char) 97 &&
                    test_username.charAt(j) != (char) 32)
                    || test_username.charAt(j) > (char) 122)
                state = false;
            j++;
        }
        if (!state) {
            Toast.makeText(ClientOperations.this,
                    "the name must contain letters only in English", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //B-email
        if (!ActionState.equalsIgnoreCase("Update")) {
            if (email.isEmpty()) {
                Toast.makeText(ClientOperations.this, "fill the empty email space", Toast.LENGTH_SHORT)
                        .show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
        }

        //C-phone
        //1-
        if (phone.isEmpty()) {
            Toast.makeText(ClientOperations.this, "fill the empty phone number space",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        //2-
        String startNumber = "" + phone.charAt(0) + phone.charAt(1) + phone.charAt(2);
        if (!startNumber.equals("059") && !startNumber.equals("056")) {
            Toast.makeText(ClientOperations.this, "the number must begin with 059 or 056", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-if Phone number hase been used
        db.getReference().child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (ActionState.equals("Update")) {
                        if (snapshot1.getValue(Client.class).getPhone().equals(phone) &&
                                !snapshot1.getValue(Client.class).getPhone().equals(pastPhoneNumberInUpdate)) {
                            DuplicateNumber = true;
                            break;
                        }
                    } else {
                        if (snapshot1.getValue(Client.class).getPhone().equals(phone)) {
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

        countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (DuplicateNumber == true) {
                    Toast.makeText(ClientOperations.this, "this phone number has been used",
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
                    Toast.makeText(ClientOperations.this, "this phone number has been used",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    DuplicateNumber = false;
                    state = true;
                    j = 0;
                    return;
                } else {
                    ContinueChecking(ActionState);
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

    private void completion(Task<AuthResult> task) {
        if (task.isSuccessful()) {

            //save the user into firebase
            Client client = new Client();
            client.setEmail(email);
            client.setPassword(password);
            client.setPhone(phone);
            client.setUsername(username);
            client.setMarketName(marketName);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String date = sdf.format(new Date());
            client.setDateRegister(date);

            String client_Id = db.getReference().child("Clients").push().getKey();
            client.setId(client_Id);
            db.getReference().child("Clients").child(client_Id).setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SendVerification();
                }
            });
        } else {
            Toast.makeText(ClientOperations.this, "something wrong ! -> " +
                    task.getException().toString(), Toast.LENGTH_LONG).
                    show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void SendVerification() {
        myAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ClientOperations.this,
                            "We Send a Verification Message For This Email", Toast.LENGTH_LONG).show();
                    myAuth.signOut();
                    myAuth.signInWithEmailAndPassword(employee.getEmail(), employee.getPassword())
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ClientOperations.this, "" +
                                            e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            setSharedAccount(employee);
                            finish();
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ClientOperations.this,
                            "failed with sending verification", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().hasExtra("source")) {
            finish();
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

    public void ContinueChecking(String ActionState) {
        //password
        //1-
        if (password.isEmpty()) {
            Toast.makeText(ClientOperations.this, "fill the empty password space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        if (password.length() < 8) {
            Toast.makeText(ClientOperations.this,
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
            Toast.makeText(ClientOperations.this, "for Secret issues\n" +
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
            Toast.makeText(ClientOperations.this, "for Secret issues\n" +
                    "the password must contain at least 1 symbol", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (marketName.length() == 0) {
            Toast.makeText(ClientOperations.this, "fill the empty market Name space", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        //Update
        if (ActionState.equalsIgnoreCase("Update")) {
            client.setUsername(username);
            client.setPhone(phone);
            client.setPassword(password);
            client.setMarketName(marketName);

            db.getReference().child("Clients")
                    .child(client.getId()).setValue(client).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.INVISIBLE);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ClientOperations.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        } else if (ActionState.equalsIgnoreCase("Add")) {
            createNewAccount();
        }
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}