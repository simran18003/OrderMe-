package com.simran.orderme.foodie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simran.orderme.Food;
import com.simran.orderme.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoodAdapterFoodie extends RecyclerView.Adapter<FoodAdapterFoodie.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private DatabaseReference cartReference;
    private FirebaseUser currentUser;

    public FoodAdapterFoodie(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.cartReference = FirebaseDatabase.getInstance().getReference().child("cart").child(currentUser.getUid());
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item_foodie, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDescription, tvPrice, tvQuantity;
        ImageView ivFoodImage;
        Button btnPlus, btnMinus;
        LinearLayout quantityControls;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodNameFoodie);
            tvDescription = itemView.findViewById(R.id.tvFoodDescriptionFoodie);
            tvPrice = itemView.findViewById(R.id.tvFoodPriceFoodie);
            ivFoodImage = itemView.findViewById(R.id.imgFoodFoodie);
            quantityControls = itemView.findViewById(R.id.quantityControlFoodie);
            btnPlus = itemView.findViewById(R.id.btnPlusFoodie);
            btnMinus = itemView.findViewById(R.id.btnMinusFoodie);
            tvQuantity = itemView.findViewById(R.id.tvQuantityFoodie);
        }

        public void bind(Food food) {
            tvName.setText(food.getName());
            tvDescription.setText(food.getDescription());
            tvPrice.setText(food.getPrice());
            Picasso.get().load(food.getImage()).into(ivFoodImage);

            btnPlus.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(tvQuantity.getText().toString());
                currentQuantity++;
                tvQuantity.setText(String.valueOf(currentQuantity));
                updateCart(food, currentQuantity);
            });

            btnMinus.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(tvQuantity.getText().toString());
                if (currentQuantity > 0) {
                    currentQuantity--;
                    tvQuantity.setText(String.valueOf(currentQuantity));
                    updateCart(food, currentQuantity);
                }
            });
        }

        private void updateCart(Food food, int quantity) {
            if (quantity <= 0) {
                // Remove item from cart if quantity is zero or negative
                cartReference.child(food.getFid()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to remove item: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                CartItem cartItem = new CartItem(food.getName(), food.getPrice(), quantity);
                cartReference.child(food.getFid()).setValue(cartItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update cart: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
