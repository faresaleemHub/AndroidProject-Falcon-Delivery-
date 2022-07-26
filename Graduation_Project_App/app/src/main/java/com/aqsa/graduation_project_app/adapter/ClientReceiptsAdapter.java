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
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.ui.clientSide.RecieptDetails;
import com.aqsa.graduation_project_app.ui.clientSide.mapClientSelectLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ClientReceiptsAdapter extends RecyclerView.Adapter<ClientReceiptsAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<ClientOrderReceipt>data;
    private PopupMenu popupMenu;

    public ClientReceiptsAdapter(Activity activity, ArrayList<ClientOrderReceipt> data) {
        this.activity = activity;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate
                (R.layout.viewholder_preview_pending_reciepts_client_activity,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ClientOrderReceipt cor=data.get(position);
        holder.tv_pending_receipt_date_order_Y.setText(cor.getOrderDate_Y());
        holder.tv_pending_receipt_date_order_H.setText(cor.getOrderDate_H());
        holder.tv_pending_receipt_number.setText("Receipt No : "+(position+1));

        if (cor.isReceivedByClient()){
            holder.btn_done.setText("Done");
            holder.btn_done.setEnabled(false);
        }else if (cor.isDistributorReceivedTheOrderFromTheStore()&& !cor.isReceivedByClient()){
            holder.btn_done.setEnabled(true);
        }else{
            holder.btn_done.setEnabled(false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v,data.get(position),position);
            }
        });
        holder.btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.get(position).setReceivedByClient(true);
                FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders")
                        .child(data.get(position).getId()).setValue(data.get(position));

            }
        });
    }

    @Override
    public int getItemCount() {
        if (data!=null)
            return data.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_pending_receipt_number,tv_pending_receipt_date_order_Y,
                tv_pending_receipt_date_order_H;
        Button btn_done;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_pending_receipt_number=itemView.findViewById(R.id.tv_pending_receipt_number);
            tv_pending_receipt_date_order_Y=itemView.findViewById(R.id.tv_pending_receipt_date_order_Y);
            tv_pending_receipt_date_order_H=itemView.findViewById(R.id.tv_pending_receipt_date_order_H);
            btn_done=itemView.findViewById(R.id.btn_done_client_order_receipt);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, ClientOrderReceipt receipt, int position) {
        popupMenu=new PopupMenu(activity,v);
        if (receipt.isReceivedByClient())
            activity.getMenuInflater().inflate(R.menu.popup_menu5,popupMenu.getMenu());
        else
            activity.getMenuInflater().inflate(R.menu.popup_menu24,popupMenu.getMenu());
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
        }catch (Exception e){

        }


        popupMenu.show();

        popup_menu_action(receipt,position);
    }

    private void popup_menu_action(ClientOrderReceipt receipt,int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.PopUpMenu_details5) {
                    Intent i =new Intent(activity, RecieptDetails.class);
                    i.putExtra("details_object_client_receipt",receipt);
                    activity.startActivity(i);
                }else if (id==R.id.PopUpMenu_delete5) {
                    showAlertDialog(receipt);
                }else if (id==R.id.PopUpMenu_dist_location) {
                    String id_dist = receipt.getResponsibleDistributorID();
                    if (id_dist != null) {
                        Intent i = new Intent(activity, mapClientSelectLocation.class);
                        i.putExtra("dist_location", id_dist);
                        activity.startActivity(i);
                    }else{
                        Toast.makeText(activity, "Not handled by a distributor yet", Toast.LENGTH_SHORT).show();
                    }
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
                for (DataSnapshot snapshot1:snapshot.getChildren()){
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

    private void showAlertDialog(ClientOrderReceipt receipt){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("ClientsReceiptOrders").child(receipt.getId()).removeValue();
                        deleteReceiptItems(receipt.getId());
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
