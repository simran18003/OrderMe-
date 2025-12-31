package com.simran.orderme.foodie;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.simran.orderme.R;
import com.simran.orderme.Restaurant;
import com.simran.orderme.adapters.ImageSliderAdapter;
import com.simran.orderme.loginsignup.LoginSignupActivity;

import java.util.ArrayList;
import java.util.List;

public class FoodieHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private ImageView profileImage;
    private TextView userName, userEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private ViewPager imageSlider;
    private List<Integer> imageList;
    private ImageSliderAdapter sliderAdapter;
    private RecyclerView restaurantRecyclerView;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> restaurantList;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodie_home);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_images");

        drawerLayout = findViewById(R.id.drawer_layout);
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        imageSlider = findViewById(R.id.image_slider);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        restaurantRecyclerView = findViewById(R.id.restaurant_recycler_view);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(this, restaurantList);
        restaurantRecyclerView.setAdapter(restaurantAdapter);

        fetchRestaurantData();
        setupSlidingMenu();
        loadUserInfo();
        setupImageSlider();
        setupBottomNavigation();
    }

    private void fetchRestaurantData() {
        mDatabase.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                restaurantList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String imageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                    String restaurantName = dataSnapshot.child("restaurantName").getValue(String.class);

                    Restaurant restaurant = new Restaurant(name, description, imageUrl, restaurantName);
                    restaurantList.add(restaurant);
                }
                restaurantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodieHomeActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        // Setup navigation bar buttons
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // Handle Home button click
            startActivity(new Intent(FoodieHomeActivity.this, FoodieHomeActivity.class));

        });

        findViewById(R.id.nav_food).setOnClickListener(v -> {
            startActivity(new Intent(FoodieHomeActivity.this, FoodActivity.class));
        });

        findViewById(R.id.nav_view_cart).setOnClickListener(v -> {
            startActivity(new Intent(FoodieHomeActivity.this, ViewCartActivity.class));
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(FoodieHomeActivity.this, FoodieProfileActivity.class));
        });

    }

    private void setupSlidingMenu() {
        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        findViewById(R.id.edit_profile).setOnClickListener(v -> selectImageFromGallery());

        findViewById(R.id.delete_profile).setOnClickListener(v -> deleteProfile());

        findViewById(R.id.logout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(FoodieHomeActivity.this, LoginSignupActivity.class));
            finish();
        });
    }

    private void loadUserInfo() {
        String userId = mCurrentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = mCurrentUser.getEmail();
                    userName.setText(name);
                    userEmail.setText(email);

                    String profileImageUri = snapshot.child("profileImage").getValue(String.class);
                    if (profileImageUri != null) {
                        Glide.with(FoodieHomeActivity.this).load(profileImageUri).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.user); // Default icon
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodieHomeActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        String userId = mCurrentUser.getUid();
        StorageReference fileReference = mStorageRef.child(userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUri = uri.toString();
                    mDatabase.child("users").child(userId).child("profileImage").setValue(downloadUri)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(FoodieHomeActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(FoodieHomeActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(FoodieHomeActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteProfile() {
        String userId = mCurrentUser.getUid();

        // Delete profile image from Firebase Storage
        mDatabase.child("users").child(userId).child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImageUri = snapshot.getValue(String.class);
                if (profileImageUri != null) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUri);
                    photoRef.delete().addOnSuccessListener(aVoid -> deleteUserData())
                            .addOnFailureListener(e -> Toast.makeText(FoodieHomeActivity.this, "Failed to delete profile image", Toast.LENGTH_SHORT).show());
                } else {
                    deleteUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodieHomeActivity.this, "Failed to delete profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserData() {
        String userId = mCurrentUser.getUid();
        mDatabase.child("users").child(userId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mCurrentUser.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(FoodieHomeActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(FoodieHomeActivity.this, LoginSignupActivity.class));
                        finish();
                    }
                });
            }
        });
    }
}
