package com.aqsa.graduation_project_app.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.io.File;
import java.util.List;

public class SliderAdapterProducts extends
        SliderViewAdapter<SliderAdapterProducts.SliderAdapterVH> {

    private Context context;
    private List<Product> data;
    private OnRV_ClickListener_Product listener;

    public SliderAdapterProducts(Context context, List<Product> mSliderItems, OnRV_ClickListener_Product listener) {
        this.context = context;
        this.data = mSliderItems;
        this.listener = listener;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_adapter_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH holder, final int position) {
        Product product = data.get(position);

        holder.textView.setText(product.getName());

        if (product.getImg() != null) {
            File localFile = new File(context.getFilesDir(), product.getImg());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().
                        getReference("ProductImages")
                        .child(product.getImg()).getFile(localFile);
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.imageView.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.imageView.setImageURI(Uri.fromFile(localFile));
            }
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnItemClick(data.get(position));
            }
        });
    }

    @Override
    public int getCount() {
        return data.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        ImageView imageView;
        TextView textView;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_auto_image_slider);
            textView = itemView.findViewById(R.id.tv_auto_image_slider);
        }
    }

}
