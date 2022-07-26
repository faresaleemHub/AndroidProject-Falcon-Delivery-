package com.aqsa.graduation_project_app.ui.supervsorSide;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.EmployeeOperations;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.CategoryOperations;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.ProductsOperations;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.ReceiptOperations;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewCategories;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewProducts;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewReciepts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SupervsorDashboard extends GroupFunctions implements View.OnClickListener {

    FirebaseStorage storage;
    ImageView profileImg;
    Employee employee;
    PopupMenu popupMenu;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervsor_dashboard_);

        checkAuthenticity();

        storage = FirebaseStorage.getInstance();

        this.initToolBar("Supervisor Dashboard", R.drawable.home);

        profileImg = findViewById(R.id.ImgProfileSupervisor_Dashboard);

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

//        checkIfThereIsRecievedOrders();

        profileImg.setOnClickListener(this);
        findViewById(R.id.const_add_category).setOnClickListener(this);
        findViewById(R.id.const_show_category).setOnClickListener(this);
        findViewById(R.id.const_add_products).setOnClickListener(this);
        findViewById(R.id.const_show_products).setOnClickListener(this);
        findViewById(R.id.const_add_reciept).setOnClickListener(this);
        findViewById(R.id.const_pending_receipt).setOnClickListener(this);
        findViewById(R.id.const_show_receipt).setOnClickListener(this);
        findViewById(R.id.const_show_delivered_orders).setOnClickListener(this);
        findViewById(R.id.const_show_orders).setOnClickListener(this);
        findViewById(R.id.const_show_tranfered_orders).setOnClickListener(this);
        findViewById(R.id.const_show_loaded_orders).setOnClickListener(this);
        findViewById(R.id.const_supervisor_settings).setOnClickListener(this);
    }

    private void checkIfThereIsRecievedOrders() {
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            ClientOrderReceipt cor1 = snapshot1.getValue(ClientOrderReceipt.class);
                            if (cor1.getResponsibleDistributorID() == null) {
                                new CountDownTimer(2000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
//                                        Alerter.create(SupervsorDashboard.this)
//                                                .setTitle("Order")
//                                                .setText("There's Pending Received Orders")
//                                                .setBackgroundResource(R.color.light_blue)
//                                                .setDuration(2500)
//                                                .enableSwipeToDismiss()
//                                                .setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View v) {
//                                                        i = new Intent(getApplicationContext(), PreviewOrders.class);
//                                                        i.putExtra("ReceivedOrders", "");
//                                                        startActivity(i);
//                                                        GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
//                                                    }
//                                                })
//                                                .show();
                                    }
                                }.start();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
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
                Toast.makeText(SupervsorDashboard.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();
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
        GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onBackPressed() {
        ConstraintLayout s = findViewById(R.id.S_layout);//to get the used layout in the activity.
        Snackbar snack = Snackbar.make(s, "", Snackbar.LENGTH_SHORT);
        //LENGTH_INDEFINITE: will keep the snack_bar shown
        snack.setTextColor(Color.BLACK);
        snack.setBackgroundTint(Color.WHITE);
        snack.setAction("ClickHere to Logout", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(SupervsorDashboard.this, LoginActivity.class));
                GroupFunctions.logoutTransitionStyle(SupervsorDashboard.this);
                GroupFunctions.closeWorkerServiceRequest();
                snack.dismiss();
            }
        });
        snack.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Image(View v) {
        popupMenu = new PopupMenu(SupervsorDashboard.this, v);
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
        popupMenu = new PopupMenu(SupervsorDashboard.this, v);
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
                    Intent i = new Intent(SupervsorDashboard.this,
                            EmployeeOperations.class);
                    i.putExtra("details_object_employee", employee);
                    startActivity(i);
                    GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                } else if (id == R.id.PopUpMenu_update3) {
                    startActivity(new Intent
                            (SupervsorDashboard.this, EmployeeOperations.class)
                            .putExtra("update_object_employee", employee));
                    GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                }
                return true;
            }
        });
    }

    public Employee selectSharedSupervisorAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        Employee employee = gson.fromJson(json, Employee.class);
        return employee;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.ImgProfileSupervisor_Dashboard:
                popup_menu_Image(v);
                break;
            case R.id.const_add_category:
                startActivity(new Intent(getApplicationContext(), CategoryOperations.class));
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_category:
                startActivity(new Intent(getApplicationContext(), PreviewCategories.class));
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_add_products:
                startActivity(new Intent(getApplicationContext(), ProductsOperations.class));
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_products:
                startActivity(new Intent(getApplicationContext(), PreviewProducts.class));
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_add_reciept:
                startActivity(new Intent(getApplicationContext(), ReceiptOperations.class));
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_pending_receipt:
                i = new Intent(getApplicationContext(), PreviewReciepts.class);
                i.putExtra("show_pending", "show_pending");
                i.putExtra("Type", "Supervisor");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_receipt:
                i = new Intent(getApplicationContext(), PreviewReciepts.class);
                i.putExtra("show_done", "show_done");
                i.putExtra("Type", "Supervisor");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_delivered_orders:
                i = new Intent(getApplicationContext(), PreviewOrders.class);
                i.putExtra("DeliveredOrders", "");
                i.putExtra("Type", "Supervisor");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_orders:
                i = new Intent(getApplicationContext(), PreviewOrders.class);
                i.putExtra("ReceivedOrders", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_tranfered_orders:
                i = new Intent(getApplicationContext(), PreviewOrders.class);
                i.putExtra("TransferedOrders", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_show_loaded_orders:
                i = new Intent(getApplicationContext(), PreviewOrders.class);
                i.putExtra("LoadedOrders", "");
                startActivity(i);
                GroupFunctions.openTransitionStyle(SupervsorDashboard.this);
                break;
            case R.id.const_supervisor_settings:
                popup_menu_Settings(v);
                break;
        }
    }

    public void checkAuthenticity() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            finish();
        } else {
            employee = selectSharedSupervisorAccount();
            if (employee != null) {
                if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(employee.getEmail())) {
                    finish();
                }
            } else {
                finish();
            }
        }
    }


}