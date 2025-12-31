package com.simran.orderme.foodie;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.simran.orderme.R;
import com.simran.orderme.loginsignup.LoginSignupActivity;

public class FoodieProfileActivity extends AppCompatActivity {

    private TextView tvProfileTitle, tvName, tvSignInType;
    private ImageView ivProfilePicture;
    private Button btnEditProfile, btnDeleteAccount, btnLogout;

    private FirebaseAuth auth;
    private DatabaseReference userReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodie_profile);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        // Initialize UI elements
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvName = findViewById(R.id.tvName);
        tvSignInType = findViewById(R.id.tvSignInType);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);

        // Set default profile picture
        ivProfilePicture.setImageResource(R.drawable.user); // Placeholder image

        // Load user data from Firebase
        loadUserProfile();

        // Set up button listeners
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserProfile() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String signInType = snapshot.child("signInType").getValue(String.class);
                    String profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                    tvName.setText(name);
                    tvSignInType.setText(signInType);

                    if (profilePictureUrl != null) {
                        // Load profile picture from URL
                        // Example: Use Glide or Picasso library to load the image
                        // Glide.with(FoodieProfileActivity.this).load(profilePictureUrl).into(ivProfilePicture);
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.user); // Placeholder image
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodieProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Inflate custom layout for dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.Name);
        EditText etEmail = dialogView.findViewById(R.id.Email);

        // Set current values
        etName.setText(tvName.getText().toString());
        etEmail.setText(tvSignInType.getText().toString());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            updateUserProfile(newName, newEmail);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateUserProfile(String name, String email) {
        userReference.child("name").setValue(name);
        userReference.child("email").setValue(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FoodieProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodieProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAccount() {
        // Delete user data from Firebase
        userReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete the user account
                auth.getCurrentUser().delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(FoodieProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(FoodieProfileActivity.this, LoginSignupActivity.class));
                        finish();
                    } else {
                        Toast.makeText(FoodieProfileActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(FoodieProfileActivity.this, "Failed to delete account data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(FoodieProfileActivity.this, LoginSignupActivity.class));
        finish();
    }
}
