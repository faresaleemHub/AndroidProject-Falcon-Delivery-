package com.aqsa.graduation_project_app.ui.adminSide;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.ClientOperations;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.EmployeeOperations;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.MerchantOperations;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.VehicleOperations;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewClients;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewEmplloyees;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewMerchant;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewVehicle;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AdminDashboard extends GroupFunctions implements View.OnClickListener {

    PopupMenu popupMenu;
    ImageView profileImg;
    Employee employee;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        checkAuthenticity();

        storage = FirebaseStorage.getInstance();
        this.initToolBar("Admin Dashboard", R.drawable.home);

        profileImg = findViewById(R.id.ImgProfileAdmin_Dashboard);

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

        profileImg.setOnClickListener(this);

        // add Actions
        findViewById(R.id.const_employee_operations).setOnClickListener(this);
        findViewById(R.id.const_client_operations).setOnClickListener(this);
        findViewById(R.id.const_add_vehicle).setOnClickListener(this);
        findViewById(R.id.const_merchant_operations).setOnClickListener(this);

        //previews Actions
        findViewById(R.id.const_show_employees).setOnClickListener(this);
        findViewById(R.id.const_preview_clients).setOnClickListener(this);
        findViewById(R.id.const_show_vehicle).setOnClickListener(this);
        findViewById(R.id.const_show_merchant).setOnClickListener(this);
        findViewById(R.id.const_admin_settings).setOnClickListener(this);

    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
        GroupFunctions.openTransitionStyle(this);
    }

    //ctrl+o : will get a list of all methods available to override here

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPicture(data);
        }
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
                    profileImg.setImageURI(fileUri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AdminDashboard.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double ProgressPercent =
                        (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage : " + (int) ProgressPercent + "%");
            }
        });
    }

    //to get the extension of the image
    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onBackPressed() {
        ConstraintLayout s = findViewById(R.id.layout);//to get the used layout in the activity.
        Snackbar snack = Snackbar.make(s, "", Snackbar.LENGTH_SHORT);
        //LENGTH_INDEFINITE: will keep the snack_bar shown
        snack.setTextColor(Color.BLACK);
        snack.setBackgroundTint(Color.WHITE);
        snack.setAction("ClickHere to Logout", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(AdminDashboard.this, LoginActivity.class));
                GroupFunctions.logoutTransitionStyle(AdminDashboard.this);
                GroupFunctions.closeWorkerServiceRequest();
                snack.dismiss();
            }
        });
        snack.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Settings(View v) {
        popupMenu = new PopupMenu(AdminDashboard.this, v);
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
                    Intent i = new Intent(AdminDashboard.this, EmployeeOperations.class);
                    i.putExtra("details_object_employee", employee);
                    startActivity(i);
                    GroupFunctions.openTransitionStyle(AdminDashboard.this);
                } else if (id == R.id.PopUpMenu_update3) {
                    startActivity(new Intent
                            (AdminDashboard.this, EmployeeOperations.class)
                            .putExtra("update_object_employee", employee));
                    GroupFunctions.openTransitionStyle(AdminDashboard.this);
                }
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Image(View v) {
        popupMenu = new PopupMenu(AdminDashboard.this, v);
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
        }catch (Exception e){}
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

    public void selectSharedAdminAccount() {
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
            case R.id.ImgProfileAdmin_Dashboard:
                popup_menu_Image(v);
                break;
            case R.id.const_employee_operations:
                startActivity(new Intent(AdminDashboard.this, EmployeeOperations.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_client_operations:
                //default start Activity here for "Adding new client"
                startActivity(new Intent(AdminDashboard.this, ClientOperations.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_add_vehicle:
                startActivity(new Intent(AdminDashboard.this, VehicleOperations.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_merchant_operations:
                startActivity(new Intent(AdminDashboard.this, MerchantOperations.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_show_employees:
                startActivity(new Intent(AdminDashboard.this, PreviewEmplloyees.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_preview_clients:
                startActivity(new Intent(AdminDashboard.this, PreviewClients.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_show_vehicle:
                startActivity(new Intent(AdminDashboard.this, PreviewVehicle.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_show_merchant:
                startActivity(new Intent(AdminDashboard.this, PreviewMerchant.class));
                GroupFunctions.openTransitionStyle(this);
                break;
            case R.id.const_admin_settings:
                popup_menu_Settings(v);
                break;
        }
    }

    public void checkAuthenticity(){
        if (FirebaseAuth.getInstance().getCurrentUser()==null) {
            finish();
        }else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            finish();
        }else {
            selectSharedAdminAccount();
            if (employee!=null) {
                if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(employee.getEmail())) {
                    finish();
                }
            }else{
                finish();
            }
        }
    }
}