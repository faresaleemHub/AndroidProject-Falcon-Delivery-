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
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.ClientOperations;
import com.aqsa.graduation_project_app.ui.adminSide.previewEntyties.PreviewClients;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
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

public class PreviewClientsAdapter
        extends RecyclerView.Adapter<PreviewClientsAdapter.ViewHolder> {

    private ArrayList<Client> data;
    private Activity activity;
    private PopupMenu popupMenu;
    private Employee employee;

    public PreviewClientsAdapter(ArrayList<Client> data, Activity activity,Employee employee) {
        this.data = data;
        this.activity = activity;
        this.employee=employee;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_clients_admin_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Client client = data.get(position);
        holder.tv_username.setText(client.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu(v, client, position);
            }
        });
        if (client.getProfileImgURI() != null){
                File localFile = new File(activity.getFilesDir(), client.getProfileImgURI());
                //here we created a directory
                if (!localFile.exists()) {
                    FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                            .child(client.getProfileImgURI()).getFile(localFile);
                    task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                holder.ImgProfileClient.setImageURI(Uri.fromFile(localFile));
                            }
                        }
                    });
                } else {
                    holder.ImgProfileClient.setImageURI(Uri.fromFile(localFile));
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
        TextView tv_username;
        ImageView ImgProfileClient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_username=itemView.findViewById(R.id.tv_client_username);
            ImgProfileClient=itemView.findViewById(R.id.ImgProfileClient);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v, Client client,int position) {
        popupMenu=new PopupMenu(activity,v);
        activity.getMenuInflater().inflate(R.menu.popup_menu1,popupMenu.getMenu());
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

        popup_menu_action(client,position);
    }

    private void popup_menu_action(Client client,int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.PopUpMenu_details) {
                    Intent i =new Intent(activity, ClientOperations.class);
                    i.putExtra("details_object_client",client);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }else if (id==R.id.PopUpMenu_update) {
                    Intent i =new Intent(activity, ClientOperations.class);
                    i.putExtra("update_object_client",client);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }else if (id==R.id.PopUpMenu_delete){
                    showAlertDialog(client);
                }else if (id==R.id.PopUpMenu_Call){
                    Intent i=new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:"+client.getPhone()));
                    Intent i2= Intent.createChooser(i,"choose the calling App you need");
                    activity.startActivity(i2);
                    GroupFunctions.openTransitionStyle(activity);
                }else if(id==R.id.PopUpMenu_DealingHistory_Client){
                    Intent i =new Intent(activity, PreviewOrders.class);
                    i.putExtra("Dealing_History_Client",client);
                    activity.startActivity(i);
                    GroupFunctions.openTransitionStyle(activity);
                }
                return true;
            }
        });
    }

    private void showAlertDialog(Client client1){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        FirebaseAuth auth=FirebaseAuth.getInstance();
                        auth.signOut();
                        auth.signInWithEmailAndPassword(client1.getEmail(),client1.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        auth.signInWithEmailAndPassword(employee.getEmail(),employee.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Clients").child(client1.getId()).removeValue();
                                                Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
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
