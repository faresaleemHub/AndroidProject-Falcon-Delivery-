package com.aqsa.graduation_project_app.ui.clientSide;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

public class ShowProductDetailsFragment extends Fragment {

    TextView addToCardBtn;
    TextView tv_title, tv_price, tv_descriptionTxt, tv_quantityOrderItem;
    ImageView plusBtn, minusBtn, picOrder;
    Product object;
    int numberOrder = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            object=(Product)getArguments().get("object");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_show_product_details, container, false);

        ClientMainActivity.toolbar.setTitle("Product Details");

        initView(v);
        if (object!=null)
            getBundle();

        return v;
    }

    private void getBundle() {

        if (object.getImg() != null) {
            File localFile = new File(getActivity().getFilesDir(), object.getImg());
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().
                        getReference("ProductImages")
                        .child(object.getImg()).getFile(localFile);
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            picOrder.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                picOrder.setImageURI(Uri.fromFile(localFile));
            }
        }

        tv_title.setText(object.getName());
        tv_price.setText(object.getPrice());
        tv_descriptionTxt.setText(object.getDescription());
        tv_quantityOrderItem.setText(String.valueOf(numberOrder));

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOrder +=1;
                tv_quantityOrderItem.setText(String.valueOf(numberOrder));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOrder > 1) {
                    numberOrder -=1;
                }
                tv_quantityOrderItem.setText(String.valueOf(numberOrder));
            }
        });

        addToCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientOrderItem item=new ClientOrderItem();
                item.setBuyPrice(tv_price.getText().toString());
                item.setQuantity(tv_quantityOrderItem.getText().toString());
                item.setProduct_ID(object.getId());
                item.setProductCategory_ID(object.getCategoryID());

                new Helper().InsertOrderItem(item);
                Toast.makeText(getActivity(), "Added", Toast.LENGTH_SHORT).show();

                MenuFragment menuFragment = new MenuFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, menuFragment,"menu");
                ft.commit();
            }
        });
    }

    private void initView(View v) {
        addToCardBtn = v.findViewById(R.id.addToCardBtn);
        tv_title = v.findViewById(R.id.productTitle_Details);
        tv_price = v.findViewById(R.id.productPrice_Details);
        tv_descriptionTxt = v.findViewById(R.id.productDescription_Details);
        tv_quantityOrderItem = v.findViewById(R.id.tv_quantityOrderItem);
        plusBtn = v.findViewById(R.id.plusBtn);
        minusBtn = v.findViewById(R.id.minusBtn);
        picOrder = v.findViewById(R.id.orderPic);
    }
}