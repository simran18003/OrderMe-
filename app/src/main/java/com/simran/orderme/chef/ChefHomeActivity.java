package com.simran.orderme.chef;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.simran.orderme.adapters.ImageSliderAdapter;
import com.simran.orderme.R;
import com.simran.orderme.loginsignup.LoginSignupActivity;

import java.util.ArrayList;
import java.util.List;

public class ChefHomeActivity extends AppCompatActivity {

    static final int PICK_IMAGE_REQUEST = 1;
    static final int REQUEST_CODE_PROFILE_UPDATE = 2;
    private DrawerLayout drawerLayout;
    private ImageView profileImage;
    private TextView restaurantName, restaurantEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private ViewPager imageSlider;
    private List<Integer> imageList;
    private ImageSliderAdapter sliderAdapter;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_home);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_images");

        drawerLayout = findViewById(R.id.drawer_layout);
        profileImage = findViewById(R.id.profile_image_chef);
        restaurantName = findViewById(R.id.restaurant_name2);
        restaurantEmail = findViewById(R.id.restaurant_email);
        imageSlider = findViewById(R.id.image_slider_chef);
        bottomNavigation = findViewById(R.id.bottom_navigation_chef);
        setupSlidingMenu();
        loadRestaurantInfo();
        setupImageSlider();
        setupBottomNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadProfileImage(imageUri);
        } else if (requestCode == REQUEST_CODE_PROFILE_UPDATE && resultCode == Activity.RESULT_OK) {
            // Refresh restaurant info when returning from ChefProfileActivity
            loadRestaurantInfo();
        }
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // Handle Home button click
            startActivity(new Intent(ChefHomeActivity.this, ChefHomeActivity.class));
        });

        findViewById(R.id.nav_add_food).setOnClickListener(v -> {
            startActivity(new Intent(ChefHomeActivity.this, AddFoodActivity.class));
        });

        findViewById(R.id.nav_view_orders).setOnClickListener(v -> {
            startActivity(new Intent(ChefHomeActivity.this, ViewOrdersActivity.class));
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(ChefHomeActivity.this, ChefProfileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PROFILE_UPDATE);
        });
    }

    private void setupSlidingMenu() {
        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        findViewById(R.id.edit_profile).setOnClickListener(v -> selectImageFromGallery());

        findViewById(R.id.delete_profile).setOnClickListener(v -> deleteProfile());

        findViewById(R.id.logout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ChefHomeActivity.this, LoginSignupActivity.class));
            finish();
        });
    }

    private void loadRestaurantInfo() {
        String userId = mCurrentUser.getUid();
        mDatabase.child("restaurants").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String profileImageUri = snapshot.child("profileImage").getValue(String.class);

                    // Update Restaurant Info Card
                    restaurantName.setText(name != null ? name : "Restaurant Name");
                    ((TextView) findViewById(R.id.restaurant_description)).setText(description != null ? description : "Restaurant Description");
                    if (profileImageUri != null) {
                        Glide.with(ChefHomeActivity.this).load(profileImageUri).into((ImageView) findViewById(R.id.restaurant_logo));
                    } else {
                        ((ImageView) findViewById(R.id.restaurant_logo)).setImageResource(R.drawable.restaurant); // Default logo
                    }

                    // Update Navigation Drawer Info
                    ((TextView) findViewById(R.id.restaurant_name)).setText(name != null ? name : "Restaurant Name");
                    ((TextView) findViewById(R.id.restaurant_email)).setText(mCurrentUser.getEmail() != null ? mCurrentUser.getEmail() : "email@example.com");
                    if (profileImageUri != null) {
                        Glide.with(ChefHomeActivity.this).load(profileImageUri).into((ImageView) findViewById(R.id.profile_image_chef));
                    } else {
                        ((ImageView) findViewById(R.id.profile_image_chef)).setImageResource(R.drawable.burger); // Default profile image
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChefHomeActivity.this, "Error loading restaurant info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupImageSlider() {
        imageList = new ArrayList<>();
        imageList.add(R.drawable.banner1); // Replace with your actual drawable resources
        imageList.add(R.drawable.banner2);
        imageList.add(R.drawable.banner3);

        sliderAdapter = new ImageSliderAdapter(this, imageList);
        imageSlider.setAdapter(sliderAdapter);
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadProfileImage(Uri imageUri) {
        String userId = mCurrentUser.getUid();
        StorageReference fileReference = mStorageRef.child(userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUri = uri.toString();
                    mDatabase.child("restaurants").child(userId).child("profileImage").setValue(downloadUri)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChefHomeActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChefHomeActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(ChefHomeActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteProfile() {
        String userId = mCurrentUser.getUid();

        // Delete profile image from Firebase Storage
        mDatabase.child("restaurants").child(userId).child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImageUri = snapshot.getValue(String.class);
                if (profileImageUri != null) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUri);
                    photoRef.delete().addOnSuccessListener(aVoid -> deleteUserData())
                            .addOnFailureListener(e -> Toast.makeText(ChefHomeActivity.this, "Failed to delete profile image", Toast.LENGTH_SHORT).show());
                } else {
                    deleteUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChefHomeActivity.this, "Failed to delete profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserData() {
        String userId = mCurrentUser.getUid();

        mDatabase.child("restaurants").child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(ChefHomeActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ChefHomeActivity.this, LoginSignupActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ChefHomeActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ChefHomeActivity.this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
