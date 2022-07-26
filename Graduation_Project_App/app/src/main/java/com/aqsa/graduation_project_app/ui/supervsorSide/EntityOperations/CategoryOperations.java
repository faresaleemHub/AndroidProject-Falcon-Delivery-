package com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.PreviewEntities.PreviewReciepts;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CategoryOperations extends GroupFunctions {

    FirebaseStorage storage;
    FirebaseDatabase db;
    Category category;

    CollapsingToolbarLayout ctl;
    FloatingActionButton fab;
    EditText ed_name,ed_dateregister;
    TextView tv_categoryPhoto;
    Button btn_chooseCategoryImg, btn_save_category;
    ImageView imgCategory;
    ProgressBar progressBar;
    TextInputLayout input_category_date_register;

    String name, imgname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        storage = FirebaseStorage.getInstance();
        db = FirebaseDatabase.getInstance();
        category = new Category();

        this.initToolBar( "",R.drawable.ic_back);

        ed_name = findViewById(R.id.input_product_name_text);
        btn_chooseCategoryImg = findViewById(R.id.btn_chooseCategoryImg);
        imgCategory = findViewById(R.id.imgCategory);
        btn_save_category = findViewById(R.id.btn_save_category);
        progressBar = findViewById(R.id.progressBar_addCategory);
        fab = findViewById(R.id.fab6);
        tv_categoryPhoto = findViewById(R.id.tv_categoryPhoto);
        ctl = findViewById(R.id.collabsing_toolbar_category);
        input_category_date_register=findViewById(R.id.input_category_date_register);
        ed_dateregister=findViewById(R.id.input_category_date_register_text);

        btn_chooseCategoryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        btn_save_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = ed_name.getText().toString();
                TestValues("Add");
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_category")) {
                Intent i = getIntent();
                category = (Category) i.getExtras().getSerializable("details_object_category");
                fab.setImageResource(R.drawable.ic_category);
                tv_categoryPhoto.setVisibility(View.GONE);
                btn_chooseCategoryImg.setVisibility(View.GONE);
                imgCategory.setVisibility(View.VISIBLE);
                input_category_date_register.setVisibility(View.VISIBLE);

                ed_name.setText(category.getName());
                ed_dateregister.setText(category.getDateRegister());

                ctl.setTitle("Category Details");
                btn_save_category.setText("Done");

                if (category.getImg() != null) {
                    File localFile = new File(getFilesDir(), category.getImg());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance()
                                .getReference("CategoryImages")
                                .child((category.getImg())).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imgCategory.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        imgCategory.setImageURI(Uri.fromFile(localFile));
                    }
                }

                btn_save_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        GroupFunctions.finishTransitionStyle(CategoryOperations.this);
                    }
                });

                ed_name.setEnabled(false);
                input_category_date_register.setEnabled(false);

            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_category")) {
                Intent i = getIntent();
                category = (Category) i.getExtras().getSerializable("update_object_category");
                fab.setImageResource(R.drawable.ic_category);
                imgCategory.setVisibility(View.VISIBLE);

                ed_name.setText(category.getName());
                imgname=category.getImg();

                ctl.setTitle("Update Category");
                btn_save_category.setText("Update");

                if (category.getImg() != null) {
                    File localFile = new File(getFilesDir(), category.getImg());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance()
                                .getReference("CategoryImages")
                                .child((category.getImg())).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imgCategory.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        imgCategory.setImageURI(Uri.fromFile(localFile));
                    }
                }

                btn_save_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name=ed_name.getText().toString();
                        TestValues("Update");
                    }
                });

            }
    }

    private void TestValues(String StateAction) {
        progressBar.setVisibility(View.VISIBLE);

        if (name.isEmpty()) {
            Toast.makeText(CategoryOperations.this, "fill the empty name space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (imgname == null) {
            Toast.makeText(CategoryOperations.this, "choose a photo for the category", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (StateAction.equals("Add"))
            saveNewCategory();
        if (StateAction.equals("Update"))
            updateExistCategory();
    }

    private void updateExistCategory() {
        category.setName(name);
        category.setImg(imgname);

        db.getReference().child("Categories")
                .child(category.getId()).setValue(category).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
                GroupFunctions.openTransitionStyle(CategoryOperations.this);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewCategory() {
        Category category = new Category();
        category.setName(name);
        category.setImg(imgname);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String date = sdf.format(new Date());
        category.setDateRegister(date);

        String employee_Id = db.getReference().child("Categories").push().getKey();
        category.setId(employee_Id);
        db.getReference().child("Categories").child(employee_Id).setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                GroupFunctions.openTransitionStyle(CategoryOperations.this);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(CategoryOperations.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
        GroupFunctions.openTransitionStyle(CategoryOperations.this);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPicture(data);
        }
    }

    private void uploadPicture(Intent data) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image ...");
        pd.show();

        Uri fileUri = data.getData();//the path of image
        final String randomKey = System.currentTimeMillis() + "." + getExtension(fileUri);
        UploadTask uploadTask = storage.getReference("CategoryImages").child(randomKey).
                putFile(fileUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    pd.dismiss();
                    imgname = randomKey;
                    imgCategory.setImageURI(fileUri);
                    imgCategory.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(CategoryOperations.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();
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
    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(CategoryOperations.this);
    }
}