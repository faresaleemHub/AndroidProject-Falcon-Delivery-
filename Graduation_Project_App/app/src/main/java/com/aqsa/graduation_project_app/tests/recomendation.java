package com.aqsa.graduation_project_app.tests;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aqsa.graduation_project_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class recomendation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendation);

        /*

        String[]arr={"tag1","tag2","tag3"};
        String key=FirebaseDatabase.getInstance().getReference().child("Prooooducts")
                .push().getKey();
        FirebaseDatabase.getInstance().getReference().child("Prooooducts")
                .child(key).setValue(new Product("1","Film1"));
        FirebaseDatabase.getInstance().getReference().child("Prooooducts")
                .child("Tags");
        for (int i=0;i<arr.length;i++) {
            String tag_key=FirebaseDatabase.getInstance().getReference().child("Prooooducts")
                    .child(key).push().getKey();
            FirebaseDatabase.getInstance().getReference().child("Prooooducts")
                    .child(key).child("Tags").child(tag_key).setValue(arr[i]);
            */

        FirebaseDatabase.getInstance().getReference().child("Prooooducts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    if (snapshot1.getValue(Product.class).getName().equals("Film1")){
                        Product p=snapshot1.getValue(Product.class);
                        Toast.makeText(getApplicationContext(), ""+p.getName()+" \nsize:"+
                                p.getTags().length,Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public class Tags{
        String id,name;
        public Tags(String id,String name){
            this.name=name;
            this.id=id;
        }
        public String getId(){
            return id;
        }
        public String getName(){
            return name;
        }
    }
    public class Product{
        String id,name;
        String[] tags;

        public Product(String id,String name){
            this.id=id;
            this.name=name;
        }
        public String getId(){
            return id;
        }
        public String getName(){
            return name;
        }
        public String[] getTags(){
            return tags;
        }
        public void setId(String id){
            this.id=id;
        }
        public void setName(String name){
            this.name=name;
        }
        public void setTags(String []tags){
            this.tags=tags;
        }
    }
    public class Score{
        String id,user_id,product_id;
        int value;
        public Score(String id,String user_id,String product_id,int value){
            this.user_id=user_id;
            this.id=id;
            this.value=value;
            this.product_id=product_id;

        }
        public String getId(){
            return id;
        }
        public String getProduct_id(){
            return product_id;
        }
        public String getUserID(){
            return user_id;
        }
        public int getValue(){
            return value;
        }
    }



}

