package com.simran.orderme.foodie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simran.orderme.R;

import java.util.ArrayList;
import java.util.List;

public class ViewCartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;  // Custom Adapter to display cart items
    private List<CartItem> cartItemList;  // Replace `CartItem` with your data model class
    private ProgressBar progressBar;
    private TextView emptyCartMessage;
    private Button checkoutButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);

        recyclerView = findViewById(R.id.cart_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyCartMessage = findViewById(R.id.empty_cart_message);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList);
        recyclerView.setAdapter(cartAdapter);
        checkoutButton = findViewById(R.id.btnCheckout);
        checkoutButton.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(ViewCartActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create an intent to start CheckoutScreen
            Intent intent = new Intent(ViewCartActivity.this, CheckoutScreen.class);

            // Pass the cart items to CheckoutScreen
            intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItemList));

            startActivity(intent);
        });



        loadCartItems();
    }

    private void loadCartItems() {
        if (mCurrentUser == null) {
            Toast.makeText(ViewCartActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mCurrentUser.getUid();
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("cart").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        CartItem cartItem = itemSnapshot.getValue(CartItem.class);  // Replace `CartItem` with your data model class
                        if (cartItem != null) {
                            cartItemList.add(cartItem);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    emptyCartMessage.setVisibility(cartItemList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    emptyCartMessage.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewCartActivity.this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
