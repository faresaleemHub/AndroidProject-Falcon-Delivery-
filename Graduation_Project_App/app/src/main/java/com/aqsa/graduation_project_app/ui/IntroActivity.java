package com.aqsa.graduation_project_app.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IntroActivity extends AppCompatActivity {

    FirebaseDatabase db;
    FirebaseAuth myAuth;
    DatabaseReference reference;
    public static final String MY_CHANNEL_ID="myChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // to run my service
        createNotificationChannel();

        //article for lottie : https://bit.ly/3kx8qj4
        //Video src : https://bit.ly/2Za1S1l
        //lottie images website : https://bit.ly/39y8FDT

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            }
        },3000);//3 sec.


        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();
/*
        Employee employee = new Employee();
        employee.setEmail("fareses1@hotmail.com");
        employee.setJobType("Admin");
        employee.setPassword("fc1234567");
        employee.setPhone("0594022616");
        employee.setSalary("100");
        employee.setUsername("fares saleem");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String date = sdf.format(new Date());
        employee.setDateRegister(date);

        String employeeId = db.getReference().child("Employees").push().getKey();
        employee.setId(employeeId);
        db.getReference().child("Employees").child(employeeId).setValue(employee);

        myAuth.createUserWithEmailAndPassword(employee.getEmail(), employee.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    SendVerification();
                    Toast.makeText(IntroActivity.this, "done", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(IntroActivity.this, ""+
                            task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


*/
    }
    //here we need to send verification to the user
    public void SendVerification() {
        myAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(IntroActivity.this, "We Send a Verification Message For Your Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(MY_CHANNEL_ID,
                    "DefaultChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("this is a test channel");
            channel.setLockscreenVisibility(NotificationCompat.PRIORITY_HIGH);

            NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //here the channel created
            manager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        //not allowed
    }
}