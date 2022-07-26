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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.ProductsOperations;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreviewProductAdapter extends RecyclerView.Adapter<PreviewProductAdapter.ViewHolder> {

    private ArrayList<Product> data;
    private Activity activity;
    private PopupMenu popupMenu;

    public PreviewProductAdapter(ArrayList<Product> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_products_supervisor_side, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = data.get(position);
        holder.tv_name.setText(product.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v, product, position);
            }
        });
        if (product.getImg() != null) {
            File localFile = new File(activity.getFilesDir(), product.getImg());
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().getReference("ProductImages")
                        .child(product.getImg()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.ImgProduct.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.ImgProduct.setImageURI(Uri.fromFile(localFile));
            }
        }

    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        ImageView ImgProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ImgProduct = itemView.findViewById(R.id.VH_imgProduct);
            tv_name = itemView.findViewById(R.id.VH_tv_product_name);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Product product, int position) {
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
        }catch (Exception e){

        }

        popupMenu.show();
        popup_menu_action(product, position);
    }

    private void popup_menu_action(Product product, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details2) {
                    Intent i = new Intent(activity, ProductsOperations.class);
                    i.putExtra("details_object_product", product);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_update2) {
                    Intent i = new Intent(activity, ProductsOperations.class);
                    i.putExtra("update_object_product", product);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete2) {
                    showAlertDialog(product);
                }
                return true;
            }
        });
    }

    private void showAlertDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Products").child(product.getId()).removeValue();
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
