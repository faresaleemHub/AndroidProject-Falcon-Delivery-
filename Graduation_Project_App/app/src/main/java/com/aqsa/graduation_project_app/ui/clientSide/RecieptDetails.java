package com.aqsa.graduation_project_app.ui.clientSide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.ClientCardListItemsAdapter;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.ClientOperations;
import com.aqsa.graduation_project_app.ui.distributorSide.DistributorOrders;
import com.aqsa.graduation_project_app.ui.distributorSide.MonitoringDistributorOrders;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

//this activity for displaying the details of recipts for different Entities but not Clients
public class RecieptDetails extends AppCompatActivity {

    ClientCardListItemsAdapter adapter;
    RecyclerView rv_cardItems;
    TextView totalCardItems, totalCardPrice, btn_chk_out,btn_preview_profile;
    EditText ed_latPoint, ed_longPoint, ed_receiptGovernorate;
    ProgressBar progress;

    CountDownTimer countDownTimer;

    ClientOrderReceipt co_receipt;
    Client client;

    int totalItems = 0;
    int totalPrice = 0;

    FirebaseDatabase db;

    ArrayList<ClientOrderItem> dataList;

    String extra_source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reciept_details);

        db = FirebaseDatabase.getInstance();
        selectSharedClientAccount();
        dataList = new ArrayList<>();
        initView();
        recyclerView();

        if (getIntent().hasExtra("details_object_client_receipt")) {
            co_receipt = (ClientOrderReceipt) getIntent().getExtras().getSerializable("details_object_client_receipt");
            if (co_receipt != null) {
                if (getIntent().hasExtra("source"))
                    if (getIntent().getStringExtra("source").equals(MonitoringDistributorOrders.source)
                    ||getIntent().getStringExtra("source").equals(DistributorOrders.source)
                    ||getIntent().getStringExtra("source").equals(PreviewOrders.source)) {
                        extra_source=getIntent().getStringExtra("source");
                        btn_preview_profile.setVisibility(View.VISIBLE);
                        btn_preview_profile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                previewClient(co_receipt);
                            }
                        });
                    }

                ed_latPoint.setEnabled(false);
                ed_longPoint.setEnabled(false);
                ed_receiptGovernorate.setEnabled(false);

                ed_latPoint.setText(co_receipt.getLat_location());
                ed_longPoint.setText(co_receipt.getLong_location());
                ed_receiptGovernorate.setText("Governorate : " + co_receipt.getGovernorate());

                selectReceiptItems(co_receipt.getId());
                btn_chk_out.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progress.setVisibility(View.VISIBLE);
                        Intent i=new Intent();
                        i.putExtra("details_object_client_receipt","");
                        setResult(1,i);
                        finish();
                    }
                });
            }
        }
    }

    private void selectReceiptItems(String Receipt_ID) {
        progress.setVisibility(View.VISIBLE);
        dataList.clear();
        adapter.notifyDataSetChanged();
        db.getReference().child("ClientsReceiptOrdersItems").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(ClientOrderItem.class).getReceipt_ID().equals(Receipt_ID)) {
                    ClientOrderItem item = snapshot.getValue(ClientOrderItem.class);
                    dataList.add(snapshot.getValue(ClientOrderItem.class));
                    adapter.notifyDataSetChanged();

                    //total quantity
                    totalItems += Integer.parseInt(item.getQuantity());

                    //total price
                    int price = Integer.parseInt(item.getBuyPrice());
                    int quantity = Integer.parseInt(item.getQuantity());
                    int totalItemPrice = price * quantity;
                    totalPrice += totalItemPrice;

                }
                totalCardPrice.setText("" + totalPrice);
                totalCardItems.setText("" + totalItems);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer = new CountDownTimer(4500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (dataList.size()>0){
                    progress.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }
        }.start();
    }

    private void recyclerView() {
        adapter = new ClientCardListItemsAdapter(dataList, RecieptDetails.this, "just preview");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rv_cardItems.setLayoutManager(linearLayoutManager);
        rv_cardItems.setAdapter(adapter);
    }

    private void initView() {
        rv_cardItems = findViewById(R.id.rv_client_order_card_items_details);
        totalCardItems = findViewById(R.id.tv_totalItems_CardDetails);
        totalCardPrice = findViewById(R.id.tv_totalPrice_CardDetails);
        ed_latPoint = findViewById(R.id.input_client_lat_point_textDetails);
        ed_longPoint = findViewById(R.id.input_client_long_point_textDetails);
        btn_chk_out = findViewById(R.id.btn_chk_outDetails);
        progress = findViewById(R.id.progressBar_addClientReceiptOrderDetails);
        ed_receiptGovernorate = findViewById(R.id.input_client_receiptGovernorate_text);
        btn_preview_profile=findViewById(R.id.btn_preview_client_profile);
    }

    public void selectSharedClientAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        client = gson.fromJson(json, Client.class);
    }

    private void previewClient(ClientOrderReceipt receipt) {
        FirebaseDatabase.getInstance().getReference().child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Client.class).getId().equals(receipt.getClient_ID())) {
                        Intent i = new Intent(getApplicationContext(), ClientOperations.class);
                        i.putExtra("details_object_client", snapshot1.getValue(Client.class));
                        i.putExtra("source", extra_source);
                        startActivity(i);
                        GroupFunctions.openTransitionStyle(RecieptDetails.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void initToolBar(String title, int icon) {
        if (getIntent().hasExtra("target")) {
            if (getIntent().getStringExtra("target").equals(MonitoringDistributorOrders.source)) {
                Toolbar toolbar = findViewById(R.id.toolbar_layout);
                toolbar.setTitle(title);
                setSupportActionBar(toolbar);
                getSupportActionBar().setHomeAsUpIndicator(icon);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            findViewById(R.id.toolbar_layout).setVisibility(View.GONE);
        }
    }
}