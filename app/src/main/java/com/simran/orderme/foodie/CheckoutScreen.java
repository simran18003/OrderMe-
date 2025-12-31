package com.simran.orderme.foodie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simran.orderme.R;
import com.simran.orderme.Order;

import java.util.ArrayList;
import java.util.List;

public class CheckoutScreen extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CheckoutItemAdapter checkoutItemAdapter;
    private TextView tvTotalAmount;
    private EditText etAddress;
    private RadioGroup radioGroupPayment;
    private Button btnConfirmOrder;
    private DatabaseReference ordersReference;
    private List<CartItem> foodList; // Change to List<CartItem>
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_screen);

        recyclerView = findViewById(R.id.recyclerViewCheckoutItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etAddress = findViewById(R.id.etAddress);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        // Retrieve data from Intent
        Intent intent = getIntent();
        foodList = intent.getParcelableArrayListExtra("cartItems");
        if (foodList == null) {
            foodList = new ArrayList<>();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutItemAdapter = new CheckoutItemAdapter(this, foodList);
        recyclerView.setAdapter(checkoutItemAdapter);

        // Calculate total amount
        totalAmount = calculateTotalAmount();
        tvTotalAmount.setText("Total Amount: ₹" + String.format("%.2f", totalAmount));

        // Initialize Firebase Database
        ordersReference = FirebaseDatabase.getInstance().getReference().child("orders");

        btnConfirmOrder.setOnClickListener(v -> {
            try {
                int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();
                if (selectedPaymentId == -1) {
                    Toast.makeText(CheckoutScreen.this, "Please select a payment option", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton selectedPaymentButton = findViewById(selectedPaymentId);
                String paymentOption = selectedPaymentButton.getText().toString();

                if (paymentOption.equals("Cash on Delivery")) {
                    String address = etAddress.getText().toString().trim();
                    if (address.isEmpty()) {
                        Toast.makeText(CheckoutScreen.this, "Please enter your address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    placeOrder(address, "COD");
                } else {
                    Toast.makeText(CheckoutScreen.this, "Invalid payment option selected", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CheckoutScreen.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        for (CartItem cartItem : foodList) {
            total += Double.parseDouble(cartItem.getFoodPrice()) * cartItem.getQuantity();
        }
        return total;
    }

    private void placeOrder(String address, String paymentMethod) {
        String orderId = ordersReference.push().getKey();
        Order order = new Order(orderId, foodList, address, paymentMethod);
        ordersReference.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CheckoutScreen.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity and return to the previous screen
            } else {
                Toast.makeText(CheckoutScreen.this, "Failed to place order: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
