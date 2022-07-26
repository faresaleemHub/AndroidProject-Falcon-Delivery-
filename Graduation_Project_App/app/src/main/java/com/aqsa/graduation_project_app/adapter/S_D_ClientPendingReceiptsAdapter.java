package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewEmplloyees;
import com.aqsa.graduation_project_app.ui.clientSide.RecieptDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class S_D_ClientPendingReceiptsAdapter extends RecyclerView.Adapter<S_D_ClientPendingReceiptsAdapter.ViewHolder> {
//supervisor / distributor

    private Activity activity;
    private ArrayList<ClientOrderReceipt> data;
    private PopupMenu popupMenu;
    private Employee employee;
    private String source;

    public S_D_ClientPendingReceiptsAdapter(
            Activity activity, ArrayList<ClientOrderReceipt> data, Employee employee,String source) {
        this.activity = activity;
        this.data = data;
        this.employee = employee;
        this.source=source;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate
                (R.layout.viewholder_preview_pending_reciepts_activity,
                        parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ClientOrderReceipt cor = data.get(position);
        holder.tv_pending_receipt_supervisor_date_order_Y.setText(cor.getOrderDate_Y());
        holder.tv_pending_receipt_supervisor_date_order_H.setText(cor.getOrderDate_H());
        holder.tv_pending_receipt_number_supervisor.setText("Receipt No : " + (position + 1));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (employee != null && employee.getJobType() != null) {
                    if (employee.getJobType().equals("Supervisor")) {
                        if (cor.getResponsibleDistributorID() == null)
                            popup_menu_Supervisor(v, data.get(position), position);
                        else if (!cor.isSupervisorConveyedTheOrderFromTheStoreToTheDistributor()) {
                            popup_menu_loading(v, data.get(position), position);
                        } else {
                            popup_menu_default_supervisor(v, data.get(position), position);
                        }
                    } else if (employee.getJobType().equals("Distributor")) {
                        if (!cor.isDistributorReceivedTheOrderFromTheStore())
                            popup_menu_loading(v, data.get(position), position);
                        else if (cor.isDistributorReceivedTheOrderFromTheStore() &&
                                !cor.isDeliveredByDistributorToClient()) {
                            popup_menu_delivered(v, data.get(position), position);
                        } else {
                            popup_menu_default_distributor(v, data.get(position), position);
                        }
                    }
                }
                if (activity.getIntent().hasExtra("Dealing_History_Client") ||
                        activity.getIntent().hasExtra("Dealing_History_Distributor")) {
                    popup_menu_client_hestory_orders(v, data.get(position), position);
                }
            }
        });
        selectOrderClientOwner(cor.getClient_ID(), holder.img_client_order);
    }

    private void selectOrderClientOwner(String Client_ID, ImageView img) {
        FirebaseDatabase.getInstance().getReference().child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Client client = snapshot1.getValue(Client.class);
                    if (client.getId().equals(Client_ID)) {
                        if (client.getProfileImgURI() != null) {
                            File localFile = new File(activity.getFilesDir(), client.getProfileImgURI());
                            //here we created a directory
                            if (!localFile.exists()) {
                                FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                                        .child(client.getProfileImgURI()).getFile(localFile);
                                //here will loaded the image from firebase into the file Directory on the device
                                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            img.setImageURI(Uri.fromFile(localFile));
                                        }
                                    }
                                });
                            } else {
                                img.setImageURI(Uri.fromFile(localFile));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_pending_receipt_number_supervisor, tv_pending_receipt_supervisor_date_order_Y,
                tv_pending_receipt_supervisor_date_order_H;
        ImageView img_client_order;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_pending_receipt_number_supervisor = itemView.findViewById(R.id.tv_pending_receipt_number_supervisor);
            tv_pending_receipt_supervisor_date_order_Y = itemView.findViewById(R.id.tv_pending_receipt_supervisor_date_order_Y);
            tv_pending_receipt_supervisor_date_order_H = itemView.findViewById(R.id.tv_pending_receipt_supervisor_date_order_H);
            img_client_order = itemView.findViewById(R.id.img_client_order);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Supervisor(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu7, popupMenu.getMenu());
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
        popup_menu_action_Supervisor(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_loading(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        if (employee.getJobType().equals("Supervisor")){
            activity.getMenuInflater().inflate(R.menu.popup_menu8, popupMenu.getMenu());
        } else if (employee.getJobType().equals("Distributor")) {
            activity.getMenuInflater().inflate(R.menu.popup_menu22, popupMenu.getMenu());
        }
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
        popup_menu_action_loading(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_delivered(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu10, popupMenu.getMenu());
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
        popup_menu_action_delivered_loading(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_client_hestory_orders(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu11, popupMenu.getMenu());
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
        popup_menu_action_delivered_default_supervisor_admin(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_default_supervisor(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu11, popupMenu.getMenu());
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
        popup_menu_action_delivered_default_supervisor_admin(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_default_distributor(View v, ClientOrderReceipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu12, popupMenu.getMenu());
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
        popup_menu_action_delivered_default_distributor(receipt, position);
    }

    private void popup_menu_action_delivered_default_distributor(ClientOrderReceipt receipt, int
            position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_Details12) {
                    Intent i = new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source",source);
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }


    private void popup_menu_action_delivered_default_supervisor_admin(ClientOrderReceipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_Details11) {
                    Intent i = new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source",source);
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);

                } else if (id == R.id.PopUpMenu_Delete11) {
                    showAlertDialog(receipt, position);
                }
                return true;
            }
        });
    }

    private void popup_menu_action_Supervisor(ClientOrderReceipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details7) {
                    Intent i = new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source",source);
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete7) {
                    showAlertDialog(receipt, position);
                } else if (id == R.id.PopUpMenu_transferToDistributor) {
                    Intent i = new Intent(activity, PreviewEmplloyees.class);
                    i.putExtra("Order", data.get(position));
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }

    private void popup_menu_action_loading(ClientOrderReceipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_loaded) {
                    if (employee.getJobType().equals("Supervisor")) {
                        receipt.setSupervisorConveyedTheOrderFromTheStoreToTheDistributor(true);
                        FirebaseDatabase.getInstance().getReference()
                                .child("ClientsReceiptOrders").child(receipt.getId()).setValue(receipt);
                    } else if (employee.getJobType().equals("Distributor")) {
                        if (receipt.isSupervisorConveyedTheOrderFromTheStoreToTheDistributor()) {
                            receipt.setDistributorReceivedTheOrderFromTheStore(true);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("ClientsReceiptOrders").child(receipt.getId()).setValue(receipt);
                        } else {
                            Toast.makeText(activity, "The supervisor must confirm the loading of order before", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (id == R.id.PopUpMenu_detals6) {
                    Intent i = new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source",source);
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete8) {
                    showAlertDialog(receipt, position);
                }
                return true;
            }
        });
    }

    private void popup_menu_action_delivered_loading(ClientOrderReceipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_Delivered) {
                    if (employee != null) {
                        if (employee.getJobType().equals("Distributor")) {
                            if (!receipt.isReceivedByClient())
                                Toast.makeText(activity, "not allowed, the Client must confirm the receiving", Toast.LENGTH_SHORT).show();
                            else {
                                receipt.setDeliveredByDistributorToClient(true);
                                FirebaseDatabase.getInstance().getReference()
                                        .child("ClientsReceiptOrders").child(receipt.getId()).setValue(receipt);
                            }
                        }
                    }
                } else if (id == R.id.PopUpMenu_Details8) {
                    Intent i = new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt", receipt);
                    i.putExtra("source",source);
                    activity.startActivityForResult(i,1);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }

    private void deleteReceiptItems(String Receipt_ID) {
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrdersItems").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.getValue(ClientOrderItem.class).getReceipt_ID().equals(Receipt_ID))
                                FirebaseDatabase.getInstance().getReference().
                                        child("ClientsReceiptOrdersItems")
                                        .child(snapshot1.getValue(ClientOrderItem.class).getId()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showAlertDialog(ClientOrderReceipt receipt, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Task<Void> task = FirebaseDatabase.getInstance().getReference()
                                .child("ClientsReceiptOrders").child(receipt.getId()).removeValue();
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                deleteReceiptItems(receipt.getId());
                            }
                        });
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