package com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewProductAdapter;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PreviewProducts extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    GridLayoutManager layoutManager;
    ArrayList<Product> product_data_list;
    PreviewProductAdapter adapter;
    Product product;
    CountDownTimer countDownTimer, countDownTimer2;

    Spinner spinner_product_category;
    ArrayList<String>  categoryList;
    String[] categoryArray;
    ArrayAdapter CategoryData;
    HashMap<String, Category> hash_categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_products);

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        product_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewProducts);
        rv = findViewById(R.id.rv_previewProduct);
        spinner_product_category = findViewById(R.id.spinner_product_category2);
        hash_categories = new HashMap<>();

        selectCategoryNameToSpinner();

        adapter = new PreviewProductAdapter(
                product_data_list, PreviewProducts.this);
        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);


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

    private void selectProducts(String category_id) {
        product_data_list.clear();
        adapter.notifyDataSetChanged();
        db.getReference().child("Products").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                product = snapshot.getValue(Product.class);
                if (product.getCategoryID().equals(category_id)) {
                    product_data_list.add(product);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < product_data_list.size(); i++) {
                    if (product_data_list.get(i).getId().equals(snapshot.getValue(Product.class).getId())) {
                        Product product1 = snapshot.getValue(Product.class);
                        Product product2=product_data_list.remove(i);
                        if (product1.getCategoryID().equals(product2.getCategoryID())) {
                            product_data_list.add(i, product1);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < product_data_list.size(); i++) {
                    if (product_data_list.get(i).getId().equals(snapshot.getValue(Product.class).getId())) {
                        product_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (product_data_list.size() == 0) {
                    startActivity(new Intent(PreviewProducts.this, SupervsorDashboard.class));
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
                if (product_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (product_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewProducts.this, "No Products Yet");
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
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
                    hash_categories.put(snapshot1.getValue(Category.class).getName(), snapshot1.getValue(Category.class));
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

                    selectProducts(spinner_product_category.getSelectedItem().toString());

                    //listener to category spinner
                    spinner_product_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Category c = hash_categories.get("" + spinner_product_category.getSelectedItem().toString());
                            selectProducts(c.getId());
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

                    Category c = hash_categories.get("" + spinner_product_category.getSelectedItem().toString());
                    selectProducts(c.getId());

                    //listener to category spinner
                    spinner_product_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Category c = hash_categories.get("" + spinner_product_category.getSelectedItem().toString());
                            selectProducts(c.getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                } else {
                    finish();
                }
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewProducts.this);
        Product product1 = product_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Products").child(product1.getId()).removeValue();
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
                startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
                GroupFunctions.finishTransitionStyle(PreviewProducts.this);
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
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(PreviewProducts.this);
    }

}