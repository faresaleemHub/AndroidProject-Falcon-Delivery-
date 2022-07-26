package com.aqsa.graduation_project_app.ui.adminSide.previewEntyties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.aqsa.graduation_project_app.adapter.PreviewMerchantsAdapter;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Merchant;
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

public class PreviewMerchant extends GroupFunctions {

    FirebaseDatabase db;
    ProgressBar progressBar;
    RecyclerView rv;
    GridLayoutManager layoutManager;
    ArrayList<Merchant> merchants_data_list;
    PreviewMerchantsAdapter adapter;
    CountDownTimer countDownTimer, countDownTimer2;
    Employee employee;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_merchant__admin_);

        this.initToolBar( "", R.drawable.ic_back);
        db = FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        employee = selectSharedAccount();

        merchants_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewMerchants);
        rv = findViewById(R.id.rv_previewMerchants);

        adapter = new PreviewMerchantsAdapter(merchants_data_list,
                PreviewMerchant.this,employee);

        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        selectMerchants();

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

    private void selectMerchants() {
        db.getReference().child("Merchants").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Merchant merchant = snapshot.getValue(Merchant.class);
                merchants_data_list.add(merchant);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i=0;i<merchants_data_list.size();i++) {
                    if (merchants_data_list.get(i).getId().equals(snapshot.getValue(Merchant.class).getId())){
                        Merchant merchant=snapshot.getValue(Merchant.class);
                        merchants_data_list.remove(i);
                        merchants_data_list.add(i,merchant);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i=0;i<merchants_data_list.size();i++) {
                    if (merchants_data_list.get(i).getId().equals(snapshot.getValue(Merchant.class).getId())){
                        merchants_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                    if (merchants_data_list.size() == 0) {
                        startActivity(new Intent(PreviewMerchant.this,AdminDashboard.class));
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

        progressBar.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(4500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (merchants_data_list.size()>0){
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (merchants_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewMerchant.this,"No Merchants Yet");
                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewMerchant.this);
        Merchant merchant1 = merchants_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        auth.signOut();
                        auth.signInWithEmailAndPassword(merchant1.getEmail(),merchant1.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        auth.signInWithEmailAndPassword(employee.getEmail(),employee.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Merchants").child(merchant1.getId()).removeValue();
                                                Toast.makeText(PreviewMerchant.this, "Done", Toast.LENGTH_SHORT).show();
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

    public void TimerClose() {
        countDownTimer2 = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
            }
        }.start();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
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
}