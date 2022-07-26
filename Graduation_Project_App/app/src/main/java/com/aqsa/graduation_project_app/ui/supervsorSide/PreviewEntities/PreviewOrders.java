package com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.S_D_ClientPendingReceiptsAdapter;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.adminSide.AdminDashboard;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PreviewOrders extends GroupFunctions {

    FirebaseDatabase db;

    ProgressBar progressBar;
    RecyclerView rv;
    LinearLayoutManager layoutManager;
    ArrayList<ClientOrderReceipt> orders_receipt_data_list;
    S_D_ClientPendingReceiptsAdapter adapter;
    CountDownTimer countDownTimer, countDownTimer2;
    Employee auth_employee;
    CollapsingToolbarLayout ctl;
    FloatingActionButton fab;
    public static final String source = "PreviewOrders";
    boolean returned_result=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_orders);

        selectSharedSupervisorAccount();
        checkAuthenticity();

        this.initToolBar("", R.drawable.ic_back);
        db = FirebaseDatabase.getInstance();

        orders_receipt_data_list = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar_previewPendingOrdersSupervisor);
        rv = findViewById(R.id.rv_previewReceivedOrders_supervisor);
        ctl = findViewById(R.id.collabsing_toolbar_supervisor_orders);
        fab = findViewById(R.id.fab10);

        adapter = new S_D_ClientPendingReceiptsAdapter(
                PreviewOrders.this, orders_receipt_data_list, auth_employee, source);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        if (getIntent().hasExtra("LoadedOrders")) {
            ctl.setTitle("Loaded Orders");
            fab.setImageResource(R.drawable.ic_load);
            fab.setColorFilter(Color.BLUE);
        } else if (getIntent().hasExtra("TransferedOrders")) {
            ctl.setTitle("Transfered Orders");
            fab.setImageResource(R.drawable.ic_telegram);
            fab.setColorFilter(Color.YELLOW);
        } else if (getIntent().hasExtra("DeliveredOrders")) {
            ctl.setTitle("Delivered Orders");
            fab.setImageResource(R.drawable.ic_done);
            fab.setColorFilter(Color.BLUE);
        } else if (getIntent().hasExtra("ReceivedOrders")) {
            ctl.setTitle("Received Orders");
            fab.setImageResource(R.drawable.ic_telegram);
            fab.setColorFilter(Color.RED);
        }

        selectOrders();

    }

    private void selectOrders() {

        progressBar.setVisibility(View.VISIBLE);
        db.getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                sortAddOrder(cor1, orders_receipt_data_list.size());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {

                ClientOrderReceipt cor1 = snapshot.getValue(ClientOrderReceipt.class);
                boolean founded = false;
                if (auth_employee.getJobType().equals("Supervisor")) {
                    for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                        if (orders_receipt_data_list.get(i).getId().equals(cor1.getId())) {
                            founded = true;
                            orders_receipt_data_list.remove(i);
                            sortAddOrder(cor1, i);
                        }
                    }

                    if (founded == false) {
                        sortAddOrder(cor1, orders_receipt_data_list.size());
                    }
                    if (orders_receipt_data_list.size() == 0) {
                        if (returned_result) {
                            startActivity(new Intent(PreviewOrders.this, SupervsorDashboard.class));
                            GroupFunctions.finishTransitionStyle(PreviewOrders.this);
                        }
                        else {
                            finish();
                            GroupFunctions.finishTransitionStyle(PreviewOrders.this);
                        }
                    }
                }
            }

            private void sortAddOrder(ClientOrderReceipt cor1, int i) {
                //add
                if (getIntent().hasExtra("DeliveredOrders")) {
                    if (cor1.isDeliveredByDistributorToClient() && cor1.isReceivedByClient()) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                } else if (getIntent().hasExtra("LoadedOrders")) {
                    if (cor1.getResponsibleDistributorID() != null &&
                            cor1.isSupervisorConveyedTheOrderFromTheStoreToTheDistributor()
                            && !(cor1.isDeliveredByDistributorToClient() && cor1.isReceivedByClient())) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                } else if (getIntent().hasExtra("TransferedOrders")) {
                    if (cor1.getResponsibleDistributorID() != null &&
                            !cor1.isSupervisorConveyedTheOrderFromTheStoreToTheDistributor()) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                } else if (getIntent().hasExtra("ReceivedOrders")) {
                    if (cor1.getResponsibleDistributorID() == null) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                } else if (getIntent().hasExtra("Dealing_History_Client")) {
                    ctl.setTitle("Dealing History");
                    Client client = (Client) getIntent().getSerializableExtra("Dealing_History_Client");
                    if (cor1.isReceivedByClient() && cor1.isDeliveredByDistributorToClient() && cor1.getClient_ID().equalsIgnoreCase(client.getId())) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                } else if (getIntent().hasExtra("Dealing_History_Distributor")) {
                    ctl.setTitle("Dealing History");
                    Employee employee = (Employee) getIntent().getSerializableExtra("Dealing_History_Distributor");
                    if (cor1.isReceivedByClient() && cor1.isDeliveredByDistributorToClient()
                            && cor1.getResponsibleDistributorID() != null &&
                            cor1.getResponsibleDistributorID().equalsIgnoreCase(employee.getId())) {
                        orders_receipt_data_list.add(i, cor1);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (auth_employee.getJobType().equals("Supervisor")) {
                    for (int i = 0; i < orders_receipt_data_list.size(); i++) {
                        if (orders_receipt_data_list.get(i).getId().equals(snapshot.getValue(ClientOrderReceipt.class).getId())) {
                            orders_receipt_data_list.remove(i);
                            adapter.notifyDataSetChanged();
                        }

                        if (orders_receipt_data_list.size() == 0) {
                            if (getIntent() != null)
                                if (getIntent().hasExtra("choosed_distributor"))
                                    startActivity(new Intent(PreviewOrders.this, SupervsorDashboard.class));
                            finish();
                            GroupFunctions.finishTransitionStyle(PreviewOrders.this);
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
                if (orders_receipt_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (orders_receipt_data_list.size() == 0) {
                    GroupFunctions.EmptyToast(PreviewOrders.this,"No Orders Yet");
                    TimerClose();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();

    }

    public void checkAuthenticity(){
        if (FirebaseAuth.getInstance().getCurrentUser()==null) {
            finish();
        }else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            finish();
        }else if(!auth_employee.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
            finish();
        }
    }

    public void selectSharedSupervisorAccount() {
        SharedPreferences sp = getSharedPreferences("LoginCredentials", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        auth_employee = gson.fromJson(json, Employee.class);
    }

    public void TimerClose() {
        countDownTimer2 = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                finish();
                GroupFunctions.finishTransitionStyle(PreviewOrders.this);
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getIntent().hasExtra("details_object_client_receipt")){
            returned_result=true;
        }
    }

    @Override
    public void onBackPressed() {
        if (countDownTimer != null)
            countDownTimer.cancel();

        if (countDownTimer2 != null)
            countDownTimer2.cancel();

        if (getIntent().hasExtra("Dealing_History_Client") ||
                getIntent().hasExtra("Dealing_History_Distributor")) {
            startActivity(new Intent(PreviewOrders.this, AdminDashboard.class));
            GroupFunctions.finishTransitionStyle(PreviewOrders.this);
        } else {
            startActivity(new Intent(PreviewOrders.this, SupervsorDashboard.class));
            GroupFunctions.finishTransitionStyle(PreviewOrders.this);
        }
    }
}