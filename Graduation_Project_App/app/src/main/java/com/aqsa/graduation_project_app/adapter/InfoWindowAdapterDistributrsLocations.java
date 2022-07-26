package com.aqsa.graduation_project_app.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.MonitoringDistributorsMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.HashMap;

public class InfoWindowAdapterDistributrsLocations implements GoogleMap.InfoWindowAdapter {


    Context context;
    HashMap<String, Employee> hashMap;

    public InfoWindowAdapterDistributrsLocations(Context context, HashMap<String, Employee> hashMap) {
        this.context = context;
        this.hashMap = hashMap;
    }


    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        View v = LayoutInflater.from(context).inflate(R.layout.marker_widget, null);
        TextView tv = v.findViewById(R.id.tv_person_name);
        ImageView imgProfile = v.findViewById(R.id.img_person_photo);

        for (int i = 0; i < hashMap.size(); i++) {
            Employee employee = hashMap.get(marker.getTag());//here will get all markers available
            // on te map , and I'll get them Id which stored before in the hash , and u=by the id I'll access the Employees tore in hash
            // when there's no employee according to the id , here am on Company marker , and then I'll deal with it in different way

            if (employee != null) {
                if (marker.getTag().toString().equalsIgnoreCase(employee.getId())) {
                    if (employee.getProfileImgURI() != null) {
                        imgProfile.setVisibility(View.VISIBLE);
                        File localFile = new File(context.getFilesDir(), employee.getProfileImgURI());
                        //here we created a directory
                        if (!localFile.exists()) {
                            FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                                    .child(employee.getProfileImgURI()).getFile(localFile);
                            //here will loaded the image from firebase into the file Directory on the device
                            task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        imgProfile.setImageURI(Uri.fromFile(localFile));
                                    }
                                }
                            });
                        } else {
                            imgProfile.setImageURI(Uri.fromFile(localFile));
                        }
                    }

                    if (employee.getUsername() != null) {
                        tv.setVisibility(View.VISIBLE);
                        tv.setText(employee.getUsername());
                    }
                }
            } else if (employee == null) {
                if (marker.getTag().equals(MonitoringDistributorsMap.COMPANY_MARKER_TAG)) {
                    tv.setText("Falcon Delivery Company");
                    tv.setVisibility(View.VISIBLE);
                    imgProfile.setVisibility(View.GONE);
                }
            }
        }
        return v;
    }
}
