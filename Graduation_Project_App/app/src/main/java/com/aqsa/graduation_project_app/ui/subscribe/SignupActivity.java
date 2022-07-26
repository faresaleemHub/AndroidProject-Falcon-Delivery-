package com.aqsa.graduation_project_app.ui.subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseDatabase db;//for realtime database
    FirebaseAuth myAuth;//for authentication

    Button btn_signup;
    EditText ed_username, ed_phone, ed_email, ed_password, ed_passwordConfirmation, ed_marketName;
    String username, phone, email, password, passwordConfirmation, marketName;
    TextView tv_haveAccount;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.signup_progress);
        btn_signup = findViewById(R.id.btn_Signup);
        ed_username = findViewById(R.id.ed_signup_username);
        ed_phone = findViewById(R.id.ed_signup_phone);
        ed_email = findViewById(R.id.ed_signup_email);
        ed_password = findViewById(R.id.ed_signup_password);
        ed_passwordConfirmation = findViewById(R.id.ed_signup_passwordConfirmation);
        ed_marketName = findViewById(R.id.ed_signup_marketName);
        tv_haveAccount = findViewById(R.id.tv_signup_haveAccount);

        tv_haveAccount.setOnClickListener(this);
        btn_signup.setOnClickListener(this);
    }

    private void sign_up() {
        myAuth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        completion(task);
                    }
                });
    }

    public void completion(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            //save the user into firebase
            Client client = new Client();
            client.setEmail(email);
            client.setMarketName(marketName);
            client.setPassword(password);
            client.setPhone(phone);
            client.setUsername(username);

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
            Toast.makeText(SignupActivity.this, "something wrong ! -> " +
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
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignupActivity.this,
                            "We Send a Verification Message For Your Email", Toast.LENGTH_LONG).show();
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                    GroupFunctions.finishTransitionStyle(SignupActivity.this);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignupActivity.this, "failed with sending verification", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.tv_signup_haveAccount:
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                GroupFunctions.finishTransitionStyle(SignupActivity.this);
                break;

            case R.id.btn_Signup:
                progressBar.setVisibility(View.VISIBLE);
                username = ed_username.getText().toString();
                phone = ed_phone.getText().toString();
                password = ed_password.getText().toString();
                passwordConfirmation = ed_passwordConfirmation.getText().toString();
                email = ed_email.getText().toString();
                marketName = ed_marketName.getText().toString();

                //conditions
                //A- username
                //1- check f the field is empty
                if (username.length() == 0) {
//                    ed_username.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this,
                            "fill the empty space", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_username);
                    return;
                }
//                else
//                    ed_username.setBackgroundColor(Color.WHITE);

                //2- to check if the username contains numbers or any other symbols
                String test_username = username.toLowerCase();
                boolean state = true;
                int j = 0;
                while (state && j < username.length()) {
                    if ((test_username.charAt(j) < (char) 97 &&
                            test_username.charAt(j) != (char) 32)
                            || test_username.charAt(j) > (char) 122)
                        state = false;
                    j++;
                }
                if (!state) {
                    Toast.makeText(SignupActivity.this,
                            "the name must contain letters only in English",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_username);
                    return;
                }
//                else
//                    ed_username.setBackgroundColor(Color.WHITE);


                //B-email
                if (email.length() == 0) {
//                    ed_email.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "fill the empty space", Toast.LENGTH_SHORT)
                            .show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_email);

                    return;
                }
//                else
//                    ed_email.setBackgroundColor(Color.WHITE);


                //C-phone
                //1-
                if (phone.length() == 0) {
//                    ed_phone.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "fill the empty space",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_phone);

                    return;
                }
//                else
//                    ed_phone.setBackgroundColor(Color.WHITE);

                if (phone.length() !=10) {
//                    ed_phone.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "enter a correct Phone Number",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_phone);

                    return;
                }
//                else
//                    ed_phone.setBackgroundColor(Color.WHITE);

                //2-
                String startNumber = "" + phone.charAt(0) + phone.charAt(1) + phone.charAt(2);
                if (!startNumber.equals("059") && !startNumber.equals("056")) {
                    Toast.makeText(SignupActivity.this, "the number must begin with 059 or 056", Toast.LENGTH_SHORT).show();
//                    ed_phone.setBackgroundColor(Color.RED);
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_phone);


                    return;
                }
//                else
//                    ed_phone.setBackgroundColor(Color.WHITE);


                //Password
                if (password.length() == 0) {
//                    ed_password.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "fill the empty password space", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_phone);

                    return;
                }
//                else
//                    ed_password.setBackgroundColor(Color.WHITE);

                //2-
                if (password.length() < 8) {
//                    ed_password.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this,
                            "The Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_password);

                    return;
                }
//                else
//                    ed_password.setBackgroundColor(Color.WHITE);

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
//                    ed_password.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "for Secret issues\n" +
                            "The password should not contain duplicate characters", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_password);

                    return;
                }
//                else
//                    ed_password.setBackgroundColor(Color.WHITE);

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
                    Toast.makeText(SignupActivity.this, "for Secret issues\n" +
                            "the password must contain at least 1 symbol", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_password);
                    return;
                }
//                else
//                    ed_password.setBackgroundColor(Color.WHITE);

                //5-
                if (passwordConfirmation.length() == 0) {
//                    ed_passwordConfirmation.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "fill the empty password space", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_passwordConfirmation);

                    return;
                }
//                else
//                    ed_passwordConfirmation.setBackgroundColor(Color.WHITE);

                //6-
                if (!password.equals(passwordConfirmation)) {
                    Toast.makeText(SignupActivity.this, "The two passwords are't the same", Toast.LENGTH_SHORT).show();
//                    ed_password.setBackgroundColor(Color.RED);
//                    ed_passwordConfirmation.setBackgroundColor(Color.RED);
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_password);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_passwordConfirmation);

                    return;
                }
//                else {
//                    ed_password.setBackgroundColor(Color.WHITE);
//                    ed_passwordConfirmation.setBackgroundColor(Color.WHITE);
//                }

                //E- Market Name
                if (marketName.length() == 0) {
//                    ed_marketName.setBackgroundColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "fill the empty space", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);

                    YoYo.with(Techniques.Pulse)
                            .duration(1000)
                            .repeat(1)
                            .playOn(ed_marketName);

                    return;
                }
//                else
//                    ed_marketName.setBackgroundColor(Color.WHITE);
                sign_up();
                break;
        }
    }
}