package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.EmployeeOperations;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewClients;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewEmplloyees;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreviewEmployeesAdapter extends RecyclerView.Adapter<PreviewEmployeesAdapter.ViewHolder> {

    private ArrayList<Employee> data;
    private Activity activity;
    private PopupMenu popupMenu;
    private ClientOrderReceipt order;
    int numorders = 0;
    private Employee employee;

    CountDownTimer countDownTimer;

    public PreviewEmployeesAdapter(ArrayList<Employee> data, Activity activity,Employee employee) {
        this.data = data;
        this.activity = activity;
        this.employee=employee;
    }

    public PreviewEmployeesAdapter(ArrayList<Employee> data, Activity activity, ClientOrderReceipt order) {
        this.data = data;
        this.activity = activity;
        this.order = order;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_employees_admin_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee employee = data.get(position);
        holder.tv_username.setText(employee.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (order == null)
                    popup_menu(v, data.get(position), position);
                else {
                    popup_menu_chooseDistributor(v, data.get(position));
                }
            }
        });

        if (employee.getProfileImgURI() != null) {
            File localFile = new File(activity.getFilesDir(), employee.getProfileImgURI());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                        .child(employee.getProfileImgURI()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.ImgProfileEmployee.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.ImgProfileEmployee.setImageURI(Uri.fromFile(localFile));
            }
        }
    }

    private void getNumOrders(Employee employee, TextView tv_numOrders) {
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ClientOrderReceipt receipt = snapshot1.getValue(ClientOrderReceipt.class);
                    if (receipt.getResponsibleDistributorID() != null) {
                        if (receipt.getResponsibleDistributorID().equals(employee.getId())) {
                            if (!receipt.isDeliveredByDistributorToClient()) {
                                numorders++;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer=new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (numorders!=0){
                    tv_numOrders.setText(numorders + "");
                    numorders = 0;
                    countDownTimer.cancel();
                }

            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        ImageView ImgProfileEmployee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_employee_username);
            ImgProfileEmployee = itemView.findViewById(R.id.ImgProfileEmployee);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Employee employee, int position) {
        popupMenu = new PopupMenu(activity, v);
        if (employee.getJobType().equalsIgnoreCase("Supervisor"))
            activity.getMenuInflater().inflate(R.menu.popup_menu20, popupMenu.getMenu());
        else if (employee.getJobType().equalsIgnoreCase("Distributor"))
            activity.getMenuInflater().inflate(R.menu.popup_menu19, popupMenu.getMenu());
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

        popup_menu_action(employee, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_chooseDistributor(View v, Employee employee) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu9, popupMenu.getMenu());
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

        popup_menu_action_chooseDistributor(employee);
    }

    private void popup_menu_action(Employee employee, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details) {
                    Intent i = new Intent(activity, EmployeeOperations.class);
                    i.putExtra("details_object_employee", employee);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_update) {
                    Intent i = new Intent(activity, EmployeeOperations.class);
                    i.putExtra("update_object_employee", employee);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete) {
                    showAlertDialog(employee);
                } else if (id == R.id.PopUpMenu_Call) {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:" + employee.getPhone()));
                    Intent i2 = Intent.createChooser(i, "choose the calling App you need");
                    activity.startActivity(i2);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_DealingHistory_Distributor) {
                    Intent i = new Intent(activity, PreviewOrders.class);
                    i.putExtra("Dealing_History_Distributor", employee);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }

    private void popup_menu_action_chooseDistributor(Employee employee) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_chooseDistributor) {
                    order.setResponsibleDistributorID(employee.getId());
                    FirebaseDatabase.getInstance().getReference().
                            child("ClientsReceiptOrders").child(order.getId()).
                            setValue(order);
//                    Intent i = new Intent(activity, PreviewOrders.class);
//                    i.putExtra("ReceivedOrders", "");
//                    i.putExtra("choosed_distributor", "");
//                    activity.startActivity(i);
                    activity.finish();
                    GroupFunctions.finishTransitionStyle(activity);
                }else if (id == R.id.PopUpMenu_details) {
                    Intent i = new Intent(activity, EmployeeOperations.class);
                    i.putExtra("details_object_employee", employee);
                    if (order!=null)
                        i.putExtra("order", employee);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });

    }


    private void showAlertDialog(Employee employee1) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth auth=FirebaseAuth.getInstance();
                        auth.signOut();
                        auth.signInWithEmailAndPassword(employee1.getEmail(),employee1.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        auth.signInWithEmailAndPassword(employee.getEmail(),employee.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Employees").child(employee1.getId()).removeValue();

                                                Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        activity.finish();
                        GroupFunctions.finishTransitionStyle(activity);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setMessage("this item will be deleted");
        AlertDialog alert = builder.create();
        alert.show();
    }

}
