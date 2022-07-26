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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewPurchases;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreviewReceiptsAdapter extends RecyclerView.Adapter<PreviewReceiptsAdapter.ViewHolder> {
    private ArrayList<Receipt> data;
    private Activity activity;
    private PopupMenu popupMenu;
    String target;

    public PreviewReceiptsAdapter(ArrayList<Receipt> data, Activity activity,String target) {
        this.data = data;
        this.activity = activity;
        this.target=target;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).
                inflate(R.layout.viewholder_preview_reciepts_supervisor_activity,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receipt receipt=data.get(position);
        holder.tv_receipt_number.setText("Receipt No : "+(position+1));
        holder.tv_receipt_date_register.setText(receipt.getDateRegister_Y());
        selectReceiptMerchantOwner(data.get(position).getMerchantID(),holder.tv_receipt_merchant_owner);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v,data.get(position),position);
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
        TextView tv_receipt_merchant_owner, tv_receipt_number,tv_receipt_date_register;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_receipt_merchant_owner=itemView.findViewById(R.id.tv_receipt_merchant_owner_value);
            tv_receipt_number=itemView.findViewById(R.id.tv_receipt_number);
            tv_receipt_date_register=itemView.findViewById(R.id.tv_receipt_date_register);
        }
    }

    public void selectReceiptMerchantOwner(String M_ID,TextView tv_receipt_merchant_owner){
        FirebaseDatabase.getInstance().getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()) {
                    if (snapshot1.getValue(Merchant.class).getId().equals(M_ID)) {
                        tv_receipt_merchant_owner.setText(snapshot1.getValue(Merchant.class).getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Receipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        if (receipt.isReceivedBySupervisor())
            activity.getMenuInflater().inflate(R.menu.popup_menu5, popupMenu.getMenu());
        else
            activity.getMenuInflater().inflate(R.menu.popup_menu15, popupMenu.getMenu());
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

        popup_menu_action(receipt, position);
    }
    private void popup_menu_action(Receipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details5) {
                    Intent i = new Intent(activity, PreviewPurchases.class);
                    i.putExtra("details_object_receipt", receipt);
                    i.putExtra("receiptID", receipt.getId());
                    if (activity.getIntent().hasExtra("Type")){
                        String type=activity.getIntent().getStringExtra("Type");
                        if (type.equalsIgnoreCase("Supervisor")){
                            i.putExtra("Type","Supervisor");
                            i.putExtra("target",target);
                        }else if (type.equalsIgnoreCase("Merchant")){
                            i.putExtra("Type","Merchant");
                        }
                    }
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete5) {
                    showAlertDialog(receipt,position);
                }
                if (id == R.id.PopUpMenu_details15) {
                    Intent i = new Intent(activity, PreviewPurchases.class);
                    i.putExtra("details_object_receipt", receipt);
                    i.putExtra("receiptID", receipt.getId());
                    i.putExtra("target",target);
                    if (activity.getIntent().hasExtra("Type")){
                        String type=activity.getIntent().getStringExtra("Type");
                        if (type.equalsIgnoreCase("Supervisor")){
                            i.putExtra("Type","Supervisor");
                        }else if (type.equalsIgnoreCase("Merchant")){
                            i.putExtra("Type","Merchant");
                        }
                    }
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete15) {
                    showAlertDialog(receipt,position);

                }else if (id==R.id.PopUpMenu_Accept15){
                    if (receipt.isPriced()){
                        if (!receipt.isAcceptedBySupervisor()) {
                            receipt.setAcceptedBySupervisor(true);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Receipts").child(receipt.getId()).setValue(receipt);
                            Toast.makeText(activity, "Accepted", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(activity, "Already Accepted", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(activity, "The receipt hasn't priced yet", Toast.LENGTH_SHORT).show();
                    }
                }else if (id==R.id.PopUpMenu_Received15){
                    if (receipt.isSentByMerchant()) {
                        receipt.setReceivedBySupervisor(true);
                        FirebaseDatabase.getInstance().getReference()
                                .child("Receipts").child(receipt.getId()).setValue(receipt);
                        //remove it from its activity to other one
//                        data.remove(position);
//                        notifyItemRangeChanged(position,data.size());
//                        notifyDataSetChanged();
//                        if (data.size()==0)
//                            activity.finish();
                    }else{
                        Toast.makeText(activity, "it's not sent by merchant yet", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    private void deleteReceiptPurchaesItems(String receipt_id) {
        FirebaseDatabase.getInstance().getReference().child("Purchases").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    if (snapshot1.getValue(PurchaseItem.class).getReceiptID().equals(receipt_id)){
                        FirebaseDatabase.getInstance().getReference()
                                .child("Purchases").child(snapshot1.getValue(PurchaseItem.class).getId()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAlertDialog(Receipt receipt, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Receipts").child(receipt.getId()).removeValue();
                        deleteReceiptPurchaesItems(receipt.getId());
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
