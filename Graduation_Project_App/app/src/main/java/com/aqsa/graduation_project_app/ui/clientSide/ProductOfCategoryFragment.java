package com.aqsa.graduation_project_app.ui.clientSide;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Product;
import com.aqsa.graduation_project_app.adapter.ClientProductAdapter;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class ProductOfCategoryFragment extends Fragment {

    Product product;
    ArrayList<Product> product_data_list;
    ClientProductAdapter adapterProduct;
    ProgressBar progressBar;
    RecyclerView rv;
    TextView tvTitle,tvEmpty;
    ImageView img_category;
    GridLayoutManager layoutManager;
    CountDownTimer countDownTimer;

    Category category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category=(Category)getArguments().getSerializable("object");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_product_of_category, container, false);

        tvTitle=v.findViewById(R.id.tv_category_name_Details);
        tvEmpty=v.findViewById(R.id.emptyTxt2);
        img_category=v.findViewById(R.id.img_category);
        rv=v.findViewById(R.id.rv_clientCategoryProducts);
        progressBar=v.findViewById(R.id.progressBar_productOfCategory);

        if (category!=null) {
            tvTitle.setText(category.getName());
            recyclerViewRecentProducts();

            if (category.getImg() != null) {
                File localFile = new File(getActivity().getFilesDir(), category.getImg());
                //here we created a directory
                if (!localFile.exists()) {
                    FileDownloadTask task = FirebaseStorage.getInstance().
                            getReference("CategoryImages")
                            .child(category.getImg()).getFile(localFile);
                    //here will loaded the image from firebase into the file Directory on the device
                    task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                img_category.setImageURI(Uri.fromFile(localFile));
                            }
                        }
                    });
                } else {
                    img_category.setImageURI(Uri.fromFile(localFile));
                }
            }
        }
        return v;
    }

    private void recyclerViewRecentProducts() {
        layoutManager=new GridLayoutManager(getContext(),2);
        rv.setLayoutManager(layoutManager);
        product_data_list=new ArrayList<>();
        selectProducts(category.getId());
        adapterProduct=new ClientProductAdapter(product_data_list, getActivity(), new OnRV_ClickListener_Product() {
            @Override
            public void OnItemClick(Product product) {
                FragmentManager fm=getActivity().getSupportFragmentManager() ;
                FragmentTransaction ft=fm.beginTransaction();
                ShowProductDetailsFragment fragment=new ShowProductDetailsFragment();
                Bundle bundle=new Bundle();
                bundle.putSerializable("object",product);
                fragment.setArguments(bundle);
                ft.replace(R.id.F_ContainerLayout,fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        } );
        rv.setAdapter(adapterProduct);
    }

    private void selectProducts(String Category_ID) {
        FirebaseDatabase.getInstance().getReference().child("Products").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                product = snapshot.getValue(Product.class);
                if (category.getName().equals("All")){
                    product_data_list.add(product);
                    adapterProduct.notifyDataSetChanged();
                }else
                if (product.getCategoryID().equals(Category_ID)) {
                    product_data_list.add(product);
                    adapterProduct.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                adapterProduct.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Toast.makeText(getActivity(), "wait a moment for loading Products", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        countDownTimer=new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                if (product_data_list.size()==0){
//                    Toast.makeText(getActivity(),
//                            "No Available Products Under this Category or there's a problem with Internet connection",
//                            Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }
}