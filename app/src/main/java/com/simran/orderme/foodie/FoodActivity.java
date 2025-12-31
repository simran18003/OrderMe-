package com.simran.orderme.foodie;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simran.orderme.Food;
import com.simran.orderme.R;

import java.util.ArrayList;
import java.util.List;

public class FoodActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapterFoodie foodAdapterFoodie;
    private List<Food> foodList;
    private DatabaseReference foodReference;
    private EditText searchView;
    private TextView noResultsTextView;
    private ImageButton sortButton;
    private Button goToCartButton;
    private String sortOrder = "none";
    private static final String TAG = "FoodActivity"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        goToCartButton = findViewById(R.id.btnGoToCart);
        goToCartButton.setOnClickListener(v -> {
            // Open ViewCartActivity
            Intent intent = new Intent(FoodActivity.this, ViewCartActivity.class);
            startActivity(intent);
        });

        try {
            recyclerView = findViewById(R.id.recyclerViewFoods);
            searchView = findViewById(R.id.etSearch);
            noResultsTextView = findViewById(R.id.noResultsTextView);
            sortButton = findViewById(R.id.btnSort);


            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            foodList = new ArrayList<>();
            foodAdapterFoodie = new FoodAdapterFoodie(this, foodList);
            recyclerView.setAdapter(foodAdapterFoodie);

            // Reference to the "foods" node in Firebase
            foodReference = FirebaseDatabase.getInstance().getReference().child("foods");

            loadFoodItems(); // Load food items from Firebase

            // Search feature
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFood(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Sort feature
            sortButton.setOnClickListener(v -> showSortOptions());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFoodItems() {
        foodReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear(); // Clear list to avoid duplicates
                // Loop through all admins (chefs) and their food items
                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot foodSnapshot : adminSnapshot.getChildren()) {
                        try {
                            Food food = foodSnapshot.getValue(Food.class);
                            if (food != null) {
                                Log.d(TAG, "Food item loaded: " + food.getName());
                                foodList.add(food);
                            } else {
                                Log.e(TAG, "Food item is null");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing food item: " + e.getMessage(), e);
                        }
                    }
                }
                sortAndDisplayFood(); // Sort and display after data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load food items: " + error.getMessage());
                Toast.makeText(FoodActivity.this, "Failed to load food items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterFood(String query) {
        List<Food> filteredList = new ArrayList<>();
        for (Food food : foodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase()) ||
                    food.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(food);
            }
        }
        foodAdapterFoodie = new FoodAdapterFoodie(FoodActivity.this, filteredList);
        recyclerView.setAdapter(foodAdapterFoodie);
        noResultsTextView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void sortAndDisplayFood() {
        switch (sortOrder) {
            case "price_low_to_high":
                foodList.sort((f1, f2) -> Double.compare(Double.parseDouble(f1.getPrice()), Double.parseDouble(f2.getPrice())));
                break;
            case "price_high_to_low":
                foodList.sort((f1, f2) -> Double.compare(Double.parseDouble(f2.getPrice()), Double.parseDouble(f1.getPrice())));
                break;
            default:
                break;
        }
        foodAdapterFoodie.notifyDataSetChanged();
        noResultsTextView.setVisibility(foodList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showSortOptions() {
        new android.app.AlertDialog.Builder(FoodActivity.this)
                .setTitle("Sort by")
                .setItems(new CharSequence[]{"Price: Low to High", "Price: High to Low"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortOrder = "price_low_to_high";
                            break;
                        case 1:
                            sortOrder = "price_high_to_low";
                            break;
                        default:
                            sortOrder = "none";
                            break;
                    }
                    sortAndDisplayFood();
                })
                .show();
    }
}
