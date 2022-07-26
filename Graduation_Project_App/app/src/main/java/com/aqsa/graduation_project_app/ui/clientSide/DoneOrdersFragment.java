package com.aqsa.graduation_project_app.ui.clientSide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.ClientReceiptsAdapter;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DoneOrdersFragment extends Fragment {

    TextView tv_empty;
    RecyclerView rv;
    FirebaseDatabase db;
    ArrayList<ClientOrderReceipt> dataList;
    LinearLayoutManager layoutManager;
    ClientReceiptsAdapter adapter;
    ScrollView scrollView;
    ProgressBar progressBar;
    CountDownTimer countDownTimer;
    Client client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_done_orders, container, false);

        ClientMainActivity.toolbar.setTitle("Done Orders");

        selectSharedClientAccount();
        rv = v.findViewById(R.id.rv_client_done_order_card_items);
        tv_empty = v.findViewById(R.id.tv_emptyDoneOrders);
        progressBar = v.findViewById(R.id.progressBar_ClientDoneOrders);
        scrollView = v.findViewById(R.id.scrollViewDoneOrders);
        db = FirebaseDatabase.getInstance();


        dataList = new ArrayList<>();
        adapter = new ClientReceiptsAdapter(getActivity(), dataList);
        layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        selectPendingOrders();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                showAlertDialog(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(rv);
        return v;
    }

    private void deleteReceiptItems(String Receipt_ID) {

        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrdersItems").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.getValue(ClientOrderItem.class).getReceipt_ID().equals(Receipt_ID))
                                FirebaseDatabase.getInstance().getReference().
                                        child("ClientsReceiptOrdersItems")
                                        .child(snapshot1.getValue(ClientOrderItem.class).getId()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void selectPendingOrders() {
        progressBar.setVisibility(View.VISIBLE);
        db.getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                if (cor1.isReceivedByClient()
                        && cor1.getClient_ID().equals(client.getId())) {
                    dataList.add(cor1);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (client != null) {
                    for (int i = 0; i < dataList.size(); i++) {
                        if (dataList.get(i).getId().equals(snapshot.getValue(ClientOrderReceipt.class).getId())) {
                            dataList.remove(i);
                            adapter.notifyDataSetChanged();
                        }

                        if (dataList.size() == 0) {
                            scrollView.setVisibility(View.GONE);
                            tv_empty.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        countDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (dataList.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                     countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (dataList.size() == 0) {
                    scrollView.setVisibility(View.GONE);
                    tv_empty.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Shake)
                            .duration(4000)
                            .repeat(2)
                            .playOn(tv_empty);
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ClientOrderReceipt receipt = dataList.get(position);
        builder.setIcon(R.drawable.ic_alert).
                setTitle("Are you Sure ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("ClientsReceiptOrders").child(receipt.getId()).removeValue();
                        deleteReceiptItems(receipt.getId());
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

    public void selectSharedClientAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("LoginCredentials", getActivity().MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        client = gson.fromJson(json, Client.class);
    }
}