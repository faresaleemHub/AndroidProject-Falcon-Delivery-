package com.aqsa.graduation_project_app.ui.supervsorSide.EntityOperations;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.supervsorSide.SupervsorDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProductsOperations extends GroupFunctions {

    FirebaseDatabase db;
    FirebaseStorage storage;

    FloatingActionButton fab;
    Spinner spinner_category_products;
    EditText ed_name, ed_description, ed_dateregister, ed_sellPrice;
    Button btn_save_product, btn_chooseProductImg,btn_scanProductParCode;
    ProgressBar progressBar;
    ImageView imgProduct;
    TextInputLayout input_product_date_register;
    TextView tv_productPhoto, tv_categoryProduct,tv_productParCodeResult;
    CollapsingToolbarLayout ctl;

    ArrayList<String> categoryList;
    String[] categoryArray;
    ArrayAdapter data;
    String name, description, categoryName, imgname, sellPrice,parCode;
    Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        this.initToolBar( "", R.drawable.ic_back);

        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        product = new Product();

        spinner_category_products = findViewById(R.id.spinner_category_products);
        ed_name = findViewById(R.id.input_product_name_text);
        ed_description = findViewById(R.id.input_product_description_text);
        btn_save_product = findViewById(R.id.btn_save_product);
        progressBar = findViewById(R.id.progressBar_addProduct);
        imgProduct = findViewById(R.id.imgProduct);
        btn_chooseProductImg = findViewById(R.id.btn_chooseProductImg);
        input_product_date_register = findViewById(R.id.input_product_date_register);
        fab = findViewById(R.id.fab7);
        tv_productPhoto = findViewById(R.id.tv_productPhoto);
        ctl = findViewById(R.id.collabsing_toolbar_product);
        ed_dateregister = findViewById(R.id.input_product_date_register_text);
        tv_categoryProduct = findViewById(R.id.tv_categoryProduct);
        ed_sellPrice = findViewById(R.id.input_sell_price_text);
        btn_scanProductParCode=findViewById(R.id.btn_scanProductParCode);
        tv_productParCodeResult=findViewById(R.id.tv_productParCodeResult);

        btn_scanProductParCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        btn_chooseProductImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        selectCategoriesToSpinner();

        btn_save_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = ed_name.getText().toString();
                description = ed_description.getText().toString();
                categoryName = spinner_category_products.getSelectedItem().toString();
                sellPrice = ed_sellPrice.getText().toString();
                TestValues("Add");
            }
        });

        if (getIntent() != null)
            if (getIntent().hasExtra("details_object_product")) {
                Intent i = getIntent();
                product = (Product) i.getExtras().getSerializable("details_object_product");
                fab.setImageResource(R.drawable.ic_store);

//                tv_productPhoto.setVisibility(View.GONE);
                btn_chooseProductImg.setVisibility(View.GONE);
                imgProduct.setVisibility(View.VISIBLE);
                input_product_date_register.setVisibility(View.VISIBLE);
                spinner_category_products.setVisibility(View.GONE);
                btn_scanProductParCode.setVisibility(View.GONE);

                ed_name.setEnabled(false);
                ed_description.setEnabled(false);
                ed_sellPrice.setEnabled(false);
                input_product_date_register.setEnabled(false);


                selectProductCategoryName(tv_categoryProduct, product.getCategoryID());
                ed_name.setText(product.getName());
                ed_description.setText(product.getDescription());
                ed_sellPrice.setText(product.getPrice());
                ed_dateregister.setText(product.getDateRegister());
                if (product.getParCode()!=null)
                    tv_productParCodeResult.setText(product.getParCode());

                ctl.setTitle("Product Details");
                btn_save_product.setText("Done");

                if (product.getImg() != null) {
                    File localFile = new File(getFilesDir(), product.getImg());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance()
                                .getReference("CategoryImages")
                                .child((product.getImg())).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imgProduct.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        imgProduct.setImageURI(Uri.fromFile(localFile));
                    }
                }

                btn_save_product.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        GroupFunctions.openTransitionStyle(ProductsOperations.this);
                    }
                });
            }

        if (getIntent() != null)
            if (getIntent().hasExtra("update_object_product")) {
                Intent i = getIntent();
                product = (Product) i.getExtras().getSerializable("update_object_product");
                fab.setImageResource(R.drawable.ic_add_products);

                imgProduct.setVisibility(View.VISIBLE);
                input_product_date_register.setVisibility(View.GONE);

                ed_name.setText(product.getName());
                ed_description.setText(product.getDescription());
                ed_sellPrice.setText(product.getPrice());
                ed_dateregister.setText(product.getDateRegister());
                if (product.getParCode()!=null)
                    tv_productParCodeResult.setText(product.getParCode());
                imgname = product.getImg();

                ctl.setTitle("Update Product");
                btn_save_product.setText("Update");

                if (product.getImg() != null) {
                    File localFile = new File(getFilesDir(), product.getImg());
                    //here we created a directory
                    if (!localFile.exists()) {
                        FileDownloadTask task = FirebaseStorage.getInstance()
                                .getReference("CategoryImages")
                                .child((product.getImg())).getFile(localFile);
                        //here will loaded the image from firebase into the file Directory on the device
                        task.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imgProduct.setImageURI(Uri.fromFile(localFile));
                                }
                            }
                        });
                    } else {
                        imgProduct.setImageURI(Uri.fromFile(localFile));
                    }
                }

                btn_save_product.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name = ed_name.getText().toString();
                        description = ed_description.getText().toString();
                        sellPrice = ed_sellPrice.getText().toString();
                        categoryName = spinner_category_products.getSelectedItem().toString();
                        parCode=tv_productParCodeResult.getText().toString();
                        TestValues("Update");
                    }
                });
            }
    }

    private void TestValues(String StateAction) {
        progressBar.setVisibility(View.VISIBLE);
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "fill the empty name space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(getApplicationContext(), "fill the empty description space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (sellPrice.isEmpty()) {
            Toast.makeText(getApplicationContext(), "fill the empty Sell Price space", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (imgname == null) {
            Toast.makeText(getApplicationContext(), "choose a photo for the Product", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (parCode == null) {
            Toast.makeText(getApplicationContext(), "identify a parCode for the Product", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (StateAction.equals("Add"))
            saveNewProduct();

        if (StateAction.equals("Update"))
            updateProduct();

    }

    private void saveNewProduct() {

        //this way to ba able to access the categoryID and save it into the product object
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Category category = snapshot1.getValue(Category.class);
                    if (category.getName().equals(categoryName)) {
                        product = new Product();
                        product.setName(name);
                        product.setCategoryID(category.getId());
                        product.setDescription(description);
                        product.setPrice(sellPrice);
                        product.setImg(imgname);
                        product.setParCode(parCode);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                        String date = sdf.format(new Date());
                        product.setDateRegister(date);

                        String product_id = db.getReference().child("Products").push().getKey();
                        product.setId(product_id);
                        db.getReference().child("Products").child(product_id).setValue(product)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                        GroupFunctions.openTransitionStyle(ProductsOperations.this);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateProduct() {
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Category category = snapshot1.getValue(Category.class);
                    if (category.getName().equals(categoryName)) {
                        product.setName(name);
                        product.setCategoryID(category.getId());
                        product.setDescription(description);
                        product.setPrice(sellPrice);
                        product.setImg(imgname);
                        product.setParCode(parCode);

                        db.getReference().child("Products").child(product.getId()).setValue(product)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                        GroupFunctions.openTransitionStyle(ProductsOperations.this);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void selectCategoriesToSpinner() {
        categoryList = new ArrayList<>();
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    categoryList.add(snapshot1.getValue(Category.class).getName());
                    categoryArray = categoryList.toArray(new String[0]);
                    data = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_spinner_item,
                            categoryArray);
                    data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_category_products.setAdapter(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
        GroupFunctions.openTransitionStyle(ProductsOperations.this);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPicture(data);
        }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(result.getContents());
                    builder.setTitle("Scanning Result");
                    builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scanCode();
                        }
                    }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            parCode=result.getContents();
                            tv_productParCodeResult.setText(result.getContents());
                        }
                    });
                    builder.setCancelable(true);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(ProductsOperations.this, "No Results", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void uploadPicture(Intent data) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image ...");
        pd.show();

        Uri fileUri = data.getData();//the path of image
        //to compress an image
        byte[]imageByte=null;
        try {
            Bitmap original= MediaStore.Images.Media.getBitmap(getContentResolver(),fileUri);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG,30,stream);
            imgProduct.setImageBitmap(original);
            imageByte=stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String randomKey = System.currentTimeMillis() + "." + getExtension(fileUri);
        UploadTask uploadTask = storage.getReference("ProductImages").child(randomKey).
                putBytes(imageByte);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    pd.dismiss();
                    imgname = randomKey;
                    imgProduct.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Failed To Upload Image", Toast.LENGTH_SHORT).show();
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

    public void selectProductCategoryName(TextView tvCategoryName, String C_ID) {
        db.getReference().child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Category.class).getId().equals(C_ID)) {
                        tvCategoryName.setText("Category : " + snapshot1.getValue(Category.class).getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void scanCode(){
        IntentIntegrator integrator= new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), SupervsorDashboard.class));
        GroupFunctions.finishTransitionStyle(ProductsOperations.this);
    }
}