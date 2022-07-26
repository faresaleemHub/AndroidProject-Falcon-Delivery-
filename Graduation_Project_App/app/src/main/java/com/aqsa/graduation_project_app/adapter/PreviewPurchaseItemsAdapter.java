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
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.PurchaseItemOperations;
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

public class PreviewPurchaseItemsAdapter extends RecyclerView.Adapter<PreviewPurchaseItemsAdapter.ViewHolder> {
    private ArrayList<PurchaseItem> data;
    private Activity activity;
    private PopupMenu popupMenu;
    private String AccountType;
    private Receipt extra_receipt;

    public PreviewPurchaseItemsAdapter(ArrayList<PurchaseItem> data, Activity activity, Receipt receipt) {
        this.data = data;
        this.activity = activity;
        this.extra_receipt = receipt;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).
                inflate(R.layout.viewholder_preview_purchase_items_supervisor_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        selectPurchaseDetails(data.get(position).getProductID(), holder.tv_purchaseItemName, holder.img_purchaseItem);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (activity.getIntent().hasExtra("Type")) {
                    AccountType = activity.getIntent().getStringExtra("Type");
                    if (AccountType.equalsIgnoreCase("Merchant")) {
                        popup_menu_merchant(v, data.get(position), position);
                    } else if (AccountType.equalsIgnoreCase("Supervisor")) {
                        popup_menu_supervisor(v, data.get(position), position);
                    }
                }
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
        ImageView img_purchaseItem;
        TextView tv_purchaseItemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_purchaseItem = itemView.findViewById(R.id.img_purchaseItem);
            tv_purchaseItemName = itemView.findViewById(R.id.tv_purchaseItemName);
        }
    }

    public void selectPurchaseDetails(String Product_ID, TextView tv_purchaseItemName, ImageView img_purchaseItem) {
        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Product.class).getId().equals(Product_ID)) {
                        Product product = snapshot1.getValue(Product.class);
                        tv_purchaseItemName.setText(product.getName());

                        File localFile = new File(activity.getFilesDir(), product.getImg());
                        //here we created a directory
                        if (!localFile.exists()) {
                            FileDownloadTask task = FirebaseStorage.getInstance().getReference("ProductImages")
                                    .child(product.getImg()).getFile(localFile);
                            //here will loaded the image from firebase into the file Directory on the device
                            task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        img_purchaseItem.setImageURI(Uri.fromFile(localFile));
                                    }
                                }
                            });
                        } else {
                            img_purchaseItem.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_supervisor(View v, PurchaseItem purchaseItem, int position) {
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
        popup_menu_action_supervisor(purchaseItem, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_merchant(View v, PurchaseItem purchaseItem, int position) {
        popupMenu = new PopupMenu(activity, v);
        activity.getMenuInflater().inflate(R.menu.popup_menu17, popupMenu.getMenu());
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
        popup_menu_action_merchant(purchaseItem, position);
    }

    private void popup_menu_action_supervisor(PurchaseItem purchaseItem, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details2) {
                    Intent i = new Intent(activity, PurchaseItemOperations.class);
                    i.putExtra("details_object_purchaseItem", purchaseItem);
                    if (activity.getIntent().hasExtra("Type")) {
                        if (AccountType.equalsIgnoreCase("Supervisor")) {
                            i.putExtra("Type", "Supervisor");
                        } else if (AccountType.equalsIgnoreCase("Merchant")) {
                            i.putExtra("Type", "Merchant");
                        }
                    }
                    activity.startActivity(i);
                } else if (id == R.id.PopUpMenu_update2) {
                    isAllowedUpdate(purchaseItem);
                } else if (id == R.id.PopUpMenu_delete2) {
                    isAllowedDelete(purchaseItem, position);
                }
                return true;
            }
        });
    }

    private void isAllowedDelete(PurchaseItem purchaseItem, int position) {
        FirebaseDatabase.getInstance().getReference().child("Receipts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Receipt receipt = snapshot1.getValue(Receipt.class);
                    if (receipt.getId().equalsIgnoreCase(purchaseItem.getReceiptID()))
                        if (receipt.isDoneByMerchant() || receipt.isReceivedBySupervisor()) {
                            Toast.makeText(activity, "Not Allowed,it's Done", Toast.LENGTH_SHORT).show();
                        } else {
                            showAlertDialog(purchaseItem);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void popup_menu_action_merchant(PurchaseItem purchaseItem, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details17) {
                    Intent i = new Intent(activity, PurchaseItemOperations.class);
                    i.putExtra("details_object_purchaseItem", purchaseItem);
                    if (activity.getIntent().hasExtra("Type")) {
                        if (AccountType.equalsIgnoreCase("Supervisor")) {
                            i.putExtra("Type", "Supervisor");
                        } else if (AccountType.equalsIgnoreCase("Merchant")) {
                            i.putExtra("Type", "Merchant");
                        }
                    }
                    activity.startActivity(i);
                } else if (id == R.id.PopUpMenu_update17) {
                    isAllowedUpdate(purchaseItem);
                }
                return true;
            }
        });
    }

    private void isAllowedUpdate(PurchaseItem purchaseItem) {
        if (extra_receipt.isDoneByMerchant() &&
                extra_receipt.isReceivedBySupervisor()) {
            Toast.makeText(activity, "Not Allowed,it's Done", Toast.LENGTH_SHORT).show();
        } else if (extra_receipt.isDoneByMerchant()) {
            Toast.makeText(activity, "Not Allowed,it's Done By Merchant", Toast.LENGTH_SHORT).show();
        } else if (extra_receipt.isReceivedBySupervisor()) {
            Toast.makeText(activity, "Not Allowed,it's Done by Supervisor", Toast.LENGTH_SHORT).show();
        } else if (extra_receipt.isSentByMerchant()) {
            Toast.makeText(activity, "Not Allowed,it's under delivering", Toast.LENGTH_SHORT).show();
        } else if (extra_receipt.isPriced()) {
            Toast.makeText(activity, "Not Allowed,it's Priced", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(activity, PurchaseItemOperations.class);
            i.putExtra("update_object_purchaseItem", purchaseItem);
            if (activity.getIntent().hasExtra("Type")) {
                if (AccountType.equalsIgnoreCase("Supervisor")) {
                    i.putExtra("Type", "Supervisor");
                } else if (AccountType.equalsIgnoreCase("Merchant")) {
                    i.putExtra("Type", "Merchant");
                }
            }
            activity.startActivity(i);
        }
    }

    private void showAlertDialog(PurchaseItem purchaseItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Purchases").child(purchaseItem.getId()).removeValue();
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