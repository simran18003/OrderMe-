package com.simran.orderme.foodie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simran.orderme.R;

import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutItemViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;

    public CheckoutItemAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CheckoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_item, parent, false);
        return new CheckoutItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutItemViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.tvFoodName.setText(cartItem.getFoodName());
        holder.tvFoodPrice.setText("₹" + cartItem.getFoodPrice());
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    class CheckoutItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvFoodName, tvFoodPrice;

        public CheckoutItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodNameCheckout);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPriceCheckout);
        }
    }
}


