package com.aqsa.graduation_project_app.tests;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AutoCompleteTextViewClass extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    ArrayList<String> productsNameList;
    HashMap<String,Product> productHashMap;//<product name,product>
    String[] productsNameArray;
    ArrayAdapter AutocompletionArrayAdapter;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete_text_view);
        db = FirebaseDatabase.getInstance();
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.actv_test);

        productHashMap=new HashMap<String,Product>();
        productsNameList=new ArrayList<>();
        selectProductsForAutoCompletionTextView();
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), ""+productHashMap.get(AutocompletionArrayAdapter.getItem(position)), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectProductsForAutoCompletionTextView() {

        db.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product=snapshot1.getValue(Product.class);

                    productHashMap.put(product.getName(),product);//to save the product in hashmap

                    productsNameList.add(product.getName());
                    productsNameArray = productsNameList.toArray(new String[0]);
                    AutocompletionArrayAdapter = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            productsNameArray);
                    autoCompleteTextView.setAdapter(AutocompletionArrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
















