package com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.PreviewCategoriesAdapter;
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

public class PreviewCategories extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    GridLayoutManager layoutManager;
    ArrayList<Category> category_data_list;
    PreviewCategoriesAdapter adapter;
    Category category;
    CountDownTimer countDownTimer, countDownTimer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_categories);

        this.initToolBar("", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();

        category_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewCategories);
        rv = findViewById(R.id.rv_previewCategory);


        adapter = new PreviewCategoriesAdapter(
                PreviewCategories.this, category_data_list);
        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        selectCategories();

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

    private void deleteProductsOfCategory(String categoryId) {
        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Product.class).getCategoryID().equals(categoryId)) {
                        String p_id = snapshot1.getValue(Product.class).getId();
                        FirebaseDatabase.getInstance().getReference().child("Products").child(p_id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectCategories() {
        db.getReference().child("Categories").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                category = snapshot.getValue(Category.class);
                category_data_list.add(category);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < category_data_list.size(); i++) {
                    if (category_data_list.get(i).getId().equals(snapshot.getValue(Category.class).getId())) {
                        Category category1 = snapshot.getValue(Category.class);
                        category_data_list.remove(i);
                        category_data_list.add(i, category1);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < category_data_list.size(); i++) {
                    if (category_data_list.get(i).getId().equals(snapshot.getValue(Category.class).getId())) {
                        category_data_list.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (category_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewCategories.this,"No Categories Yet");
                    TimerClose();
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
        countDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (category_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (category_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewCategories.this,"No Categories Yet");
                    TimerClose();
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
                GroupFunctions.finishTransitionStyle(PreviewCategories.this);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewCategories.this);
        Category category1=category_data_list.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("Categories").child(category1.getId()).removeValue();
                        deleteProductsOfCategory(category1.getId());
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
        if (countDownTimer != null)
            countDownTimer.cancel();
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(this);
    }
}