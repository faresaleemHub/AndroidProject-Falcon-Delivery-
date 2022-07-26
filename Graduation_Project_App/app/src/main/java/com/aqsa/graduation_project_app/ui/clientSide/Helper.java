package com.aqsa.graduation_project_app.ui.clientSide;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

public class Helper {

    boolean duplicated=false;
    int totalItems=0;
    int totalPrice=0;

    public void InsertOrderItem(ClientOrderItem item){
        for (int i=0;i<CardListFragment.orderedItemsList.size();i++){
            ClientOrderItem item1=CardListFragment.orderedItemsList.get(i);
            if (item1.getProduct_ID().equals(item.getProduct_ID())){
                int quantity1=Integer.parseInt(item1.getQuantity());
                int quantity2=Integer.parseInt(item.getQuantity());
                CardListFragment.orderedItemsList.get(i).setQuantity(""+(quantity1+quantity2));
                duplicated=true;
            }
        }
        if (duplicated==false)
            CardListFragment.orderedItemsList.add(item);
    }

    public void selectProductDetails(Activity activity,String P_ID, ImageView product_img, TextView tv_title){
        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    if (snapshot1.getValue(Product.class).getId().equals(P_ID)){
                        Product product=snapshot1.getValue(Product.class);
                        tv_title.setText(product.getName());
                        if (product.getImg() != null){
                            File localFile = new File(activity.getFilesDir(), product.getImg());
                            if (!localFile.exists()) {
                                FileDownloadTask task = FirebaseStorage.getInstance().getReference("ProductImages")
                                        .child(product.getImg()).getFile(localFile);
                                //here will loaded the image from firebase into the file Directory on the device
                                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            product_img.setImageURI(Uri.fromFile(localFile));
                                        }
                                    }
                                });
                            } else {
                                product_img.setImageURI(Uri.fromFile(localFile));
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

    public void selectTotalNumberItemsOrdered(TextView tv_totalItems){
        for (int i=0;i<CardListFragment.orderedItemsList.size();i++){
            ClientOrderItem item=CardListFragment.orderedItemsList.get(i);
            totalItems+=Integer.parseInt(item.getQuantity());
        }
        tv_totalItems.setText(""+totalItems);
    }

    public void selectTotalPriceItemsOrdered(TextView tv_totalPrice){
        for (int i=0;i<CardListFragment.orderedItemsList.size();i++){
            ClientOrderItem item=CardListFragment.orderedItemsList.get(i);
            int price = Integer.parseInt(item.getBuyPrice());
            int quantity = Integer.parseInt(item.getQuantity());
            int totalItemPrice= price*quantity;
            int tv_price = Integer.parseInt(tv_totalPrice.getText().toString());
            totalPrice+=totalItemPrice+tv_price;
        }
        tv_totalPrice.setText(""+totalPrice);
    }

}
