package com.aqsa.graduation_project_app.ui.merchantSide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.MerchantOrdersAdapter;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MerchantOrders extends GroupFunctions {

    FirebaseDatabase db;
    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    ArrayList<Receipt> orders_receipt_data_list;
    MerchantOrdersAdapter adapter;
    CountDownTimer countDownTimer, countDownTimer2;
    Merchant merchant;
    CollapsingToolbarLayout ctl;
    FloatingActionButton fab;

    String target = "";//to save the getStringExtra from before Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_orders__merchant);

        checkAuthenticity();
        selectSharedMerchantAccount();

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        orders_receipt_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewPendingOrdersMerchant);
        rv = findViewById(R.id.rv_previewReceivedOrders_merchant);
        ctl = findViewById(R.id.collabsing_toolbar_merchant_orders);
        fab = findViewById(R.id.fab12);

        if (getIntent().hasExtra("show_received")) {
            target = "show_received";
            ctl.setTitle("Preview Orders");
            fab.setImageResource(R.drawable.ic_telegram);
        } else if (getIntent().hasExtra("show_pending")) {
            target = "show_pending";
            ctl.setTitle("Pending Orders");
            fab.setImageResource(R.drawable.ic_timer);

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

        } else if (getIntent().hasExtra("show_done")) {
            target = "show_done";
            ctl.setTitle("Done Orders");
            fab.setImageResource(R.drawable.ic_done);
            fab.setColorFilter(Color.BLACK);
        }

        adapter = new MerchantOrdersAdapter(
                MerchantOrders.this, orders_receipt_data_list, target);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        selectOrders();

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

    private void selectOrders() {
        progressBar.setVisibility(View.VISIBLE);
        db.getReference().child("Receipts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                if (receipt.getMerchantID().equals(merchant.getId())) {
                    if (getIntent().hasExtra("show_done")) {
                        if (receipt.isDoneByMerchant())
                            orders_receipt_data_list.add(receipt);
                    } else if (getIntent().hasExtra("show_pending")) {
                        if (receipt.isSentByMerchant() && !receipt.isDoneByMerchant())
                            orders_receipt_data_list.add(receipt);
                    } else if (getIntent().hasExtra("show_received")) {
                        if (!receipt.isSentByMerchant())
                            orders_receipt_data_list.add(receipt);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                    if (orders_receipt_data_list.get(i).getId().equals(snapshot.getValue(Receipt.class).getId())) {
                        Receipt receipt = snapshot.getValue(Receipt.class);
                        orders_receipt_data_list.remove(i);
                        if (getIntent().hasExtra("show_done")) {
                            if (receipt.getMerchantID().equals(merchant.getId())
                                    && receipt.isDoneByMerchant()) {
                                orders_receipt_data_list.add(i, receipt);
                                adapter.notifyDataSetChanged();
                            }
                        } else if (getIntent().hasExtra("show_pending")) {
                            if (receipt.isSentByMerchant() && !receipt.isDoneByMerchant() &&
                                    receipt.getMerchantID().equals(merchant.getId())) {
                                orders_receipt_data_list.add(i, receipt);
                                adapter.notifyDataSetChanged();
                            }
                        } else if (getIntent().hasExtra("show_received")) {
                            if (receipt.getMerchantID().equals(merchant.getId())
                                    && !receipt.isSentByMerchant()) {
                                orders_receipt_data_list.add(i, receipt);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    if (orders_receipt_data_list.size() == 0) {
                        startActivity(new Intent(MerchantOrders.this, MerchantDashboard.class));
                        GroupFunctions.finishTransitionStyle(MerchantOrders.this);
                    }
                }
            }


            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                    if (orders_receipt_data_list.get(i).getId().equals(snapshot.getValue(Receipt.class).getId())) {
                        orders_receipt_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }

                    if (orders_receipt_data_list.size() == 0) {
                        startActivity(new Intent(MerchantOrders.this, MerchantDashboard.class));
                        GroupFunctions.finishTransitionStyle(MerchantOrders.this);
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

        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (orders_receipt_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (orders_receipt_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(MerchantOrders.this, "No Orders Yet");
                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MerchantOrders.this);
        Receipt receipt1 = orders_receipt_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Receipts").child(receipt1.getId()).removeValue();
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
                finish();
            }
        }.start();
    }


    public void selectSharedMerchantAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        merchant = gson.fromJson(json, Merchant.class);
    }

    public void checkAuthenticity() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
        startActivity(new Intent(getApplicationContext(), MerchantDashboard.class));
        GroupFunctions.finishTransitionStyle(MerchantOrders.this);
    }
}