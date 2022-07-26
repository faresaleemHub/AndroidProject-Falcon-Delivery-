package com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewTemporaryPurchasesAdapter;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReceiptOperations extends GroupFunctions {


    FirebaseDatabase db;
    FloatingActionButton fab;
    Spinner spinner_merchant_company;
    Button btn_save_receipt, btn_insert_purchaseItem;
    ProgressBar progressBar;
    CollapsingToolbarLayout ctl;
    ArrayList<String> merchantList;
    String[] merchantArray;
    ArrayAdapter data;
    Receipt receipt;
    ArrayList<PurchaseItem> purchaseItemsList;

    RecyclerView rv;
    LinearLayoutManager layoutManager;
    PreviewTemporaryPurchasesAdapter adapter;
    ArrayList<PurchaseItem> purchaseItem_data_list;

    final static int BUY_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_receipt);

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();
        receipt = new Receipt();

        purchaseItemsList = new ArrayList<>();
        rv = findViewById(R.id.rv_TempPurchases);

        purchaseItem_data_list = new ArrayList<>();

        adapter = new PreviewTemporaryPurchasesAdapter(purchaseItem_data_list,
                ReceiptOperations.this);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        spinner_merchant_company = findViewById(R.id.spinner_merchant_company);
        btn_save_receipt = findViewById(R.id.btn_save_receipt);
        btn_insert_purchaseItem = findViewById(R.id.btn_insert_purchaseItem);
        progressBar = findViewById(R.id.progressBar_addPurchaseList);
        ctl = findViewById(R.id.collabsing_toolbar_newRecipt);
        fab = findViewById(R.id.fab5);

        selectMerchantsToSpinner();

        String product_id = db.getReference().child("Products").push().getKey();
        receipt.setId(product_id);

        btn_insert_purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PurchaseItemOperations.class);
                i.putExtra("Type", "Supervisor");
                startActivityForResult(i, BUY_REQ_CODE);
                GroupFunctions.openTransitionStyle(ReceiptOperations.this);
            }
        });


        btn_save_receipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (purchaseItemsList.size() > 0)
                    selectMerchantID();
                else {
                    finish();
                    GroupFunctions.finishTransitionStyle(ReceiptOperations.this);
                }
            }
        });

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

    public void selectMerchantsToSpinner() {
        merchantList = new ArrayList<>();
        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    merchantList.add(snapshot1.getValue(Merchant.class).getName());
                    merchantArray = merchantList.toArray(new String[0]);
                    data = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_spinner_item,
                            merchantArray);
                    data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_merchant_company.setAdapter(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void selectMerchantID() {
        String MerchantName = spinner_merchant_company.getSelectedItem().toString();

        db.getReference().child("Merchants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Merchant.class).getName().equals(MerchantName)) {

                        Receipt receipt = new Receipt();

                        receipt.setMerchantID(snapshot1.getValue(Product.class).getId());

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                        String date = sdf.format(new Date());
                        receipt.setDateRegister_Y(date);

                        sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        date = sdf.format(new Date());
                        receipt.setDateRegister_H(date);

                        String receipt_id = db.getReference().child("Receipts").push().getKey();
                        receipt.setId(receipt_id);

                        db.getReference().child("Receipts").child(receipt_id).setValue(receipt)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //... put the purchases into this root node
                                        insertReceiptPurchaseItems(receipt_id);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                finish();
                                GroupFunctions.openTransitionStyle(ReceiptOperations.this);
                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //to insert the purchases which into the receipt
    private void insertReceiptPurchaseItems(String receipt_id) {
        for (int i = 0; i < purchaseItemsList.size(); i++) {
            String purchase_id = db.getReference().child("Purchases").push().getKey();
            purchaseItemsList.get(i).setId(purchase_id);
            purchaseItemsList.get(i).setReceiptID(receipt_id);
            db.getReference().child("Purchases").child(purchaseItemsList.get(i).getId()).setValue(purchaseItemsList.get(i));
        }
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(ReceiptOperations.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK && requestCode == BUY_REQ_CODE && data.hasExtra("PurchaseItem")) {
            PurchaseItem purchaseItem = (PurchaseItem) data.getExtras().getSerializable("PurchaseItem");
            //if not exist
            if (!isExist(purchaseItem)) {
                purchaseItemsList.add(purchaseItem);
                purchaseItem_data_list.add(purchaseItem);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public boolean isExist(PurchaseItem purchaseItem) {
        for (int i = 0; i < purchaseItem_data_list.size(); i++) {
            if (purchaseItem_data_list.get(i).getProductID().equalsIgnoreCase(purchaseItem.getProductID())) {
                int quantity = Integer.parseInt(purchaseItem_data_list.get(i).getQuantity());
                int quantity2 = Integer.parseInt(purchaseItem.getQuantity());
                purchaseItem_data_list.get(i).setQuantity("" + (quantity + quantity2));
                purchaseItemsList.get(i).setQuantity("" + (quantity + quantity2));
                adapter.notifyItemChanged(i);
                return true;
            }
        }
        return false;
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiptOperations.this);
        PurchaseItem purchaseItem=purchaseItem_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        purchaseItem_data_list.remove(position);
                        purchaseItemsList.remove(position);
                        adapter.notifyDataSetChanged();
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
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(ReceiptOperations.this);
    }
}