package com.aqsa.graduation_project_app.ui.adminSide.previewEntyties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewClientsAdapter;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PreviewClients extends GroupFunctions {

    FirebaseDatabase db;
    Client client;

    ProgressBar progressBar;
    RecyclerView rv;
    GridLayoutManager layoutManager;
    ArrayList<Client> clients_data_list;
    PreviewClientsAdapter adapter;
    CountDownTimer countDownTimer,countDownTimer2;
    FirebaseAuth auth;
    Employee employee;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_clients__admin_);

        this.initToolBar( "",R.drawable.ic_back);
        db = FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        employee = selectSharedAccount();

        clients_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewClint);
        rv = findViewById(R.id.rv_previewClint);

        adapter = new PreviewClientsAdapter(clients_data_list,
                PreviewClients.this,employee);
        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        new MyAsyncTask().execute();

        //initialize item touch helper
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //when item swipe
                showAlertDialog(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(rv);
    }

    private void selectClients() {
        db.getReference().child("Clients").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                client = snapshot.getValue(Client.class);
                clients_data_list.add(client);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i=0;i<clients_data_list.size();i++) {
                    if (clients_data_list.get(i).getId().equals(snapshot.getValue(Client.class).getId())){
                        Client client=snapshot.getValue(Client.class);
                        clients_data_list.remove(i);
                        clients_data_list.add(i,client);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i=0;i<clients_data_list.size();i++) {
                    if (clients_data_list.get(i).getId().equals(snapshot.getValue(Client.class).getId())){
                        clients_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                    if (clients_data_list.size() == 0) {
                        startActivity(new Intent(PreviewClients.this,AdminDashboard.class));
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewClients.this);
        Client client1 = clients_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                                                Toast.makeText(PreviewClients.this, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        adapter.notifyItemChanged(position);
                    }
                })
                .setMessage("this item will be deleted");
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
    }

    public Employee selectSharedAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        Employee employee = gson.fromJson(json, Employee.class);
        return employee;
    }

    @Override
    public void finish() {
        super.finish();
        GroupFunctions.finishTransitionStyle(this);
    }

    class MyAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressBar.setVisibility(View.GONE);


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            selectClients();
            return null;
        }
    }
}