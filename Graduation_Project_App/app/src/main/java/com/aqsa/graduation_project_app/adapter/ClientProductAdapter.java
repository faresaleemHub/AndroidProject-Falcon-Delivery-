package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Product;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class ClientProductAdapter extends RecyclerView.Adapter<ClientProductAdapter.ViewHolder>{

    public ArrayList<Product> data;
    private Activity activity;
    private OnRV_ClickListener_Product listener;
    private int lastPosition=-1;

    public ClientProductAdapter(ArrayList<Product> data, Activity activity,
                                OnRV_ClickListener_Product listener) {
        this.data = data;
        this.activity = activity;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_recent_product_client_side,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product=data.get(position);
        holder.productTitle.setText(product.getName());
        holder.productPrice.setText(product.getPrice());
        if (product.getImg() != null) {
            File localFile = new File(activity.getFilesDir(), product.getImg());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().
                        getReference("ProductImages")
                        .child(product.getImg()).getFile(localFile);
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.productPic.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.productPic.setImageURI(Uri.fromFile(localFile));
            }
        }

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnItemClick(data.get(position));
            }
        });

        //set animation on recyclerview item
        setAnimation(holder.itemView,position);
    }

    private void setAnimation(View itemView, int position) {
        if (position>lastPosition){
            //initialize animation
            Animation animation= AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left);
            //set animation
            itemView.setAnimation(animation);
            //set current position into last position
            lastPosition=position;
        }
    }

    @Override
    public int getItemCount() {
        if (data!=null)
            return data.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle,productPrice,addBtn;
        ImageView productPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle_client);
            productPrice=itemView.findViewById(R.id.productPrice_client);
            addBtn=itemView.findViewById(R.id.productAddBtn_client);
            productPic = itemView.findViewById(R.id.productPic_client);
        }
    }
}
