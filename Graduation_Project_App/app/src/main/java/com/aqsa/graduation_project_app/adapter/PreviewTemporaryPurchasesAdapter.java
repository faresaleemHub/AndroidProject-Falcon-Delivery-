package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class PreviewTemporaryPurchasesAdapter extends RecyclerView.Adapter<PreviewTemporaryPurchasesAdapter.ViewHolder> {

    private ArrayList<PurchaseItem> data;
    private Activity activity;

    public PreviewTemporaryPurchasesAdapter(ArrayList<PurchaseItem> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).
                inflate(R.layout.viewholder_preview_purchase_items_supervisor_activity,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_count_items.setVisibility(View.VISIBLE);
        holder.tv_count_items.setText(data.get(position).getQuantity()+"");
        selectPurchaseDetails(data.get(position).getProductID(),holder.tv_purchaseItemName,holder.img_purchaseItem);

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
        TextView tv_purchaseItemName,tv_count_items;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_purchaseItem=itemView.findViewById(R.id.img_purchaseItem);
            tv_purchaseItemName=itemView.findViewById(R.id.tv_purchaseItemName);
            tv_count_items=itemView.findViewById(R.id.tv_count_items);
        }
    }

    public void selectPurchaseDetails(String Product_ID, TextView tv_purchaseItemName, ImageView img_purchaseItem){
        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    if (snapshot1.getValue(Product.class).getId().equals(Product_ID)){
                        Product product=snapshot1.getValue(Product.class);
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


}
