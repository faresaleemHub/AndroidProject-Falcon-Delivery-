package com.aqsa.graduation_project_app.ui.distributorSide;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.DistributorLocation;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.EmployeeOperations;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DistributorDashboard extends AppCompatActivity implements View.OnClickListener {


    FirebaseStorage storage;
    FirebaseDatabase db;
    ImageView profileImg;
    Employee employee;
    PopupMenu popupMenu;
    Intent i;
    View parentLayout;

    static final int PERMISSION_REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_dashboard);

        checkAuthenticity();

        enableTracking();

        storage = FirebaseStorage.getInstance();
        db = FirebaseDatabase.getInstance();
        parentLayout = findViewById(android.R.id.content);
        initToolBar("Distributor Dashboard", R.drawable.home);
        profileImg = findViewById(R.id.ImgProfileDistributor_Dashboard);


        if (employee != null)
            if (employee.getProfileImgURI() != null) {
                File localFile = new File(getFilesDir(), employee.getProfileImgURI());
                //here we created a directory
                if (!localFile.exists()) {
                    FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                            .child(employee.getProfileImgURI()).getFile(localFile);
                    //here will loaded the image from firebase into the file Directory on the device
                    task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                profileImg.setImageURI(Uri.fromFile(localFile));
                            }
                        }
                    });
                } else {
                    profileImg.setImageURI(Uri.fromFile(localFile));
                }
            }

        profileImg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu_Image(v);
            }
        });
        findViewById(R.id.const_conveyed_receipt_orders_D).setOnClickListener(this);
        findViewById(R.id.const_loaded_receipt_orders_D).setOnClickListener(this);

        findViewById(R.id.const_done_receipt_orders_D).setOnClickListener(this);
        findViewById(R.id.const_distributor_settings).setOnClickListener(this);


    }

    public void uploadPicture(Intent data) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image ...");
        pd.show();

        Uri fileUri = data.getData();//the path of image
        final String randomKey = System.currentTimeMillis() + "." + getExtension(fileUri);
        UploadTask uploadTask = storage.getReference("images").child(randomKey).
                putFile(fileUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    pd.dismiss();
                    //set the image path from F_Storage to F_realtime
                    employee.setProfileImgURI(randomKey);
                    FirebaseDatabase.getInstance().getReference().
                            child("Employees").child(employee.getId()).setValue(employee);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(DistributorDashboard.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double ProgressPercent =
                        (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage : " + (int) ProgressPercent + "%");
                profileImg.setImageURI(fileUri);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPicture(data);
        }
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
        GroupFunctions.openTransitionStyle(this);
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onBackPressed() {
        ConstraintLayout s = findViewById(R.id.D_layout);//to get the used layout in the activity.
        Snackbar snack = Snackbar.make(s, "", Snackbar.LENGTH_SHORT);
        //LENGTH_INDEFINITE: will keep the snack_bar shown
        snack.setTextColor(Color.BLACK);
        snack.setBackgroundTint(Color.WHITE);
        snack.setAction("ClickHere to Logout", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(parentLayout, "Remember turning Off The GPS", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Done", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(DistributorDashboard.this, LoginActivity.class));
                                GroupFunctions.logoutTransitionStyle(DistributorDashboard.this);
                                GroupFunctions.closeWorkerServiceRequest();
                                snack.dismiss();
                            }
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark))
                        .show();

            }
        });
        snack.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Image(View v) {
        popupMenu = new PopupMenu(DistributorDashboard.this, v);
        getMenuInflater().inflate(R.menu.popup_menu4, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(
                            menuPopupHelper
                                    .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
        }
        popupMenu.show();
        popup_menu_action_Image(employee);
    }

    private void popup_menu_action_Image(Employee employee) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_update4) {
                    choosePicture();
                } else if (id == R.id.PopUpMenu_delete4) {
                    employee.setProfileImgURI(null);
                    FirebaseDatabase.getInstance().getReference().
                            child("Employees").child(employee.getId()).setValue(employee);
                    profileImg.setImageURI(null);
                }
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Settings(View v) {
        popupMenu = new PopupMenu(DistributorDashboard.this, v);
        getMenuInflater().inflate(R.menu.popup_menu3, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(
                            menuPopupHelper
                                    .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
        }
        popupMenu.show();
        popup_menu_action_Settings(employee);
    }

    private void popup_menu_action_Settings(Employee employee) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details3) {
                    Intent i = new Intent(DistributorDashboard.this,
                            EmployeeOperations.class);
                    i.putExtra("details_object_employee", employee);
                    startActivity(i);
                    GroupFunctions.openTransitionStyle(DistributorDashboard.this);
                } else if (id == R.id.PopUpMenu_update3) {
                    startActivity(new Intent
                            (DistributorDashboard.this, EmployeeOperations.class)
                            .putExtra("update_object_employee", employee));
                    GroupFunctions.openTransitionStyle(DistributorDashboard.this);
                }
                return true;
            }
        });
    }

    public void selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        employee = gson.fromJson(json, Employee.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.const_conveyed_receipt_orders_D:
                i = new Intent(getApplicationContext(), DistributorOrders.class);
                i.putExtra("show_Received", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(DistributorDashboard.this);
                break;
            case R.id.const_loaded_receipt_orders_D:
                i = new Intent(getApplicationContext(), DistributorOrders.class);
                i.putExtra("show_Loaded", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(DistributorDashboard.this);
                break;
            case R.id.const_done_receipt_orders_D:
                i = new Intent(getApplicationContext(), DistributorOrders.class);
                i.putExtra("show_done", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(DistributorDashboard.this);
                break;
            case R.id.const_distributor_settings:
                popup_menu_Settings(v);
                break;
            case R.id.ImgProfileSupervisor_Dashboard:
                popup_menu_Image(v);
                break;
        }
    }

    public void checkAuthenticity() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            finish();
        } else {
            selectSharedAccount();
            if (employee != null) {
                if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(employee.getEmail())) {
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    public void enableTracking() {
//        Dexter : to check the permission of Tracking
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        ActivityCompat.requestPermissions(DistributorDashboard.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQ_CODE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void updateLocation() {
//        here we need to build the request for location
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//the accuracy of location
        locationRequest.setInterval(20000);// أي أن تحديث الموقع سينم كل 20 ثواني
        locationRequest.setFastestInterval(15000);
        // فاصل زمني بين جلب اليانات السريعة وأخذها منه , فلو تم الوصول لبيانات الموقع في أقل من هذه المدة ... سينتظر حتى مرور هذه المدة قبل التحديث

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getApplication());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DistributorDashboard.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQ_CODE);
        }
        client.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location loc = locationResult.getLastLocation();
//                    Toast.makeText(getApplicationContext(), "" + loc.getLatitude() + "-" + loc.getLongitude(), Toast.LENGTH_SHORT).show();
                    // store the location on firebase
                    DistributorLocation location = new DistributorLocation();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    String date = sdf.format(new Date());
                    location.setDate(date);
                    location.setId(employee.getId());//جعلت الرقم المُعرف للموقع هو نفسه الخاص بالموزع كي يظل ثابتاً عند تحديث موقع الموزع , فيقوم بالتغيير عليه وليس انشاء نود جديد
                    location.setLatPoint(loc.getLatitude());
                    location.setLongPoint(loc.getLongitude());
                    db.getReference().child("DistributorsLocations").child(location.getId()).setValue(location);
                }
            }
        }, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            Snackbar.make(parentLayout, "Remember turning Off The GPS", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Done", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            startActivity(new Intent(DistributorDashboard.this, LoginActivity.class));
                            GroupFunctions.logoutTransitionStyle(DistributorDashboard.this);
                            GroupFunctions.closeWorkerServiceRequest();
                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark))
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initToolBar(String title, int icon) {
        Toolbar toolbar = findViewById(R.id.toolbar_layout);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }
}