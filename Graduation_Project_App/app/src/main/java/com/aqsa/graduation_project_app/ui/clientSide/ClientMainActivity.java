package com.aqsa.graduation_project_app.ui.clientSide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ClientMainActivity extends GroupFunctions {

    static Toolbar toolbar; // to access it from other fragments
    boolean isPressed = false;
    String temporalPCID;//productCategoryID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__client);

        SharedPreferenceProposedProductCategoryID();
        checkAuthenticity();

        //toolbar
        toolbar = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //main fragment
        MenuFragment menuFragment = new MenuFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        toolbar.setTitle("Menu Items");
        ft.replace(R.id.F_ContainerLayout, menuFragment, "menu");
        ft.commit();

        bottomNavigation();

    }

    private void SharedPreferenceProposedProductCategoryID() {
        //هان بدي لما يدخل على حسابه يقوم بالبحث عن قمة المعرف لأكثر طلبية سابقة , ثم يثبتها ويشتغل عليها في عرض المنتجات في قائمة المنيو
        SharedPreferences temporalSP = getSharedPreferences("TemporalProposedProductCategoryID", Context.MODE_PRIVATE);
        SharedPreferences staticSP = getSharedPreferences("StaticProposedProductCategoryID"
                , Context.MODE_PRIVATE);

        if (temporalSP.getString("CategoryID", null) != null) {
            temporalPCID = temporalSP.getString("CategoryID", null);
            SharedPreferences.Editor editor = staticSP.edit();
            editor.putString("CategoryID", temporalPCID);
            editor.commit();
//            Toast.makeText(getApplicationContext(), "Destroy\nsaved static ID", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void bottomNavigation() {
        LinearLayout homeBtn = findViewById(R.id.xclient_home_btn);
        LinearLayout pendingOrders = findViewById(R.id.xclient_pending_orders_btn);
        LinearLayout doneOrders = findViewById(R.id.xclient_done_orders_btn);
        LinearLayout profileBtn = findViewById(R.id.xclient_profile_btn);
        FloatingActionButton fab_card = findViewById(R.id.xcard_btn);

        fab_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardListFragment fragment = new CardListFragment();
                //Get current fragment placed in container
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.F_ContainerLayout);
                //Prevent adding same fragment on top
                if (currentFragment.getClass() == fragment.getClass()) {
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, fragment);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment menuFragment = new MenuFragment();

                //Get current fragment placed in container
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.F_ContainerLayout);
                //Prevent adding same fragment on top
                if (currentFragment.getClass() == menuFragment.getClass()) {
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, menuFragment, "menu");
                ft.commit();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInformationFragment fragment = new ProfileInformationFragment();

                //Get current fragment placed in container
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.F_ContainerLayout);
                //Prevent adding same fragment on top
                if (currentFragment.getClass() == fragment.getClass()) {
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, fragment, "profile");
                ft.addToBackStack(null);
                ft.commit();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            }
        });

        pendingOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingOrdersFragment fragment = new PendingOrdersFragment();

                //Get current fragment placed in container
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.F_ContainerLayout);
                //Prevent adding same fragment on top
                if (currentFragment.getClass() == fragment.getClass()) {
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, fragment);
                ft.addToBackStack(null);
                ft.commit();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            }
        });

        doneOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoneOrdersFragment fragment = new DoneOrdersFragment();

                //Get current fragment placed in container
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.F_ContainerLayout);
                //Prevent adding same fragment on top
                if (currentFragment.getClass() == fragment.getClass()) {
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.F_ContainerLayout, fragment);
                ft.addToBackStack(null);
                ft.commit();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            }
        });

    }

    //this for listening for home icon on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("menu") != null &&
                getSupportFragmentManager().findFragmentByTag("menu").isVisible()) {
            if (isPressed) {
                finishAffinity();
                System.exit(0);
            } else {
                Toast.makeText(getApplicationContext(), "double back click to exit", Toast.LENGTH_SHORT).show();
                isPressed = true;
            }
        } else {
            MenuFragment menuFragment = new MenuFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            toolbar.setTitle("Menu Items");
            ft.replace(R.id.F_ContainerLayout, menuFragment, "menu");
            ft.commit();
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        }
    }

    //no used , just to see
    public void backPressed() {
        ConstraintLayout s = findViewById(R.id.Client_layout);//to get the used layout in the activity.
        Snackbar snack = Snackbar.make(s, "", Snackbar.LENGTH_SHORT);
        //LENGTH_INDEFINITE: will keep the snack_bar shown
        snack.setTextColor(Color.BLACK);
        snack.setBackgroundTint(Color.WHITE);
        snack.setAction("Click Here to Logout", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                GroupFunctions.logoutTransitionStyle(ClientMainActivity.this);
                GroupFunctions.closeWorkerServiceRequest();
                snack.dismiss();
            }
        });
        snack.show();
    }

    public void checkAuthenticity() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            finish();
        }
    }
}