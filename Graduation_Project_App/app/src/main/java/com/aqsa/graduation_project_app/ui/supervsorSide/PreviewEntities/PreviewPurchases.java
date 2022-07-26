package com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewPurchaseItemsAdapter;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.entityOperations.MerchantOperations;
import com.aqsa.graduation_project_app.ui.merchantSide.MerchantDashboard;
import com.aqsa.graduation_project_app.ui.merchantSide.MerchantOrders;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PreviewPurchases extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    PreviewPurchaseItemsAdapter adapter;
    ArrayList<PurchaseItem> purchaseItem_data_list;
    CountDownTimer countDownTimer, countDownTimer2;
    Receipt extra_receipt;
    TextView tv_totalItems, tv_totalPrice, tv_receipt_done, tv_preview_merchant_profile;
    int totalPrice = 0, totalQuantity = 0;
    String extra_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_purchases);

        if (getIntent().hasExtra("details_object_receipt"))
            extra_receipt = (Receipt) getIntent().getExtras().getSerializable("details_object_receipt");

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        progressBar = findViewById(R.id.progressBar_previewPurchaseItems);
        rv = findViewById(R.id.rv_previewPurchaseItems);
        tv_receipt_done = findViewById(R.id.tv_receipt_done);
        tv_totalItems = findViewById(R.id.tv_totalItems_receipt);
        tv_totalPrice = findViewById(R.id.tv_totalPrice_receipt);
        tv_preview_merchant_profile = findViewById(R.id.tv_preview_merchant_profile);

        purchaseItem_data_list = new ArrayList<>();

        adapter = new PreviewPurchaseItemsAdapter(purchaseItem_data_list,
                PreviewPurchases.this, extra_receipt);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        selectPurchases();

        if (getIntent().hasExtra("details_object_receipt"))
            if (getIntent().hasExtra("Type"))
                if (getIntent().getStringExtra("Type").equals("Supervisor")) {
                    tv_preview_merchant_profile.setVisibility(View.VISIBLE);
                    Receipt receipt = (Receipt) getIntent().getExtras().getSerializable("details_object_receipt");
                    extra_type=getIntent().getStringExtra("Type");
                    tv_preview_merchant_profile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            previewMerchant(receipt);
                        }
                    });
                }

        tv_receipt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().hasExtra("Type") && getIntent().hasExtra("target")) {
                    String type = getIntent().getStringExtra("Type").toString();
                    String target = getIntent().getStringExtra("target").toString();

//                    Toast.makeText(getApplicationContext(), "the type is:" + type, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getApplicationContext(), "the target is:" + target, Toast.LENGTH_SHORT).show();
                    if (type.equals("Merchant")) {
                        Intent i = new Intent(PreviewPurchases.this, MerchantOrders.class);
                        i.putExtra(target, target);
                        startActivity(i);
                        GroupFunctions.openTransitionStyle(PreviewPurchases.this);
                    } else if (type.equals("Supervisor")) {
                        Intent i = new Intent(PreviewPurchases.this, SupervsorDashboard.class);
                        startActivity(i);
                        GroupFunctions.finishTransitionStyle(PreviewPurchases.this);
                    }
                }
            }
        });

        if (getIntent().hasExtra("Type") && getIntent().hasExtra("target")) {
            String type = getIntent().getStringExtra("Type").toString();
            String target = getIntent().getStringExtra("target").toString();

            if (type.equals("Supervisor") && target.equals("show_pending") && !extra_receipt.isAcceptedBySupervisor()) {
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
        }

    }

    private void previewMerchant(Receipt receipt) {
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    if (snapshot1.getValue(Merchant.class).getId().equals(receipt.getMerchantID())) {
                        Intent i = new Intent(getApplicationContext(), MerchantOperations.class);
                        i.putExtra("details_object_merchant", snapshot1.getValue(Merchant.class));
                        i.putExtra("Type", extra_type);
                        startActivity(i);
                        GroupFunctions.openTransitionStyle(PreviewPurchases.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteReceipt(String receipt_id) {
        FirebaseDatabase.getInstance().getReference()
                .child("Receipts").child(receipt_id).removeValue();
    }

    private void selectPurchases() {
        db.getReference().child("Purchases").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                PurchaseItem purchaseItem1 = snapshot.getValue(PurchaseItem.class);
                if (purchaseItem1.getReceiptID().equals(extra_receipt.getId())) {
                    purchaseItem_data_list.add(purchaseItem1);
                    adapter.notifyDataSetChanged();

                    //total quantity
                    totalQuantity += Integer.parseInt(purchaseItem1.getQuantity());

                    //total price
                    if (!purchaseItem1.getPrice().isEmpty()) {
                        int product_price = Integer.parseInt(purchaseItem1.getPrice());
                        int productQuantity = Integer.parseInt(purchaseItem1.getQuantity());
                        int totalProductPrice = product_price * productQuantity;
                        totalPrice += totalProductPrice;
                    }
                }
                tv_totalItems.setText("" + (totalQuantity));
                tv_totalPrice.setText("" + totalPrice);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < purchaseItem_data_list.size(); i++) {
                    if (purchaseItem_data_list.get(i).getId().equals(snapshot.getValue(PurchaseItem.class).getId())) {
                        PurchaseItem purchaseItem = snapshot.getValue(PurchaseItem.class);
                        if (purchaseItem.getReceiptID().equals(extra_receipt.getId())) {
                            purchaseItem_data_list.remove(i);
                            purchaseItem_data_list.add(i, purchaseItem);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                //to re-calculate
                totalQuantity = 0;
                totalPrice = 0;
                for (int i = 0; i < purchaseItem_data_list.size(); i++) {
                    //total quantity
                    totalQuantity += Integer.parseInt(purchaseItem_data_list.get(i).getQuantity());

                    //total price
                    if (!purchaseItem_data_list.get(i).getPrice().isEmpty()) {
                        int product_price = Integer.parseInt(purchaseItem_data_list.get(i).getPrice());
                        int productQuantity = Integer.parseInt(purchaseItem_data_list.get(i).getQuantity());
                        int totalProductPrice = product_price * productQuantity;
                        totalPrice += totalProductPrice;
                    }
                }
                tv_totalItems.setText("" + (totalQuantity));
                tv_totalPrice.setText("" + totalPrice);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < purchaseItem_data_list.size(); i++) {
                    if (purchaseItem_data_list.get(i).getId().equals(snapshot.getValue(PurchaseItem.class).getId())) {
                        PurchaseItem purchaseItem = snapshot.getValue(PurchaseItem.class);
                        if (purchaseItem.getReceiptID().equals(extra_receipt.getId())) {
                            purchaseItem_data_list.remove(i);
                            adapter.notifyDataSetChanged();
                            if (purchaseItem_data_list.size() == 0) {
                                deleteReceipt(extra_receipt.getId());
                                onBackPressed();
                            }
                        }
                    }
                }
                //to re-calculate
                totalQuantity = 0;
                totalPrice = 0;
                for (int i = 0; i < purchaseItem_data_list.size(); i++) {
                    //total quantity
                    totalQuantity += Integer.parseInt(purchaseItem_data_list.get(i).getQuantity());

                    //total price
                    if (!purchaseItem_data_list.get(i).getPrice().isEmpty()) {
                        int product_price = Integer.parseInt(purchaseItem_data_list.get(i).getPrice());
                        int productQuantity = Integer.parseInt(purchaseItem_data_list.get(i).getQuantity());
                        int totalProductPrice = product_price * productQuantity;
                        totalPrice += totalProductPrice;
                    }
                }
                tv_totalItems.setText("" + (totalQuantity));
                tv_totalPrice.setText("" + totalPrice);


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
                if (purchaseItem_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (purchaseItem_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewPurchases.this,"No Purchases Yet");

                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        PurchaseItem purchaseItem1 = purchaseItem_data_list.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewPurchases.this);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Purchases").child(purchaseItem1.getId()).removeValue();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        adapter.notifyItemChanged(position);//لاسترجاع العنصر الذي تم عمل سحب له
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
                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                GroupFunctions.finishTransitionStyle(PreviewPurchases.this);
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
        if (getIntent().hasExtra("Type")) {
            String type = getIntent().getStringExtra("Type");
            if (type.equalsIgnoreCase("Supervisor")) {
                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                GroupFunctions.finishTransitionStyle(PreviewPurchases.this);
            } else if (type.equalsIgnoreCase("Merchant")) {
                startActivity(new Intent(getApplicationContext(), MerchantDashboard.class));
                GroupFunctions.finishTransitionStyle(PreviewPurchases.this);
            }
        }
    }
}