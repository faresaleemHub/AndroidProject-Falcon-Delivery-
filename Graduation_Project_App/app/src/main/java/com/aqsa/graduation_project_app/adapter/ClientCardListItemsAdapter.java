package com.aqsa.graduation_project_app.adapter;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.aqsa.graduation_project_app.R;
import com.aqsa.graduation_project_app.model.ClientOrderItem;
import com.aqsa.graduation_project_app.ui.clientSide.Helper;
import com.aqsa.graduation_project_app.ui.clientSide.MenuFragment;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ClientCardListItemsAdapter extends RecyclerView.Adapter<ClientCardListItemsAdapter.ViewHolder> {
    public ArrayList<ClientOrderItem> data;
    private Activity activity;//or we can use " parent.getcontext()"
    private PopupMenu popupMenu;
    String state;

    public ClientCardListItemsAdapter(ArrayList<ClientOrderItem> data, Activity activity, String state) {
        this.data = data;
        this.activity = activity;
        this.state = state;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.viewholder_preview_card_orders_client_side,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClientOrderItem item = data.get(position);
        holder.unitPrice.setText(item.getBuyPrice());
        holder.quantity.setText(item.getQuantity());

        double totalPrice1=
                Integer.parseInt(item.getQuantity())
                        *Integer.parseInt(item.getBuyPrice());
        holder.totalPrice.setText(""+totalPrice1);
        new Helper().selectProductDetails(activity,item.getProduct_ID(),holder.ImgProduct,holder.title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (!state.equals("just preview"))
                    popup_menu(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, quantity, unitPrice, totalPrice;
        ImageView ImgProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_productTitlecarOrderItem);
            quantity = itemView.findViewById(R.id.tv_quantity_cardOrderItem);
            unitPrice = itemView.findViewById(R.id.tv_unitPrice_cardOrderItem);
            totalPrice = itemView.findViewById(R.id.tv_totoalPrice_cardOrderItem);
            ImgProduct = itemView.findViewById(R.id.picCard);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void popup_menu(View v,int position) {
        popupMenu=new PopupMenu(activity,v);
        activity.getMenuInflater().inflate(R.menu.popup_menu6,popupMenu.getMenu());
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
        }catch (Exception e){

        }
        popupMenu.show();
        popup_menu_action(position);
    }

    private void popup_menu_action(int position) {
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.PopUpMenu_delete6){
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, data.size());
                    if (data.size()==0){
                        activity.findViewById(R.id.emptyTxt).setVisibility(View.VISIBLE);
                        activity.findViewById(R.id.scrollView4).setVisibility(View.GONE);
                        YoYo.with(Techniques.Shake)
                                .duration(4000)
                                .repeat(2)
                                .playOn(activity.findViewById(R.id.emptyTxt));
                    }
                }
                return true;
            }
        });
    }
}
