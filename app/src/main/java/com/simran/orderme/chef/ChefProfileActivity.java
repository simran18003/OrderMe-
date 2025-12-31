package com.simran.orderme.chef;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simran.orderme.R;
import com.simran.orderme.loginsignup.LoginSignupActivity;

public class ChefProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userName, userEmail, restaurantName, description;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_profile);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        restaurantName = findViewById(R.id.restaurant_name);
        description = findViewById(R.id.description);

        loadProfileData(); // Load existing data into EditText fields

        findViewById(R.id.edit_profile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.delete_profile).setOnClickListener(v -> confirmDeleteProfile());
        findViewById(R.id.logout).setOnClickListener(v -> logout());
    }

    private void loadProfileData() {
        String userId = mCurrentUser.getUid();
        mDatabase.child("restaurants").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String restaurantNameText = snapshot.child("restaurantName").getValue(String.class);
                    String descriptionText = snapshot.child("description").getValue(String.class);
                    String profileImageUri = snapshot.child("profileImage").getValue(String.class);

                    userName.setText(name);
                    userEmail.setText(email);
                    restaurantName.setText(restaurantNameText);
                    description.setText(descriptionText);

                    if (profileImageUri != null) {
                        Glide.with(ChefProfileActivity.this).load(profileImageUri).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.user); // Default icon
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChefProfileActivity.this, "Error loading profile info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile_chef, null);
        EditText editName = view.findViewById(R.id.edit_name);
        EditText editEmail = view.findViewById(R.id.edit_email);
        EditText editRestaurantName = view.findViewById(R.id.edit_restaurant_name);
        EditText editDescription = view.findViewById(R.id.edit_description);

        editName.setText(userName.getText().toString());
        editEmail.setText(userEmail.getText().toString());
        editRestaurantName.setText(restaurantName.getText().toString());
        editDescription.setText(description.getText().toString());

        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = editName.getText().toString();
            String email = editEmail.getText().toString();
            String restaurantNameText = editRestaurantName.getText().toString();
            String descriptionText = editDescription.getText().toString();

            updateProfile(name, email, restaurantNameText, descriptionText);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateProfile(String name, String email, String restaurantNameText, String descriptionText) {
        String userId = mCurrentUser.getUid();

        mDatabase.child("restaurants").child(userId).child("name").setValue(name);
        mDatabase.child("restaurants").child(userId).child("email").setValue(email);
        mDatabase.child("restaurants").child(userId).child("restaurantName").setValue(restaurantNameText);
        mDatabase.child("restaurants").child(userId).child("description").setValue(descriptionText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChefProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Notify the result
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(ChefProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteProfile() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteProfile() {
        String userId = mCurrentUser.getUid();

        mDatabase.child("restaurants").child(userId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mCurrentUser.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(ChefProfileActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ChefProfileActivity.this, LoginSignupActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(ChefProfileActivity.this, LoginSignupActivity.class));
        finish();
    }
}
