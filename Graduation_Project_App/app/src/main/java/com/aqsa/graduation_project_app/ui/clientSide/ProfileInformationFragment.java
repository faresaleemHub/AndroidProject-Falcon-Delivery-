package com.aqsa.graduation_project_app.ui.clientSide;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProfileInformationFragment extends Fragment {

    ProgressBar progressBar;
    FirebaseDatabase db;
    FirebaseAuth myAuth;
    Client client;

    EditText ed_username, ed_phone, ed_email, ed_password, ed_marketName, ed_dateregister;
    Button btn_update_client;
    CollapsingToolbarLayout ctl;
    TextInputLayout input_date, input_email;
    String username, phone, email, password, marketName;

    int countClick = 0;

    FirebaseStorage storage;
    ImageView profileImg;
    PopupMenu popupMenu;

    CountDownTimer countDownTimer;
    boolean DuplicateNumber = false;
    String pastPhoneNumberInUpdate;

    boolean state = true;
    int j = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_profile_information, container, false);

        ClientMainActivity.toolbar.setTitle("My Profile");

        storage=FirebaseStorage.getInstance();
        profileImg = v.findViewById(R.id.ImgProfileClient_Dashboard);
        db = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        selectSharedClientAccount();
        uploadExistPicture();

        profileImg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu_Image(v);
            }
        });

        progressBar = v.findViewById(R.id.progressBar_addClient_ClientSide);
        ed_username = v.findViewById(R.id.input_client_username_text_ClientSide);
        ed_email = v.findViewById(R.id.input_client_email_text_ClientSide);
        ed_password = v.findViewById(R.id.input_client_Password_text_ClientSide);
        ed_phone = v.findViewById(R.id.input_client_phone_number_text_ClientSide);
        ed_marketName = v.findViewById(R.id.input_client_marketName_text_ClientSide);
        btn_update_client = v.findViewById(R.id.btn_update_client_ClientSide);

        ed_dateregister = v.findViewById(R.id.input_client_date_register_text_ClientSide);
        input_date = v.findViewById(R.id.input_client_date_register_ClientSide);
        input_email = v.findViewById(R.id.input_client_email_ClientSide);

        ed_username.setText(client.getUsername());
        ed_phone.setText(client.getPhone());
        ed_password.setText(client.getPassword());
        ed_marketName.setText(client.getMarketName());
        ed_email.setText(client.getEmail());
        ed_dateregister.setText(client.getDateRegister());

        ControlFeilds(false);

        btn_update_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countClick == 0) {
                    btn_update_client.setText("Save");
                    ControlFeilds(true);//enable
                    countClick++;
                    Toast.makeText(getActivity(), "update your credentials", Toast.LENGTH_SHORT).show();
                    pastPhoneNumberInUpdate=client.getPhone();
                    input_email.setVisibility(View.GONE);
                } else if (countClick > 0) {
                    username = ed_username.getText().toString();
                    email = ed_email.getText().toString();
                    phone = ed_phone.getText().toString();
                    password = ed_password.getText().toString();
                    marketName = ed_marketName.getText().toString();
                    TestCredentials();
                }
            }
        });

        return v;
    }

    private void ControlFeilds(boolean state) {
        ed_username.setEnabled(state);
        ed_phone.setEnabled(state);
        ed_password.setEnabled(state);
        ed_dateregister.setEnabled(state);
        ed_marketName.setEnabled(state);
        ed_email.setEnabled(state);
    }

    private void TestCredentials() {

        progressBar.setVisibility(View.VISIBLE);

        //conditions
        //A- username
        //1- check f the field is empty
        if (username.isEmpty()) {
            Toast.makeText(getActivity(), "fill the empty username space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2- to check if the username contains numbers or any other symbols
        String test_username = username.toLowerCase();
        boolean state = true;
        int j = 0;
        while (state && j < username.length()) {
            if ((test_username.charAt(j) < (char) 97 &&
                    test_username.charAt(j) != (char) 32)
                    || test_username.charAt(j) > (char) 122)
                state = false;
            j++;
        }
        if (state==false) {
            Toast.makeText(getActivity(),
                    "the name must contain letters only in English", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        //C-phone
        //1-
        if (phone.isEmpty()) {
            Toast.makeText(getActivity(), "fill the empty phone number space",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        String startNumber = "" + phone.charAt(0) + phone.charAt(1) + phone.charAt(2);
        if (!startNumber.equals("059") && !startNumber.equals("056")) {
            Toast.makeText(getActivity(), "the number must begin with 059 or 056", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-
        FirebaseDatabase.getInstance().getReference().child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Client client1 = snapshot1.getValue(Client.class);
                    if (snapshot1.getValue(Client.class).getPhone().equals(phone) &&
                            !snapshot1.getValue(Client.class).getPhone().equals(pastPhoneNumberInUpdate)) {
                        DuplicateNumber = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (DuplicateNumber == true) {
                    Toast.makeText(getActivity(), "this phone number has been used",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    DuplicateNumber = false;
                    countDownTimer.cancel();
                    return;
                }
            }

            @Override
            public void onFinish() {
                if (DuplicateNumber == true) {
                    Toast.makeText(getActivity(), "this phone number has been used",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    DuplicateNumber = false;
                    return;
                } else {
                    ContinueChecking();
                }
            }
        }.start();
    }

    public void ContinueChecking(){
        //password
        //1-
        if (password.isEmpty()) {
            Toast.makeText(getActivity(), "fill the empty password space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //2-
        if (password.length() < 8) {
            Toast.makeText(getActivity(),
                    "The Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //3-
        String pass = password;
        state = false;
        for (int i = 0; i < pass.length() - 1; i++)
            for (j = i + 1; j < pass.length(); j++)
                if (pass.charAt(i) == pass.charAt(j)) {
                    state = true;
                    break;
                }
        if (state == true) {
            Toast.makeText(getActivity(), "for Secret issues\n" +
                    "The password should not contain duplicate characters", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //4-
        j = 0;
        state = true;
        while (state && j < pass.length()) {
            if (pass.charAt(j) < (char) 97 && pass.charAt(j) != (char) 32 ||
                    pass.charAt(j) > (char) 121)
                state = false;
            j++;
        }
        if (state) {
            Toast.makeText(getActivity(), "for Secret issues\n" +
                    "the password must contain at least 1 symbol", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        //Update
        client.setUsername(username);
        client.setPhone(phone);
        client.setPassword(password);
        client.setMarketName(marketName);

        db.getReference().child("Clients")
                .child(client.getId()).setValue(client).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Updating Don", Toast.LENGTH_SHORT).show();
                updateSharedClientAccount();
                btn_update_client.setText("Update");
                countClick--;
                ControlFeilds(false);
                input_email.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void selectSharedClientAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("LoginCredentials", getActivity().MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        client = gson.fromJson(json, Client.class);
    }

    public void updateSharedClientAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("LoginCredentials", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(client);
        editor.putString(LoginActivity.Shared_Login_Object, json);
        editor.putString("Type", "Client");
        editor.commit();
    }

    private void uploadExistPicture() {
        if (client.getProfileImgURI() != null) {
            File localFile = new File(getActivity().getFilesDir(), client.getProfileImgURI());
            //here we created a directory
            if (!localFile.exists()) {
                FileDownloadTask task = FirebaseStorage.getInstance().getReference("images")
                        .child(client.getProfileImgURI()).getFile(localFile);
                //here will loaded the image from firebase into the file Directory on the device
                task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            profileImg.setImageURI(Uri.fromFile(localFile));
                        }
                    }
                });
            } else {
                profileImg.setImageURI(Uri.fromFile(localFile));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu_Image(View v) {
        popupMenu = new PopupMenu(getActivity(), v);
        getActivity().getMenuInflater().inflate(R.menu.popup_menu4, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(
                            menuPopupHelper
                                    .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        }catch (Exception e){}
        popupMenu.show();
        popup_menu_action_Image(client);
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
        GroupFunctions.openTransitionStyle(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            uploadPicture(data);
        }
    }

    public void uploadPicture(Intent data) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setTitle("Uploading Image ...");
        pd.show();

        Uri fileUri=data.getData();//the path of image
        final String randomKey = System.currentTimeMillis()+"."+getExtension(fileUri);
        UploadTask uploadTask=storage.getReference("images").child(randomKey).
                putFile(fileUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    pd.dismiss();
                    //set the image path from F_Storage to F_realtime
                    client.setProfileImgURI(randomKey);
                    FirebaseDatabase.getInstance().getReference().
                            child("Clients").child(client.getId()).setValue(client);
                    profileImg.setImageURI(fileUri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Failed To Upload Image", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double ProgressPercent =
                        (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage : " + (int) ProgressPercent + "%");
            }
        });
    }

    //to get the extension of the image
    private String getExtension(Uri uri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    private void popup_menu_action_Image(Client client) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.PopUpMenu_update4) {
                    choosePicture();
                } else if (id == R.id.PopUpMenu_delete4) {
                    client.setProfileImgURI(null);
                    FirebaseDatabase.getInstance().getReference().
                            child("Clients").child(client.getId()).setValue(client);
                    profileImg.setImageURI(null);
                }
                return true;
            }
        });
    }

}