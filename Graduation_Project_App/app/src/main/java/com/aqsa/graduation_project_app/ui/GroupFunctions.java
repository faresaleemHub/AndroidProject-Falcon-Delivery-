package com.aqsa.graduation_project_app.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.Services.MyWorker;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public abstract class GroupFunctions extends AppCompatActivity {

//    this class to collect redundant functions along "Admin,supervisor,merchant,distributor" activities
// this class has been discussed in video : "4" in at : "D:\Android\learning\record Notes\تسجيلات فيديو"

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            GroupFunctions.closeWorkerServiceRequest();
        }
        return super.onOptionsItemSelected(item);
    }

    //this for listening for home icon on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void initToolBar(String title, int icon) {
        Toolbar toolbar = findViewById(R.id.toolbar_layout);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
    }

    public static void EmptyToast(Activity activity,String text){
        LayoutInflater inflater=activity.getLayoutInflater();
        View layout=inflater.inflate(R.layout.toast_layout,(ViewGroup) activity.findViewById(R.id.card_toast) );
        TextView tv_empty=layout.findViewById(R.id.tv_empty);
        tv_empty.setText(text);
        Toast toast=new Toast(activity);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void openTransitionStyle(Activity activity){
        activity.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
    public static void loginTransitionStyle(Activity activity) {
        //First param is for the incoming Activity (so the new)
        //Second param is for the outgoing Activity (the actual)
        activity.overridePendingTransition(R.anim.zoom_out,R.anim.static_transition);
    }

    public static void finishTransitionStyle(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public static void logoutTransitionStyle(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_down,R.anim.slide_out_down);
    }

    public static void closeWorkerServiceRequest(){
        // to run my service
        Constraints constraints=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest.Builder builder=new OneTimeWorkRequest.Builder(MyWorker.class);
        builder.setConstraints(constraints);
        OneTimeWorkRequest request=builder.build();
        WorkManager.getInstance().cancelAllWork();
    }

}
