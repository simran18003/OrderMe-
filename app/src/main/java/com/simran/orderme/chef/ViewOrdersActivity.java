package com.simran.orderme.chef;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simran.orderme.ChefOrder;
import com.simran.orderme.Food;
import com.simran.orderme.R;

public class ViewOrdersActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentChefId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);

        ordersContainer = findViewById(R.id.orders_container);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentChefId = mAuth.getCurrentUser().getUid();

        fetchOrders();
    }

    private void fetchOrders() {
        mDatabase.child("orders").orderByChild("foodList/0/chefId").equalTo(currentChefId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ordersContainer.removeAllViews();
                        if (snapshot.exists()) {
                            for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                ChefOrder order = orderSnapshot.getValue(ChefOrder.class);
                                if (order != null) {
                                    displayOrder(order);
                                }
                            }
                        } else {
                            Toast.makeText(ViewOrdersActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewOrdersActivity.this, "Error fetching orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayOrder(ChefOrder order) {
        mDatabase.child("users").child(order.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                if (userName == null) userName = "Unknown";

                View orderView = getLayoutInflater().inflate(R.layout.order_item, null);

                TextView orderIdTextView = orderView.findViewById(R.id.order_id);
                TextView foodItemsTextView = orderView.findViewById(R.id.food_items);
                TextView userNameTextView = orderView.findViewById(R.id.user_name);

                orderIdTextView.setText("Order ID: " + order.getOrderId());
                userNameTextView.setText("User: " + userName);

                StringBuilder foodItems = new StringBuilder();
                for (Food food : order.getFoodList()) {
                    foodItems.append(food.getName()).append(" - $").append(food.getPrice()).append("\n");
                }
                foodItemsTextView.setText(foodItems.toString());

                ordersContainer.addView(orderView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewOrdersActivity.this, "Error fetching user info", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
