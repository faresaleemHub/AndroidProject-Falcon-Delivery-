package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Vehicle;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.VehicleOperations;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreviewVehicleAdapter extends RecyclerView.Adapter<PreviewVehicleAdapter.ViewHolder> {

    private ArrayList<Vehicle> data;
    private Activity activity;
    private PopupMenu popupMenu;

    public PreviewVehicleAdapter(ArrayList<Vehicle> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PreviewVehicleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_vehicles_admin_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewVehicleAdapter.ViewHolder holder, int position) {
        holder.tv_vehicleName.setText(data.get(position).getName());
        selectDistributorName(data.get(position).getDistributorID(), holder.tv_ownerName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v, data.get(position), position);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_vehicleName, tv_ownerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_vehicleName = itemView.findViewById(R.id.tv_vehicle_name);
            tv_ownerName = itemView.findViewById(R.id.tv_vehicle_owner);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Vehicle vehicle, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu2, popupMenu.getMenu());
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

        popup_menu_action(vehicle, position);
    }

    private void popup_menu_action(Vehicle vehicle, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details2) {
                    Intent i = new Intent(activity, VehicleOperations.class);
                    i.putExtra("details_object_vehicle", vehicle);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_update2) {
                    Intent i = new Intent(activity, VehicleOperations.class);
                    i.putExtra("update_object_vehicle", vehicle);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete2) {
                    showAlertDialog(vehicle,position);
                }
                return true;
            }
        });

    }


    public void selectDistributorName(String D_ID, TextView tv) {
        FirebaseDatabase.getInstance().getReference().child("Employees").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Employee employee = snapshot1.getValue(Employee.class);
                    if (!D_ID.equals("-1")) {
                        if (D_ID.equals(employee.getId())) {
                            tv.setText(employee.getUsername());
                        }
                    }
                    else{
                            tv.setText("No one");
                            return;
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAlertDialog(Vehicle vehicle, int position){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Vehicles").child(vehicle.getId()).removeValue();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setMessage("this item will be deleted");
        AlertDialog alert = builder.create();
        alert.show();
    }

}
