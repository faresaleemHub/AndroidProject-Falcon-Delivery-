package com.aqsa.graduation_project_app.ui.clientSide;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.adapter.ClientCategoryAdapter;
import com.aqsa.graduation_project_app.adapter.ClientProductAdapter;
import com.aqsa.graduation_project_app.adapter.SliderAdapterProducts;
import com.aqsa.graduation_project_app.adapter.SliderAdapterProposedProducts;
import com.aqsa.graduation_project_app.model.Category;
import com.aqsa.graduation_project_app.model.Client;
import com.aqsa.graduation_project_app.model.Product;
import com.aqsa.graduation_project_app.ui.GroupFunctions;
import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Category;
import com.aqsa.graduation_project_app.ui.clickListener.OnRV_ClickListener_Product;
import com.aqsa.graduation_project_app.ui.subscribe.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


public class MenuFragment extends Fragment {

    ClientCategoryAdapter adapterCategory;
    ClientProductAdapter adapterProduct;
    LinearLayoutManager layoutManager;

    ArrayList<Category> category_data_list;
    ArrayList<Product> recentProduct_list;
    ArrayList<Product> proposedProduct_list;
    ArrayList<Product> product_data_list;

    FirebaseDatabase db;
    ProgressBar progressBar;
    RecyclerView rvCategories, rvProducts;
    CountDownTimer countDownTimer;
    TextView tv_client_greeting, tv_proposedProducts;

    Client client;
    FirebaseStorage storage;
    ImageView profileImg;
    PopupMenu popupMenu;

    SliderAdapterProducts sliderAdapterProducts;
    SliderAdapterProposedProducts sliderAdapterProposedProducts;

    SliderView sliderViewRecentProducts;
    SliderView sliderViewProposedProducts;

    String proposedProductCategoryID;

