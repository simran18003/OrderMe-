package com.simran.orderme.chef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.simran.orderme.Food;
import com.simran.orderme.R;
import com.simran.orderme.adapters.FoodAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewFoodActivity extends AppCompatActivity implements FoodAdapter.OnImageSelectListener {

    private static final int IMAGE_PICK_CODE = 100;
    private RecyclerView recyclerViewFoods;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;
    private DatabaseReference databaseReference;
    private String selectedFoodId;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_food);

        // Initialize RecyclerView and Adapter
        recyclerViewFoods = findViewById(R.id.recyclerViewFoods);
        recyclerViewFoods.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList, this);
        recyclerViewFoods.setAdapter(foodAdapter);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up the Database reference to fetch user's food items
        String currentUserId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("foods").child(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference("food_images");

        // Fetch data from the user's food node
        fetchFoodData();

    }

    private void fetchFoodData() {
        // Fetch food data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    Food food = foodSnapshot.getValue(Food.class);
                    if (food != null) {  // Ensure that food is not null
                        foodList.add(food);
                    }
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFoodActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onImageSelect(String foodId) {
        // Set selected food ID and open image picker
        this.selectedFoodId = foodId;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (imageUri != null && selectedFoodId != null) {
                uploadImageToFirebase(imageUri, selectedFoodId);
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String foodId) {
        // Ensure the foodId is not null
        if (foodId == null) {
            Toast.makeText(ViewFoodActivity.this, "Invalid food ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String currentUserId = currentUser.getUid();

        // Reference to the correct location in Firebase Storage
        StorageReference fileReference = storageReference.child(currentUserId).child(foodId + ".jpg");

        // Upload the image file to Firebase Storage
        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Get the download URL of the uploaded image
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                DatabaseReference foodRef = databaseReference.child(foodId);

                Map<String, Object> updates = new HashMap<>();
                updates.put("image", uri.toString());

                foodRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFoodActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ViewFoodActivity.this, "Failed to update database: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e ->
                    Toast.makeText(ViewFoodActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }).addOnFailureListener(e ->
                Toast.makeText(ViewFoodActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
