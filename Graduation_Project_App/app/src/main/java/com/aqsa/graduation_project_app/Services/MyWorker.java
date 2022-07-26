package com.aqsa.graduation_project_app.Services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.ClientOrderReceipt;
import com.aqsa.graduation_project_app.model.Employee;
import com.aqsa.graduation_project_app.model.Merchant;
import com.aqsa.graduation_project_app.model.Receipt;
import com.aqsa.graduation_project_app.ui.IntroActivity;
import com.aqsa.graduation_project_app.ui.distributorSide.DistributorOrders;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewOrders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class MyWorker extends Worker {

    @NonNull
    @Override
    public Result doWork() {
        monitorSupervisorOrders();
        monitorMerchantOrders();
        monitorDistributorOrders();
        return Result.success();
    }

    Employee employee;
    Merchant merchant;
    Client client;
    Context context;
    Notification notification;
    NotificationCompat.Action action;
    PendingIntent PIntent;

    final int supervisorNotificationIdNewOrder = 1;
    final int supervisorNotificationIdDoneOrder = 1;
    final int merchantNotificationIdNewOrder = 1;
    final int merchantNotificationIdDoneOrder = 1;
    final int distributorNotificationIdDoneOrder = 1;

//    I made the notificationsID the same to remove them others when changing the account so don't see the notification of other entity.
    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }



    private NotificationCompat.Builder buildNotification(String title, String content) {
        //here we need to build a notification and binds it with the channel that has the id : MY_CHANNEL_ID
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, IntroActivity.MY_CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.main_logo);
        builder.setSound(Settings.System.DEFAULT_RINGTONE_URI);
        builder.setShowWhen(true);
        return builder;
    }

    private void releaseNotificationForSupervisorOrders(String title, String content, int notificationID) {

        NotificationCompat.Builder builder=buildNotification(title, content);
        builder.setAutoCancel(true);
        //here we need to build the Action that will release when clicking on the notification
        Intent i = new Intent(context, PreviewOrders.class);
        i.putExtra("Type", "Supervisor");

        // to show pending orders
//        if (notificationID == supervisorNotificationIdNewOrder) {
//            i.putExtra("show_pending", "show_pending");
//            //here we need to build the Pending Intent
//            PIntent =
//                    PendingIntent.getActivity(context, 555, i, 0);
//            action = new
//                    NotificationCompat.Action(
//                    R.drawable.main_logo, "Show", PIntent);
//            builder.addAction(action);
//            builder.setAutoCancel(true);
//        }

        notification = builder.build();

        //now will create the manager for the notification for notifying the notification
        NotifyNotification(notification, notificationID);
    }

    private void releaseNotificationForDistributorOrders(String title, String content, int notificationID) {

        NotificationCompat.Builder builder=buildNotification(title,content);

        Intent i = new Intent(getApplicationContext(), DistributorOrders.class);
        i.putExtra("show_Received", "");

        //here we need to build the Pending Intent
        PIntent =
                PendingIntent.getActivity(context, 555, i, 0);
        action = new
                NotificationCompat.Action(
                R.drawable.main_logo, "Show", PIntent);
        builder.addAction(action);
        builder.setAutoCancel(true);

        notification = builder.build();

        //now will create the manager for the notification for notifying the notification
        NotifyNotification(notification, notificationID);
    }

    private void releaseNotificationForMerchantOrders(String title, String content, int notificationID) {

        NotificationCompat.Builder builder=buildNotification(title, content);

        //here we need to build the Action that will release when clicking on the notification
        builder.setAutoCancel(true);
        notification = builder.build();

        //now will create the manager for the notification for notifying the notification
        NotifyNotification(notification, notificationID);
    }

    private void NotifyNotification(Notification notification, int notificationID) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(notificationID, notification);
    }

    private void monitorSupervisorOrders() {
        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                selectSharedAccount();
                if(FirebaseAuth.getInstance().getCurrentUser() != null){
                    if (employee != null) {
                        if (employee.getJobType() != null) {
                            if (employee.getJobType().equals(context.getString(R.string.supervisorEmployee))) {
                                if (snapshot.getValue(ClientOrderReceipt.class).getResponsibleDistributorID() == null) {
                                    releaseNotificationForSupervisorOrders(
                                            "Pending Orders", "Check Received Orders", supervisorNotificationIdNewOrder);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {
                selectSharedAccount();
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (employee != null) {
                        if (employee.getJobType() != null) {
                            if (employee.getJobType().equals(context.getString(R.string.supervisorEmployee))) {

                                ClientOrderReceipt receipt = snapshot.getValue(ClientOrderReceipt.class);
                                if (receipt.isReceivedByClient() && receipt.isDeliveredByDistributorToClient())
                                    releaseNotificationForSupervisorOrders(
                                            "Done Order", "New Order has Done", supervisorNotificationIdDoneOrder);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void monitorMerchantOrders() {

        FirebaseDatabase.getInstance().getReference().child("Receipts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                selectSharedAccount();
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (merchant != null) {
                        Receipt receipt = snapshot.getValue(Receipt.class);
                        if (receipt.getMerchantID().equals(merchant.getId())) {
                            if (!receipt.isPriced() || !receipt.isSentByMerchant())
                                releaseNotificationForMerchantOrders("Pending Orders", "Check Received Orders"
                                        , merchantNotificationIdNewOrder);

                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void monitorDistributorOrders() {

        FirebaseDatabase.getInstance().getReference().child("ClientsReceiptOrders").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                selectSharedAccount();
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (employee != null) {
                        if (employee.getJobType() != null) {
                            if (employee.getJobType().equals(context.getString(R.string.distributorEmployee))) {
                                if (snapshot.getValue(ClientOrderReceipt.class).getResponsibleDistributorID() != null)
                                    if (snapshot.getValue(ClientOrderReceipt.class).getResponsibleDistributorID().equals(employee.getId()))
                                        if (!snapshot.getValue(ClientOrderReceipt.class).isDistributorReceivedTheOrderFromTheStore())
                                            releaseNotificationForDistributorOrders(
                                                    "Pending Orders", "Check Received Orders", distributorNotificationIdDoneOrder);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void selectSharedAccount() {
        SharedPreferences sp = context.getSharedPreferences("LoginCredentials", context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);

        employee = gson.fromJson(json, Employee.class);
        merchant = gson.fromJson(json, Merchant.class);
        client = gson.fromJson(json, Client.class);
    }

}
