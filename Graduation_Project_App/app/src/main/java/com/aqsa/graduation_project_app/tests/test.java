package com.aqsa.graduation_project_app.tests;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aqsa.graduation_project_app.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.tapadoo.alerter.Alerter;

public class test extends AppCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.button_press).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alerter.create(test.this)
                        .setTitle("Title")
                        .setText("Alert text...")
                        .setBackgroundResource(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_done)
                        .setDuration(3000)
                        .enableProgress(true)
                        .setProgressColorRes(R.color.generic_color_blue)
                        .enableSwipeToDismiss()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .show();
            }
        });



        YoYo.with(Techniques.Shake)
                .duration(4000)
                .repeat(2)
                .playOn(findViewById(R.id.test_tv_test));


//        Toasty.error(this, "This is an error toast.", Toast.LENGTH_SHORT, true).show();
//        Toasty.success(this, "Success!", Toast.LENGTH_SHORT, true).show();
//        Toasty.info(this, "info!", Toast.LENGTH_SHORT, true).show();
//        Toasty.warning(this, "This is an error toast.", Toast.LENGTH_SHORT, true).show();
//        Toasty.normal(this, "This is an normal toast.",
//                Toast.LENGTH_SHORT, ContextCompat.getDrawable(this,R.drawable.ic_empty_yet)).show();
//        Toasty.normal(this,"this is normal one").show();
        // بدهم المكتبة :
//        implementation 'com.github.GrenderG:Toasty:1.5.2'
    }
}