package com.simran.orderme.chef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.simran.orderme.R;

import java.util.HashMap;
import java.util.Map;

public class AddFoodActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton imgButtonSelectImage;
    private EditText etFoodName, etFoodDescription, etFoodPrice;
    private Button btnUploadFood;
    private Uri imageUri;  // Store the selected image URI

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        // Initialize UI components
        imgButtonSelectImage = findViewById(R.id.imgButtonSelectImage);
        etFoodName = findViewById(R.id.etFoodName);
        etFoodDescription = findViewById(R.id.etFoodDescription);
        etFoodPrice = findViewById(R.id.etFoodPrice);
        btnUploadFood = findViewById(R.id.btnUploadFood);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("foods").child(currentUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("food_images");

        // Set click listener to select an image
        imgButtonSelectImage.setOnClickListener(v -> openFileChooser());

        // Set click listener to upload food data
        btnUploadFood.setOnClickListener(v -> uploadFoodData());

        Button btnViewFoods = findViewById(R.id.btnViewFoods);
        btnViewFoods.setOnClickListener(v -> {
            Intent intent = new Intent(AddFoodActivity.this, ViewFoodActivity.class);
            startActivity(intent);
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Food Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgButtonSelectImage.setImageURI(imageUri); // Display selected image
        }
    }

    private void uploadFoodData() {
        String foodName = etFoodName.getText().toString().trim();
        String foodDescription = etFoodDescription.getText().toString().trim();
        String foodPrice = etFoodPrice.getText().toString().trim();

        if (foodName.isEmpty() || foodDescription.isEmpty() || foodPrice.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String currentUserId = currentUser.getUid();

        // Create a unique ID for the food item
        String foodId = mDatabase.push().getKey();

        if (foodId == null) {
            Toast.makeText(this, "Error generating food ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize food data without the image URL
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("fid", foodId);
        foodData.put("name", foodName);
        foodData.put("description", foodDescription);
        foodData.put("price", foodPrice);
        foodData.put("userId", currentUserId);
        foodData.put("image", "");  // Initialize the image field with an empty string

        if (imageUri != null) {
            // Upload the image to Firebase Storage
            StorageReference fileReference = mStorageRef.child(currentUserId).child(foodId + ".jpg");

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Get the download URL of the uploaded image
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    foodData.put("image", imageUrl);  // Update the foodData map with the image URL

                    // Save the food data to Firebase Realtime Database under the current user
                    mDatabase.child(foodId).setValue(foodData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddFoodActivity.this, "Food uploaded successfully", Toast.LENGTH_SHORT).show();
                            finish();  // Close the activity
                        } else {
                            Toast.makeText(AddFoodActivity.this, "Failed to upload food: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }).addOnFailureListener(e -> Toast.makeText(AddFoodActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(AddFoodActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // If no image is selected, just save the data without an image
            // Save the food data to Firebase Realtime Database under the current user
            mDatabase.child(foodId).setValue(foodData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddFoodActivity.this, "Food uploaded successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity
                } else {
                    Toast.makeText(AddFoodActivity.this, "Failed to upload food: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
