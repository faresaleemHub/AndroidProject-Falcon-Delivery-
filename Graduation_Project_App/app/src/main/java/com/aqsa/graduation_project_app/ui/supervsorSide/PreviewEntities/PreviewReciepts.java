package com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewReceiptsAdapter;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PreviewReciepts extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    ArrayList<Receipt> receipt_data_list;
    PreviewReceiptsAdapter adapter;
    CountDownTimer countDownTimer, countDownTimer2,countDownTimer3;

    Spinner spinner_receipts_merchants;
    ArrayList<String> merchantsList;
    String[] merchantsArray;
    ArrayAdapter data;
    String merchantName, merchantID;
    CollapsingToolbarLayout ctl;
    String target = "";//to save the getStringExtra from before Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_reciepts);

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        progressBar = findViewById(R.id.progressBar_preview_receipts);
        rv = findViewById(R.id.rv_previewReceipts);
        spinner_receipts_merchants = findViewById(R.id.spinner_receipts_merchants);
        ctl = findViewById(R.id.collabsing_toolbar_previewReceipt);

        receipt_data_list = new ArrayList<>();
        receipt_data_list.clear();

        if (getIntent().hasExtra("show_done")) {
            target = "show_done";
            ctl.setTitle("Done Receipts");
        } else if (getIntent().hasExtra("show_pending")) {
            target = "show_pending";
            ctl.setTitle("Pending Receipts");
        }


        adapter = new PreviewReceiptsAdapter(receipt_data_list,
                PreviewReciepts.this, target);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (!getIntent().hasExtra("Dealing_History_Merchant")) {

            selectMerchantsToSpinner();

            //selecting spinner data , and to check if there's data into it.
            progressBar.setVisibility(View.VISIBLE);
            countDownTimer3 = new CountDownTimer(4000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (spinner_receipts_merchants.getSelectedItem() != null) {
                        countDownTimer3.cancel();
                    }
                }

                @Override
                public void onFinish() {
                    if (spinner_receipts_merchants.getSelectedItem() == null) {
                        progressBar.setVisibility(View.GONE);
                        GroupFunctions.EmptyToast(PreviewReciepts.this, "No Reciepts Yet");
                        TimerClose();
                    }
                }
            }.start();

            spinner_receipts_merchants.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    merchantName = spinner_receipts_merchants.getSelectedItem().toString();
                    if (merchantName.equals("All"))
                        selectReceipts("All");
                    else
                        selectMerchantID(merchantName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        if (getIntent().hasExtra("Dealing_History_Merchant")) {
            spinner_receipts_merchants.setVisibility(View.GONE);
            findViewById(R.id.tv_company_tv).setVisibility(View.GONE);
            Merchant merchant = (Merchant) getIntent().getSerializableExtra("Dealing_History_Merchant");
            ctl.setTitle("Dealing History");
            selectReceipts(merchant.getId());
        }
    }

    public void selectMerchantsToSpinner() {
        merchantsList = new ArrayList<>();
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                merchantsList.add("All");
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    merchantsList.add(snapshot1.getValue(Merchant.class).getName());
                    merchantsArray = merchantsList.toArray(new String[0]);
                    data = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_spinner_item,
                            merchantsArray);
                    data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_receipts_merchants.setAdapter(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void selectMerchantID(String M_Name) {
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Merchant.class).getName().equals(M_Name)) {
                        merchantID = snapshot1.getValue(Merchant.class).getId();
                        selectReceipts(merchantID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void selectReceipts(String M_ID) {
        receipt_data_list.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);

        db.getReference().child("Receipts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                sortAddReceipt(receipt, receipt_data_list.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                boolean founded = false;

                for (int i = 0; i < receipt_data_list.size(); i++) {
                    if (receipt_data_list.get(i).getId().equals(receipt.getId())) {
                        founded = true;
                        receipt_data_list.remove(i);
                        sortAddReceipt(receipt, i);
                    }
                }

                if (founded == false) {
                    sortAddReceipt(receipt, receipt_data_list.size());
                }

                if (receipt_data_list.size() == 0) {
                    finish();
                    GroupFunctions.finishTransitionStyle(PreviewReciepts.this);
                }
            }

            private void sortAddReceipt(Receipt receipt, int i) {
                //add
                if (getIntent().hasExtra("show_done") || getIntent().hasExtra("Dealing_History_Merchant")) {
                    if (M_ID.equals("All") && receipt.isReceivedBySupervisor()) {
                        receipt_data_list.add(receipt);
                    } else if (receipt.getMerchantID().equals(M_ID) &&
                            receipt.isReceivedBySupervisor()) {
                        receipt_data_list.add(receipt);
                    }
                } else if (getIntent().hasExtra("show_pending")) {
                    if (M_ID.equals("All") &&
                            !receipt.isReceivedBySupervisor()) {
                        receipt_data_list.add(receipt);
                    } else if (receipt.getMerchantID().equals(M_ID)
                            && !receipt.isReceivedBySupervisor()) {
                        receipt_data_list.add(receipt);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < receipt_data_list.size(); i++) {
                    if (receipt_data_list.get(i).getId().equals(snapshot.getValue(Receipt.class).getId())) {
                        receipt_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }

                    if (receipt_data_list.size() == 0) {
                        GroupFunctions.EmptyToast(PreviewReciepts.this,"No Reciepts Yet");
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

        countDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (receipt_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (receipt_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewReciepts.this, "No Reciepts Yet");
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    public void TimerClose() {
        countDownTimer2 = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                GroupFunctions.finishTransitionStyle(PreviewReciepts.this);
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

        if (getIntent().hasExtra("Dealing_History_Merchant")) {
            startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
            GroupFunctions.finishTransitionStyle(PreviewReciepts.this);
        } else {
            startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
            GroupFunctions.finishTransitionStyle(PreviewReciepts.this);
        }
    }
}