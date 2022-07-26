package com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.model.PurchaseItem;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.merchantSide.MerchantDashboard;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PurchaseItemOperations extends GroupFunctions {


    FirebaseDatabase db;
    Spinner spinner_product_type, spinner_product_category;
    Button btn_save_purchaseItem;
    ArrayList<String> productList, categoryList;
    ProgressBar progressBar;
    String[] producttArray, categoryArray;
    ArrayAdapter CategoryData, ProductData;
    EditText ed_product_quantity, ed_product_price;
    TextView tv_product_type, tv_product_category;
    String productQuantity, productType, productID, productPrice;
    PurchaseItem purchaseItem;
    FloatingActionButton fab;
    CollapsingToolbarLayout ctl;
    TextInputLayout input_product_price;
    String AccountType;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_purchase_item);

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();
        purchaseItem = new PurchaseItem();

        spinner_product_type = findViewById(R.id.spinner_product_type);
        btn_save_purchaseItem = findViewById(R.id.btn_save_purchaseItem);
        tv_product_type = findViewById(R.id.tv_product_type);
        tv_product_category = findViewById(R.id.tv_product_category);
        ed_product_quantity = findViewById(R.id.ed_product_quantity);
        progressBar = findViewById(R.id.progressBar_addPurchaseItem);
        fab = findViewById(R.id.fab9);
        ctl = findViewById(R.id.collabsing_toolbar_newPurchaseReceipt);
        input_product_price = findViewById(R.id.input_product_price);
        ed_product_price = findViewById(R.id.ed_product_price);
        spinner_product_category = findViewById(R.id.spinner_product_category);

        selectCategoryNameToSpinner();

        if (getIntent().hasExtra("Type"))
            AccountType = getIntent().getStringExtra("Type");

        btn_save_purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productQuantity = ed_product_quantity.getText().toString();
                if (spinner_product_type.getSelectedItem() != null) {
                    TestValues("Add");
                } else
                    Toast.makeText(PurchaseItemOperations.this, "choose the product", Toast.LENGTH_SHORT).show();
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_purchaseItem")) {
                Intent i = getIntent();
                purchaseItem = (PurchaseItem) i.getExtras().getSerializable("details_object_purchaseItem");
                fab.setImageResource(R.drawable.ic_receipt);

                spinner_product_type.setVisibility(View.GONE);
                spinner_product_category.setVisibility(View.GONE);
                tv_product_category.setVisibility(View.GONE);
                tv_product_type.setVisibility(View.GONE);

                ed_product_quantity.setEnabled(false);
                ed_product_quantity.setText(purchaseItem.getQuantity());

                ed_product_price.setEnabled(false);

                if (purchaseItem.getPrice() != null)
                    if (!purchaseItem.getPrice().isEmpty()) {
                        input_product_price.setVisibility(View.VISIBLE);
                        ed_product_price.setText(purchaseItem.getPrice());
                    }

                ctl.setTitle("Purchase Item Details");
                btn_save_purchaseItem.setText("Done");

                btn_save_purchaseItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        GroupFunctions.finishTransitionStyle(PurchaseItemOperations.this);
                    }
                });
            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_purchaseItem")) {
                if (getIntent().hasExtra("Type")) {
                    AccountType = getIntent().getStringExtra("Type");
                }

                Intent i = getIntent();
                purchaseItem = (PurchaseItem) i.getExtras().getSerializable("update_object_purchaseItem");
                fab.setImageResource(R.drawable.ic_add_products);

                ed_product_quantity.setText(purchaseItem.getQuantity());

                ctl.setTitle("Update Purchase Item");
                btn_save_purchaseItem.setText("Update");

                tv_product_type.setVisibility(View.GONE);
                if (AccountType != null && AccountType.equalsIgnoreCase("Merchant")) {
                    tv_product_category.setVisibility(View.GONE);
                    spinner_product_category.setVisibility(View.GONE);
                    spinner_product_type.setVisibility(View.GONE);
                    if (purchaseItem.getPrice() != null) {
                        input_product_price.setVisibility(View.VISIBLE);
                        ed_product_price.setText(purchaseItem.getPrice());
                    }
                } else if (AccountType != null && AccountType.equalsIgnoreCase("Supervisor")) {
                    input_product_price.setVisibility(View.GONE);
                }

                btn_save_purchaseItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        productQuantity = ed_product_quantity.getText().toString();
                        if (AccountType != null && AccountType.equalsIgnoreCase("Merchant")) {
                            productID = purchaseItem.getProductID();
                            TestValues("Update");
                        } else {
                            if (spinner_product_type.getSelectedItem() != null) {
                                productType = spinner_product_type.getSelectedItem().toString();
                                TestValues("Update");
                            } else {
                                Toast.makeText(PurchaseItemOperations.this, "choose the product", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

            }
    }

    private void TestValues(String ActionState) {

        if (productQuantity.isEmpty()) {
            Toast.makeText(getApplicationContext(), "fill the empty quantity space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (ActionState.equalsIgnoreCase("Update"))
            if (AccountType != null && AccountType.equalsIgnoreCase("Merchant")) {
                if (ed_product_price.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "fill the empty Price space", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
            }

        selectProductID(ActionState);
    }

    private void selectCategoryNameToSpinner() {
        progressBar.setVisibility(View.VISIBLE);
        categoryList = new ArrayList<>();
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    categoryList.add(snapshot1.getValue(Category.class).getName());
                    categoryArray = categoryList.toArray(new String[0]);
                    CategoryData = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_spinner_item,

                            categoryArray);
                    CategoryData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_product_category.setAdapter(CategoryData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer = new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (spinner_product_category.getSelectedItem() != null) {

                    selectCategoryID(spinner_product_category.getSelectedItem().toString());

                    //listener to category spinner
                    spinner_product_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectCategoryID(spinner_product_category.getSelectedItem().toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (spinner_product_category.getSelectedItem() != null) {

                    selectCategoryID(spinner_product_category.getSelectedItem().toString());

                    //listener to category spinner
                    spinner_product_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectCategoryID(spinner_product_category.getSelectedItem().toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }else{
                    finish();
                }
            }
        }.start();

    }

    private void selectCategoryID(String CategoryName) {
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                    if (snapshot1.getValue(Category.class).getName().equals(CategoryName)) {
                        selectProductsToSpinner(snapshot1.getValue(Category.class).getId());
                        return;
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectProductsToSpinner(String Category_ID) {
        productList = new ArrayList<>();
        db.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    if (product.getCategoryID().equals(Category_ID)) {
                        productList.add(snapshot1.getValue(Product.class).getName());
                        producttArray = productList.toArray(new String[0]);
                        ProductData = new ArrayAdapter(getApplicationContext(),
                                android.R.layout.simple_spinner_item,
                                producttArray);
                        ProductData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_product_type.setAdapter(ProductData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void selectProductID(String ActionState) {
        if (AccountType != null && AccountType.equalsIgnoreCase("Supervisor"))
            productType = spinner_product_type.getSelectedItem().toString();

        db.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (ActionState.equals("Add")) {
                        if (snapshot1.getValue(Product.class).getName().equals(productType)) {
                            purchaseItem.setQuantity(productQuantity);
                            purchaseItem.setPrice(ed_product_price.getText().toString());
                            purchaseItem.setProductID(snapshot1.getValue(Product.class).getId());
                            savePurchaseItem(purchaseItem);
                        }
                    } else if (ActionState.equalsIgnoreCase("Update")) {
                        if (AccountType.equalsIgnoreCase("Supervisor")) {//not merchant but supervisor
                            if (snapshot1.getValue(Product.class).getName().equals(productType)) {
                                purchaseItem.setQuantity(productQuantity);
                                purchaseItem.setPrice(ed_product_price.getText().toString());
                                purchaseItem.setProductID(snapshot1.getValue(Product.class).getId());
                                updatePurchaseItem(purchaseItem);
                                return;
                            }
                        } else {
                            purchaseItem.setQuantity(productQuantity);
                            purchaseItem.setPrice(ed_product_price.getText().toString());
                            purchaseItem.setProductID(productID);
                            updatePurchaseItem(purchaseItem);
                            return;
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void savePurchaseItem(PurchaseItem purchaseItem) {
        Intent i = new Intent();
        i.putExtra("PurchaseItem", purchaseItem);
        setResult(RESULT_OK, i);
        finish();
        GroupFunctions.finishTransitionStyle(PurchaseItemOperations.this);
    }

    private void updatePurchaseItem(PurchaseItem purchaseItem) {
        db.getReference().child("Purchases").child(purchaseItem.getId()).setValue(purchaseItem);
        if (AccountType != null) {
            if (AccountType.equalsIgnoreCase("Supervisor")) {
//                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                finish();
                GroupFunctions.finishTransitionStyle(PurchaseItemOperations.this);
            } else if (AccountType.equalsIgnoreCase("Merchant")) {
//                startActivity(new Intent(getApplicationContext(),MerchantDashboard.class));
                finish();
                GroupFunctions.finishTransitionStyle(PurchaseItemOperations.this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().hasExtra("Type")) {
            String type = getIntent().getStringExtra("Type");
            if (type.equalsIgnoreCase("Supervisor")) {
                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                GroupFunctions.openTransitionStyle(PurchaseItemOperations.this);
            } else if (type.equalsIgnoreCase("Merchant")) {
                startActivity(new Intent(getApplicationContext(), MerchantDashboard.class));
                GroupFunctions.openTransitionStyle(PurchaseItemOperations.this);
            }
        }
    }

}