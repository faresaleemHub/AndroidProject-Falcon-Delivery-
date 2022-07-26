package com.aqsa.graduation_project_app.ui.clientSide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.ClientCardListItemsAdapter;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogAnimation;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CardListFragment extends Fragment {

    ClientCardListItemsAdapter adapter;
    RecyclerView rv_cardItems;
    TextView totalCardItems, totalCardPrice, emptyTxt, btn_chk_out;
    EditText ed_latPoint, ed_longPoint;
    ScrollView scrollView;
    Spinner spGovernorate;
    ProgressBar progress;
    CheckBox check_client_default_location;

    ClientOrderReceipt co_receipt;
    Client client;

    String lat, longg, governate, date, co_receipt_id, co_item_id;

    FirebaseDatabase db;
    Button btn_selectLocation_client;

    public static ArrayList<ClientOrderItem> orderedItemsList = new ArrayList<>();
    Helper helper;

    static final int MAP_REQ_CODE = 1;
    boolean isChecked = false;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_card_list, container, false);

        ClientMainActivity.toolbar.setTitle("");

        db = FirebaseDatabase.getInstance();
        selectSharedClientAccount();
        helper = new Helper();

        initView(v);

        recyclerView(orderedItemsList);

        helper.selectTotalNumberItemsOrdered(totalCardItems);
        helper.selectTotalPriceItemsOrdered(totalCardPrice);

        btn_selectLocation_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), mapClientSelectLocation.class), MAP_REQ_CODE);
            }
        });

        sp = getActivity().getSharedPreferences("default_location"
                , Context.MODE_PRIVATE);
        if (sp!=null) {
            ed_latPoint.setText(sp.getString("lat", null));
            ed_longPoint.setText(sp.getString("long", null));
        }

        btn_chk_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                lat = ed_latPoint.getText().toString();
                longg = ed_longPoint.getText().toString();
                governate = spGovernorate.getSelectedItem().toString();

                if (lat.length() == 0) {
                    Toast.makeText(getActivity(), "fill the lat point", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    return;
                }
                if (longg.length() == 0) {
                    Toast.makeText(getActivity(), "fill the long point", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    return;
                }

                // create sharedPreference with most productID quantity requested
                int indexItemOfMaxQuantity = 0, maxQuantity = 0;
                for (int i = 0; i < orderedItemsList.size(); i++) {
                    if (Integer.parseInt(orderedItemsList.get(i).getQuantity()) > maxQuantity) {
                        maxQuantity = Integer.parseInt(orderedItemsList.get(i).getQuantity());
                        indexItemOfMaxQuantity = i;

                    }
                }

                sp = getActivity().getSharedPreferences("TemporalProposedProductCategoryID"
                        , Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("CategoryID", orderedItemsList.get(indexItemOfMaxQuantity).getProductCategory_ID());
                if (editor.commit())
                    Toast.makeText(getActivity(), "commit Temporal SP", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "not commit Temporal SP", Toast.LENGTH_SHORT).show();

                saveOrederRequest();

                //save request items
                for (int i = 0; i < orderedItemsList.size(); i++)
                    saveOrderRequestItems(orderedItemsList.get(i));

                //about setDefaultLocation
                if (isChecked) {
                    sp = getActivity().getSharedPreferences("default_location", Context.MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putBoolean("saved", true);
                    editor.putString("lat", ed_latPoint.getText().toString());
                    editor.putString("long", ed_longPoint.getText().toString());
                    editor.commit();
                    Toast.makeText(getActivity(), "saved as default location in SP", Toast.LENGTH_SHORT).show();
                }


                Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();

                //crete custom AlertDialog : https://bit.ly/3kqqiff
                AestheticDialog.Builder builder = new AestheticDialog.Builder(
                        getActivity(), DialogStyle.FLAT, DialogType.SUCCESS);
                builder.setTitle("Done");
                builder.setMessage("Thanks for dealing");
                builder.setAnimation(DialogAnimation.SHRINK);
                try {
                    builder.show();
                } catch (Exception e) {
                }

                //finish
                MenuFragment menuFragment = new MenuFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, menuFragment);
                ft.commit();

                progress.setVisibility(View.GONE);
                orderedItemsList.clear();
            }
        });


        check_client_default_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked = ((CheckBox) v).isChecked();
            }
        });

        if (orderedItemsList.size() == 0) {
            emptyTxt.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            YoYo.with(Techniques.Shake)
                    .duration(4000)
                    .repeat(2)
                    .playOn(emptyTxt);
        }
        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQ_CODE) {
            ed_latPoint.setText(data.getStringExtra("lat"));
            ed_longPoint.setText(data.getStringExtra("long"));
            check_client_default_location.setVisibility(View.VISIBLE);
        }
    }

    private void saveOrderRequestItems(ClientOrderItem item) {
        co_item_id = db.getReference().child("ClientsReceiptOrdersItems").push().getKey();
        item.setId(co_item_id);
        item.setReceipt_ID(co_receipt_id);
        db.getReference().child("ClientsReceiptOrdersItems").child(co_item_id).setValue(item);
    }

    private void saveOrederRequest() {
        co_receipt = new ClientOrderReceipt();
        co_receipt.setClient_ID(client.getId());
        co_receipt.setGovernorate(governate);
        co_receipt.setLat_location(lat);
        co_receipt.setLong_location(longg);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        date = sdf.format(new Date());
        co_receipt.setOrderDate_Y(date);

        sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        date = sdf.format(new Date());
        co_receipt.setOrderDate_H(date);

        co_receipt_id = db.getReference().child("ClientsReceiptOrders").push().getKey();
        co_receipt.setId(co_receipt_id);
        db.getReference().child("ClientsReceiptOrders").child(co_receipt_id).setValue(co_receipt)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                //finish();
            }
        });
    }

    private void recyclerView(ArrayList<ClientOrderItem> dataList) {
        adapter = new ClientCardListItemsAdapter(dataList, getActivity(), "");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        rv_cardItems.setLayoutManager(linearLayoutManager);
        rv_cardItems.setAdapter(adapter);
    }

    private void initView(View v) {
        rv_cardItems = v.findViewById(R.id.rv_client_order_card_items);
        totalCardItems = v.findViewById(R.id.tv_totalItems_Card);
        totalCardPrice = v.findViewById(R.id.tv_totalPrice_Card);
        emptyTxt = v.findViewById(R.id.emptyTxt);
        scrollView = v.findViewById(R.id.scrollView4);
        ed_latPoint = v.findViewById(R.id.input_client_lat_point_text);
        ed_longPoint = v.findViewById(R.id.input_client_long_point_text);
        spGovernorate = v.findViewById(R.id.spinner_client_governat_place);
        btn_chk_out = v.findViewById(R.id.btn_chk_out);
        progress = v.findViewById(R.id.progressBar_addClientReceiptOrder);
        btn_selectLocation_client = v.findViewById(R.id.btn_selectLocation_client);
        check_client_default_location = v.findViewById(R.id.check_client_default_location);
    }

    public void selectSharedClientAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("LoginCredentials", getActivity().MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        client = gson.fromJson(json, Client.class);
    }
}