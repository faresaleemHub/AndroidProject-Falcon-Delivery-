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

import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class PreviewDistributorAdapter extends RecyclerView.Adapter<PreviewDistributorAdapter.ViewHolder> {

    private ArrayList<Employee> data;
    private Activity activity;

    public PreviewDistributorAdapter(ArrayList<Employee> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_destributors_admin_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Employee employee=data.get(position);
        holder.tv_title.setText(employee.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = activity.findViewById(R.id.tv_DistributorName);
                tv.setText(data.get(position).getUsername());
                TextView tv2 = activity.findViewById(R.id.tv_DistributorID_V);
                tv2.setText(data.get(position).getId());
            }
        });
        if (employee.getProfileImgURI() != null) {
            File localFile = new File(activity.getFilesDir(), employee.getProfileImgURI());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                        .child(employee.getProfileImgURI()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.ImgProfileDistributor.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.ImgProfileDistributor.setImageURI(Uri.fromFile(localFile));
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
        TextView tv_title;
        ImageView ImgProfileDistributor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_Distributor_username);
            ImgProfileDistributor=itemView.findViewById(R.id.ImgProfileDistributor);
        }
    }
}
