package com.simran.orderme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simran.orderme.chef.ChefHomeActivity;
import com.simran.orderme.foodie.FoodieHomeActivity;
import com.simran.orderme.loginsignup.LoginSignupActivity;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mCurrentUser != null) {
            checkUserRole(); // Check the user's role and redirect accordingly
        } else {
            // Redirect to Login Activity if user is not logged in
            startActivity(new Intent(MainActivity.this, LoginSignupActivity.class));
            finish();
        }
    }

    private void checkUserRole() {
        String userId = mCurrentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch the role value from the snapshot
                    String role = snapshot.child("role").getValue(String.class);

                    if (role != null) {
                        if ("Chef".equals(role)) {
                            // Navigate to Chef's home screen
                            Intent intent = new Intent(MainActivity.this, ChefHomeActivity.class);
                            startActivity(intent);
                        } else if ("Foodie".equals(role)) {
                            // Navigate to Foodie's home screen
                            Intent intent = new Intent(MainActivity.this, FoodieHomeActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Error: Unknown role", Toast.LENGTH_SHORT).show();
                        }
                        finish(); // Close MainActivity after redirection
                    } else {
                        Toast.makeText(MainActivity.this, "Error: Role not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error retrieving user role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
