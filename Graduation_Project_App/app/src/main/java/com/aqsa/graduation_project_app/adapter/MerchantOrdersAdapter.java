package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class MerchantOrdersAdapter extends RecyclerView.Adapter<MerchantOrdersAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Receipt> data;
    private PopupMenu popupMenu;
    boolean correct = true;
    String target;
    CountDownTimer countDownTimer;

    public MerchantOrdersAdapter(Activity activity, ArrayList<Receipt> data,String target) {
        this.activity = activity;
        this.data = data;
        this.target=target;
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
        Receipt receipt = data.get(position);
        holder.tv_pending_receipt_supervisor_date_order_Y.setText(receipt.getDateRegister_Y());
        holder.tv_pending_receipt_supervisor_date_order_H.setText(receipt.getDateRegister_H());
        holder.tv_pending_receipt_number_supervisor.setText("Receipt No : " + (position + 1));

        holder.img_client_order.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (receipt.isDoneByMerchant())
                    popup_menu_Merchant_done(v, data.get(position), position);
                else if (receipt.isSentByMerchant())
                    popup_menu_Merchant_transfered(v, data.get(position), position);
                else
                    popup_menu_Merchant(v, data.get(position), position);
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
        TextView tv_pending_receipt_number_supervisor,
                tv_pending_receipt_supervisor_date_order_Y,
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
    private void popup_menu_Merchant(View v, Receipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu16, popupMenu.getMenu());
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
        popup_menu_action_Merchant(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Merchant_transfered(View v, Receipt receipt, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu18, popupMenu.getMenu());
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
        popup_menu_action_Merchant_transfered(receipt, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Merchant_done(View v, Receipt receipt, int position) {
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
        }catch (Exception e){

        }
        popupMenu.show();
        popup_menu_action_Merchant_done(receipt, position);
    }

    private void popup_menu_action_Merchant(Receipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details16) {
                    Intent i = new Intent(activity, PreviewPurchases.class);
                    i.putExtra("details_object_receipt", receipt);
                    i.putExtra("Type", "Merchant");
                    i.putExtra("receiptID", receipt.getId());
                    i.putExtra("target",target);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete16) {
                    showAlertDialog(receipt,position);
                } else if (id == R.id.PopUpMenu_Transfer16) {
                    if (!receipt.isPriced()) {
                        Toast.makeText(activity.getApplicationContext(), "not priced yet", Toast.LENGTH_SHORT).show();
                    } else if (receipt.isAcceptedBySupervisor()) {
                        if (!receipt.isSentByMerchant()) {
                            receipt.setSentByMerchant(true);
                            FirebaseDatabase.getInstance().getReference().child("Receipts").child(receipt.getId()).setValue(receipt);
                            data.remove(position);
                            notifyItemRangeChanged(position, data.size());
                            notifyDataSetChanged();
                            if (data.size()==0)
                                activity.finish();
                        } else
                            Toast.makeText(activity.getApplicationContext(), "it's already sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Not Accepted yet by Supervisor", Toast.LENGTH_SHORT).show();
                    }
                } else if (id == R.id.PopUpMenu_Priced16) {
                    if (!receipt.isPriced()) {
                        isPricedCorrectly(receipt);
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Receipt has already been priced", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    private void popup_menu_action_Merchant_transfered(Receipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details18) {
                    Intent i = new Intent(activity, PreviewPurchases.class);
                    i.putExtra("details_object_receipt", receipt);
                    i.putExtra("Type", "Merchant");
                    i.putExtra("receiptID", receipt.getId());
                    i.putExtra("target",target);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete18) {
                    showAlertDialog(receipt,position);
                } else if (id == R.id.PopUpMenu_Done18) {
                    if (receipt.isReceivedBySupervisor()) {
                        receipt.setDoneByMerchant(true);
                        FirebaseDatabase.getInstance().getReference().child("Receipts").child(receipt.getId()).setValue(receipt);
                        data.remove(position);
                        notifyDataSetChanged();
                        notifyItemRangeChanged(position,data.size());
                        if (data.size()==0)
                            activity.finish();
                    } else {
                        Toast.makeText(activity, "Not Received by Supervisor yet", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    private void popup_menu_action_Merchant_done(Receipt receipt, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_Details11) {
                    Intent i = new Intent(activity, PreviewPurchases.class);
                    i.putExtra("details_object_receipt", receipt);
                    i.putExtra("Type", "Merchant");
                    i.putExtra("receiptID", receipt.getId());
                    i.putExtra("target",target);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_Delete11) {
                    showAlertDialog(receipt,position);
                }
                return true;
            }
        });
    }

    private void deleteReceiptPurchaesItems(String receipt_id) {
        FirebaseDatabase.getInstance().getReference().child("Purchases").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(PurchaseItem.class).getReceiptID().equals(receipt_id)) {
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

    public void isPricedCorrectly(Receipt receipt) {

        activity.findViewById(R.id.progressBar_previewPendingOrdersMerchant).setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child("Purchases").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(PurchaseItem.class).getReceiptID().equalsIgnoreCase(receipt.getId())) {
                        if (snapshot1.getValue(PurchaseItem.class).getPrice().isEmpty()) {
                            correct = false;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        countDownTimer=new CountDownTimer(2500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (correct == false) {
                    Toast.makeText(activity, "something wrong with pricing", Toast.LENGTH_SHORT).show();
                    countDownTimer.cancel();
                    activity.findViewById(R.id.progressBar_previewPendingOrdersMerchant).setVisibility(View.GONE);
                }
            }

            @Override
            public void onFinish() {
                if (correct) {
                    receipt.setPriced(true);
                    FirebaseDatabase.getInstance().getReference().child("Receipts").child(receipt.getId()).setValue(receipt);
                    Toast.makeText(activity, "Receipt priced", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "something wrong with pricing", Toast.LENGTH_SHORT).show();
                }
                activity.findViewById(R.id.progressBar_previewPendingOrdersMerchant).setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(Receipt receipt, int position){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Receipts").child(receipt.getId()).removeValue();
                        deleteReceiptPurchaesItems(receipt.getId());
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