    //for autoCompletionTextView
    //https://bit.ly/3JcV0C9
    AutoCompleteTextView actv;
    HashMap<String,Product> productHashMap;//<product name,product>
    ArrayList<String> productsNameList_actv;
    String[] productsNameArray_actv;
    ArrayAdapter ArrayAdapter_actv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to get sharedProductCategoryID for proposedProducts
        SharedPreferences sp = this.getActivity().getSharedPreferences("StaticProposedProductCategoryID", Context.MODE_PRIVATE);
        if (sp.getString("CategoryID", null) != null) {
            proposedProductCategoryID = sp.getString("CategoryID", null);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        db = FirebaseDatabase.getInstance();

        productHashMap=new HashMap<String,Product>();

        recentProduct_list = new ArrayList<>();
        proposedProduct_list = new ArrayList<>();
        product_data_list = new ArrayList<>();
        category_data_list = new ArrayList<>();
        productsNameList_actv=new ArrayList<>();


        storage = FirebaseStorage.getInstance();
        profileImg = v.findViewById(R.id.ImgProfileClient_Dashboard);
        progressBar = v.findViewById(R.id.progressBar_clientMenue);
        tv_client_greeting = v.findViewById(R.id.tv_client_greeting);
        rvCategories = v.findViewById(R.id.rv_clientCategories);
        rvProducts = v.findViewById(R.id.rv_clientProducts);
        sliderViewRecentProducts = v.findViewById(R.id.imageSlider);//src : https://bit.ly/2ZlF8vA
        tv_proposedProducts = v.findViewById(R.id.tv_proposedProducts);
        sliderViewProposedProducts = v.findViewById(R.id.imageSlider2);
        actv=v.findViewById(R.id.ed_actv);

        progressBar.setVisibility(View.VISIBLE);

        selectSharedClientAccount();
        ClientMainActivity.toolbar.setTitle("Menu Items");
        tv_client_greeting.setText("Hi " + client.getUsername());
        uploadExistPicture();


        selectProductsForAutoCompletionTextView();
        SliderProposedProducts();
        recyclerViewProducts();
        recyclerViewCategory();
        SliderRecentProducts();



        profileImg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                popup_menu_Image(v);
            }
        });

        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //***
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ShowProductDetailsFragment fragment = new ShowProductDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", productHashMap.get(ArrayAdapter_actv.getItem(position)));
                fragment.setArguments(bundle);
                ft.replace(R.id.F_ContainerLayout, fragment);
                ft.commit();
                //***
            }
        });

        return v;
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

    private void SliderProposedProducts() {

        if (proposedProductCategoryID == null) {
            //سيتحقق هذا الشرط حال لم يطلب المستخدم طلبية بعد عبر التطبيق مطلقاً
            sliderViewProposedProducts.setVisibility(View.GONE);
            tv_proposedProducts.setVisibility(View.GONE);
        } else {

            sliderViewProposedProducts.setVisibility(View.VISIBLE);
            tv_proposedProducts.setVisibility(View.VISIBLE);

            //initializing the adapter
            sliderAdapterProposedProducts = new SliderAdapterProposedProducts(getActivity(), proposedProduct_list,
                    new OnRV_ClickListener_Product() {
                        @Override
                        public void OnItemClick(Product product) {
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            FragmentTransaction ft = fm.beginTransaction();
                            ShowProductDetailsFragment fragment = new ShowProductDetailsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("object", product);
                            fragment.setArguments(bundle);
                            ft.replace(R.id.F_ContainerLayout, fragment);
                            ft.commit();
                        }
                    });

            sliderViewProposedProducts.setSliderAdapter(sliderAdapterProposedProducts);
            sliderViewProposedProducts.setIndicatorAnimation(IndicatorAnimationType.SCALE); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            sliderViewProposedProducts.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            sliderViewProposedProducts.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
            sliderViewProposedProducts.setIndicatorSelectedColor(Color.WHITE);
            sliderViewProposedProducts.setIndicatorUnselectedColor(Color.GRAY);
            sliderViewProposedProducts.setScrollTimeInSec(4); //set scroll delay in seconds :
            sliderViewProposedProducts.startAutoCycle();

            selectProposedProducts();
        }
    }

    private void SliderRecentProducts() {
        //initializing the adapter
        sliderAdapterProducts = new SliderAdapterProducts(getActivity(), recentProduct_list,
                new OnRV_ClickListener_Product() {
                    @Override
                    public void OnItemClick(Product product) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ShowProductDetailsFragment fragment = new ShowProductDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("object", product);
                        fragment.setArguments(bundle);
                        ft.replace(R.id.F_ContainerLayout, fragment);
                        ft.commit();
                    }
                });

        sliderViewRecentProducts.setSliderAdapter(sliderAdapterProducts);
        sliderViewRecentProducts.setIndicatorAnimation(IndicatorAnimationType.SCALE); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderViewRecentProducts.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderViewRecentProducts.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderViewRecentProducts.setIndicatorSelectedColor(Color.WHITE);
        sliderViewRecentProducts.setIndicatorUnselectedColor(Color.GRAY);
        sliderViewRecentProducts.setScrollTimeInSec(4); //set scroll delay in seconds :
        sliderViewRecentProducts.startAutoCycle();

        selectMostRecentProducts();
    }

    private void recyclerViewProducts() {
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvProducts.setLayoutManager(layoutManager);
        adapterProduct = new ClientProductAdapter(product_data_list, getActivity(), new OnRV_ClickListener_Product() {
            @Override
            public void OnItemClick(Product product) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ShowProductDetailsFragment fragment = new ShowProductDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", product);
                fragment.setArguments(bundle);
                ft.replace(R.id.F_ContainerLayout, fragment);
                ft.commit();
            }
        });
        rvProducts.setAdapter(adapterProduct);
        selectProducts();
    }

    private void recyclerViewCategory() {
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvCategories.setLayoutManager(layoutManager);
        adapterCategory = new ClientCategoryAdapter(category_data_list, getActivity(), new OnRV_ClickListener_Category() {
            @Override
            public void OnItemClick(Category category) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ProductOfCategoryFragment fragment = new ProductOfCategoryFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", category);
                fragment.setArguments(bundle);
                ft.replace(R.id.F_ContainerLayout, fragment);
                ft.commit();
            }
        });
        rvCategories.setAdapter(adapterCategory);
        selectCategories();
    }

    private void selectCategories() {
        db.getInstance().getReference().child("Categories").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Category category = snapshot.getValue(Category.class);
                category_data_list.add(0,category);
                adapterCategory.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                adapterCategory.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });



        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (category_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (category_data_list.size() == 0) {
                    Toast.makeText(getActivity(),
                            "No Available Categories or there's a problem with Internet connection",
                            Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void selectProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.getInstance().getReference().child("Products").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                product_data_list.add(snapshot.getValue(Product.class));
                adapterProduct.notifyDataSetChanged();
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

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (category_data_list.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (category_data_list.size() == 0) {
                    Toast.makeText(getActivity(),
                            "No Available Categories or there's a problem with Internet connection",
                            Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void selectProposedProducts() {
        proposedProduct_list.clear();
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Products").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(Product.class).getCategoryID().equals(proposedProductCategoryID)
                && proposedProduct_list.size()<=4) {
                    //here to limit the size of proposed products in the slider
                    proposedProduct_list.add(0,snapshot.getValue(Product.class));
                    sliderAdapterProposedProducts.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    private void selectMostRecentProducts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Products").orderByPriority().limitToLast(3).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                recentProduct_list.add(0,snapshot.getValue(Product.class));
                sliderAdapterProducts.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

        Uri fileUri = data.getData();//the path of image
        final String randomKey = System.currentTimeMillis() + "." + getExtension(fileUri);
        UploadTask uploadTask = storage.getReference("images").child(randomKey).
                putFile(fileUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
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
    private String getExtension(Uri uri) {
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

    private void selectProductsForAutoCompletionTextView() {

        db.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product=snapshot1.getValue(Product.class);

                    productHashMap.put(product.getName(),product);//to save the product in hashmap

                    productsNameList_actv.add(product.getName());

                    productsNameArray_actv = productsNameList_actv.toArray(new String[0]);
                    if (productsNameArray_actv!=null) {
                        ArrayAdapter_actv = new ArrayAdapter(getActivity(),
                                android.R.layout.simple_dropdown_item_1line,
                                productsNameArray_actv);
                        actv.setAdapter(ArrayAdapter_actv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void selectSharedClientAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("LoginCredentials", getActivity().MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(LoginActivity.Shared_Login_Object, null);
        client = gson.fromJson(json, Client.class);
    }

}