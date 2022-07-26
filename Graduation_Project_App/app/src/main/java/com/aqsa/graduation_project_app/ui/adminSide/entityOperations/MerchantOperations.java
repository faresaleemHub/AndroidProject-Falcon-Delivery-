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
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
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

public class MerchantOperations extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    EditText ed_name, ed_PhoneNumber, ed_companyName, ed_dateregister, ed_email, ed_password;
    Button btn_save_merchant;
    CollapsingToolbarLayout ctl;
    TextInputLayout Input_date;
    FloatingActionButton fab;
    String name, phoneNumber, companyName, email, password, pastPhoneNumberInUpdate;
    Merchant merchant;
    TextInputLayout input_email, input_password, input_date;
    FirebaseAuth myAuth;
    Employee employee;
    boolean DuplicateNumber = false;
    boolean state = true;
    int j = 0;
    CountDownTimer countDownTimer;
    ImageView img_merchant_profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marchent__admin);

        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        this.initToolBar("", R.drawable.ic_back);

        selectSharedEmployeeAccount();

        progressBar = findViewById(R.id.progressBar_addMerchant);
        btn_save_merchant = findViewById(R.id.btn_save_merchant);
        ed_name = findViewById(R.id.input_merchant_name_text);
        ed_PhoneNumber = findViewById(R.id.input_merchant_phone_number_text);
        ed_companyName = findViewById(R.id.input_merchant_company_name_text);
        ed_dateregister = findViewById(R.id.input_merchant_date_register_text);
        ed_email = findViewById(R.id.input_merchant_email_text);
        ctl = findViewById(R.id.collabsing_toolbar_merchant);
        Input_date = findViewById(R.id.input_merchant_date_register);
        input_email = findViewById(R.id.input_merchant_email);
        fab = findViewById(R.id.fab4);
        ed_password = findViewById(R.id.input_merchant_password_text);
        input_password = findViewById(R.id.input_merchant_password);
        input_date = findViewById(R.id.input_merchant_date_register);
        img_merchant_profile=findViewById(R.id.img_merchant_profile);

        merchant = new Merchant();

        btn_save_merchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = ed_name.getText().toString();
                phoneNumber = ed_PhoneNumber.getText().toString();
                companyName = ed_companyName.getText().toString();
                password = ed_password.getText().toString();
                email = ed_email.getText().toString();
                TestValues("Add");
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_merchant")) {
                if (getIntent().hasExtra("Type"))
                    if (getIntent().getStringExtra("Type").equals("Supervisor")) {
                        input_email.setVisibility(View.GONE);
                        input_password.setVisibility(View.GONE);
                        input_date.setVisibility(View.GONE);
                    }

                Input_date.setVisibility(View.VISIBLE);
                Intent i = getIntent();
                merchant = (Merchant) i.getExtras().getSerializable("details_object_merchant");
                fab.setImageResource(R.drawable.ic_person3);

                ed_name.setText(merchant.getName());
                ed_companyName.setText(merchant.getCompanyName());
                ed_PhoneNumber.setText(merchant.getPhoneNumber());
                ed_dateregister.setText(merchant.getDateRegister());
                ed_email.setText(merchant.getEmail());
                ed_password.setText(merchant.getPassword());
                ctl.setTitle("Merchant Details");

                // for profile's image
                if (merchant.getProfileImgURI() != null) {
                    img_merchant_profile.setVisibility(View.VISIBLE);
                    File localFile = new File(getFilesDir(), merchant.getProfileImgURI());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                                .child(merchant.getProfileImgURI()).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    img_merchant_profile.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        img_merchant_profile.setImageURI(Uri.fromFile(localFile));
                    }
                }
                //

                btn_save_merchant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                ed_name.setEnabled(false);
                ed_companyName.setEnabled(false);
                ed_PhoneNumber.setEnabled(false);
                ed_dateregister.setEnabled(false);
                ed_email.setEnabled(false);
                ed_password.setEnabled(false);
                btn_save_merchant.setText("Done");
            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_merchant")) {

                ctl.setTitle("Update Details");
                Input_date.setVisibility(View.GONE);
                input_email.setVisibility(View.GONE);

                Intent i = getIntent();
                merchant = (Merchant) i.getExtras().getSerializable("update_object_merchant");
                ed_name.setText(merchant.getName());
                ed_companyName.setText(merchant.getCompanyName());
                ed_PhoneNumber.setText(merchant.getPhoneNumber());
                pastPhoneNumberInUpdate = merchant.getPhoneNumber();
                ed_password.setText(merchant.getPassword());
                btn_save_merchant.setText("Update");

                btn_save_merchant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name = ed_name.getText().toString();
                        phoneNumber = ed_PhoneNumber.getText().toString();
                        companyName = ed_companyName.getText().toString();
                        password = ed_password.getText().toString();
                        email = merchant.getEmail();
                        TestValues("Update");
                    }
                });
            }
    }

    private void TestValues(String ActionState) {
        progressBar.setVisibility(View.VISIBLE);

        //A- username
        //1- check f the field is empty
        if (name.isEmpty()) {
            Toast.makeText(MerchantOperations.this, "fill the empty name space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2- to check if the username contains numbers or any other symbols
        String test_username = name.toLowerCase();
        state = true;
        j = 0;
        while (state && j < name.length()) {
            if ((test_username.charAt(j) < (char) 97 &&
                    test_username.charAt(j) != (char) 32)
                    || test_username.charAt(j) > (char) 122)
                state = false;
            j++;
        }
        if (!state) {
            Toast.makeText(MerchantOperations.this,
                    "the name must contain letters only in English", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //B_phone Number
        if (phoneNumber.isEmpty()) {
            Toast.makeText(MerchantOperations.this, "fill the empty phone number space",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        String startNumber = "" + phoneNumber.charAt(0) + phoneNumber.charAt(1) + phoneNumber.charAt(2);
        if (!startNumber.equals("059") && !startNumber.equals("056")) {
            Toast.makeText(MerchantOperations.this, "the number must begin with 059 or 056", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-if Phone number hase been used
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (ActionState.equals("Update")) {
                        if (snapshot1.getValue(Merchant.class).getPhoneNumber().equals(phoneNumber) &&
                                !snapshot1.getValue(Merchant.class).getPhoneNumber().equals(pastPhoneNumberInUpdate)) {
                            DuplicateNumber = true;
                            break;
                        }
                    } else {
                        if (snapshot1.getValue(Merchant.class).getPhoneNumber().equals(phoneNumber)) {
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

        countDownTimer= new CountDownTimer(3000, 1000) {
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

    private void completion(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(password);
            merchant.setPhoneNumber(phoneNumber);
            merchant.setCompanyName(companyName);
            merchant.setName(name);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String date = sdf.format(new Date());
            merchant.setDateRegister(date);

            String merchant_Id = db.getReference().child("Merchants").push().getKey();
            merchant.setId(merchant_Id);

            db.getReference().child("Merchants").child(merchant_Id).
                    setValue(merchant).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SendVerification();
                }
            });
        } else {
            Toast.makeText(MerchantOperations.this, "something wrong ! -> " +
                    task.getException().toString(), Toast.LENGTH_LONG).
                    show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void continueChecking(String ActionState) {
        //C_company Name
        if (companyName.isEmpty()) {
            Toast.makeText(this, "fill the empty Company Name space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (ActionState.equalsIgnoreCase("Add")) {
            if (email.isEmpty()) {
                Toast.makeText(MerchantOperations.this, "fill the empty email space", Toast.LENGTH_SHORT)
                        .show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
        }

        if (password.isEmpty()) {
            Toast.makeText(MerchantOperations.this, "fill the empty password space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        if (password.length() < 8) {
            Toast.makeText(MerchantOperations.this,
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
            Toast.makeText(MerchantOperations.this, "for Secret issues\n" +
                    "The password should not contain duplicate characters", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (ActionState.equalsIgnoreCase("Add")) {
            createNewAccount();
        } else if (ActionState.equalsIgnoreCase("Update")) {
            merchant.setName(name);
            merchant.setCompanyName(companyName);
            merchant.setPhoneNumber(phoneNumber);
            merchant.setEmail(email);
            merchant.setPassword(password);
            db.getReference().child("Merchants")
                    .child(merchant.getId()).setValue(merchant).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
//                    decideEmailOwner();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MerchantOperations.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return;
    }

    public void selectSharedEmployeeAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        employee = gson.fromJson(json, Employee.class);
    }

    public void SendVerification() {
        myAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MerchantOperations.this,
                            "We Send a Verification Message For This Email", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MerchantOperations.this,
                            "failed with sending verification", Toast.LENGTH_SHORT).show();
                }

                //change the login account to Admin
                myAuth.signOut();
                myAuth.signInWithEmailAndPassword(employee.getEmail(),
                        employee.getPassword())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(MerchantOperations.this, "" +
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
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
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