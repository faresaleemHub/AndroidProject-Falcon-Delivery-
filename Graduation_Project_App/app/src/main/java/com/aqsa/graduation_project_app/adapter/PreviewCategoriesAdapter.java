package com.aqsa.graduation_project_app.adapter;

import android.annotation.SuppressLint;
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
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations.CategoryOperations;
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

public class PreviewCategoriesAdapter
        extends RecyclerView.Adapter<PreviewCategoriesAdapter.ViewHolder> {

    public ArrayList<Category> data;
    private Activity activity;
    private PopupMenu popupMenu;

    public PreviewCategoriesAdapter(Activity activity, ArrayList<Category> data) {
        this.activity = activity;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_categories_supervisor_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Category category = data.get(position);
        holder.tv_name.setText(category.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v, category, position);
            }
        });

        if (category.getImg() != null) {
            File localFile = new File(activity.getFilesDir(), category.getImg());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().
                        getReference("CategoryImages")
                        .child(category.getImg()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.img.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.img.setImageURI(Uri.fromFile(localFile));
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
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_category_name);
            img = itemView.findViewById(R.id.ImgCategoryHolder);
        }
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Category category, int position) {
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
        popup_menu_action(category, position);
    }

    @SuppressLint("RestrictedApi")
    private void popup_menu_action(Category category, int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_details2) {
                    Intent i = new Intent(activity, CategoryOperations.class);
                    i.putExtra("details_object_category", category);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_update2) {
                    Intent i = new Intent(activity, CategoryOperations.class);
                    i.putExtra("update_object_category", category);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                } else if (id == R.id.PopUpMenu_delete2) {
                    showAlertDialog(category);
                }
                return true;
            }
        });
    }

    private void deleteProductsOfCategory(String categoryId) {
        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Product.class).getCategoryID().equals(categoryId)) {
                        String p_id = snapshot1.getValue(Product.class).getId();
                        FirebaseDatabase.getInstance().getReference().child("Products").child(p_id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAlertDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Categories").child(category.getId()).removeValue();
                        deleteProductsOfCategory(category.getId());
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
