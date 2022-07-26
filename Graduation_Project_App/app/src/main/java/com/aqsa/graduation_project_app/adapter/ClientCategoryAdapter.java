package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Category;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class ClientCategoryAdapter extends RecyclerView.Adapter<ClientCategoryAdapter.ViewHolder>{

    public ArrayList<Category> data;
    private Activity activity;//or we can use " parent.getcontext()"
    private OnRV_ClickListener_Category listener;

    public ClientCategoryAdapter(ArrayList<Category> data, Activity activity,OnRV_ClickListener_Category listener) {
        this.data = data;
        this.activity = activity;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_categories_client_side,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category=data.get(position);
        holder.categoryName.setText(category.getName());
        if (category.getImg() != null) {
            File localFile = new File(activity.getFilesDir(), category.getImg());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().
                        getReference("CategoryImages")
                        .child(category.getImg()).getFile(localFile);
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.categoryPic.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.categoryPic.setImageURI(Uri.fromFile(localFile));
            }
        }

        int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnItemClick(data.get(finalPosition));
            }
        });
        String picUrl="";
        switch (position) {
            case 0: {
                picUrl = "cat_1";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(
                        holder.itemView.getContext(), R.drawable.category_background1));
                break;
            }
            case 1: {
                picUrl = "cat_2";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_background2));
                break;
            }
            case 2: {
                picUrl = "cat_3";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_background3));
                break;
            }
            case 3: {
                picUrl = "cat_4";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_background4));
                break;
            }
            case 4: {
                picUrl = "cat_5";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_background5));
                break;
            }
            default:
                position%=5;
        }
    }

    @Override
    public int getItemCount() {
        if (data!=null)
            return data.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryPic;
        ConstraintLayout mainLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName_client);
            categoryPic = itemView.findViewById(R.id.categoryPic_client);
            mainLayout=itemView.findViewById(R.id.v_h_c_c_layout);
        }
    }
}
