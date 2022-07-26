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
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.MerchantOperations;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewClients;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewReciepts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreviewMerchantsAdapter extends
        RecyclerView.Adapter<PreviewMerchantsAdapter.ViewHolder> {

    private ArrayList<Merchant> data;
    private Activity activity;
    PopupMenu popupMenu;
    Employee employee;

    public PreviewMerchantsAdapter(ArrayList<Merchant> data, Activity activity,Employee employee) {
        this.data = data;
        this.activity = activity;
        this.employee=employee;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_merhcants_admin_activity,
                parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Merchant merchant=data.get(position);
        holder.tv_companyName.setText(merchant.getCompanyName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v,merchant,position);
            }
        });

        if (merchant.getProfileImgURI() != null) {
            File localFile = new File(activity.getFilesDir(), merchant.getProfileImgURI());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                        .child(merchant.getProfileImgURI()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.ImgProfileMerchant.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                holder.ImgProfileMerchant.setImageURI(Uri.fromFile(localFile));
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
        TextView tv_companyName;
        ImageView ImgProfileMerchant;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_companyName=itemView.findViewById(R.id.tv_merchant_company_name);
            ImgProfileMerchant=itemView.findViewById(R.id.picMerchant);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Merchant merchant,int position) {
        popupMenu=new PopupMenu(activity,v);
        activity.getMenuInflater().inflate(R.menu.popup_menu21,popupMenu.getMenu());
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

        popup_menu_action(merchant,position);
    }

    private void popup_menu_action(Merchant merchant,int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.PopUpMenu_details) {
                    Intent i =new Intent(activity, MerchantOperations.class);
                    i.putExtra("details_object_merchant",merchant);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }else if (id==R.id.PopUpMenu_update) {
                    Intent i =new Intent(activity, MerchantOperations.class);
                    i.putExtra("update_object_merchant",merchant);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }else if (id==R.id.PopUpMenu_delete){
                   showAlertDialog(merchant,position);
                } else if (id==R.id.PopUpMenu_Call){
                    Intent i=new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:"+merchant.getPhoneNumber()));
                    Intent i2= Intent.createChooser(i,"choose the calling App you need");
                    activity.startActivity(i2);
                    GroupFunctions.openTransitionStyle(activity);
                }else if (id==R.id.PopUpMenu_DealingHistory_Merchant){
                    Intent i=new Intent(activity, PreviewReciepts.class);
                    i.putExtra("Dealing_History_Merchant",merchant);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }

    private void showAlertDialog(Merchant merchant, int position){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        FirebaseAuth auth=FirebaseAuth.getInstance();
                        auth.signOut();
                        auth.signInWithEmailAndPassword(merchant.getEmail(),merchant.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        auth.signInWithEmailAndPassword(employee.getEmail(),employee.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Merchants").child(merchant.getId()).removeValue();
                                                Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        activity.finish();
                        GroupFunctions.finishTransitionStyle(activity);

                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setMessage("this item will be deleted");
        AlertDialog alert = builder.create();
        alert.show();
    }
}