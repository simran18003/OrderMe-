package com.simran.orderme.loginsignup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simran.orderme.MainActivity;
import com.simran.orderme.R;
import com.simran.orderme.User;

public class LoginSignupDetailsActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private EditText editTextName, editTextRestaurantName, editTextEmail, editTextPassword;
    private Button btnSignup, btnLogin, btnGoogle, btnPhone;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup_details);

        role = getIntent().getStringExtra("role");

        editTextName = findViewById(R.id.editTextName);
        editTextRestaurantName = findViewById(R.id.editTextRestaurantName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google);
        btnPhone = findViewById(R.id.btn_phone);

        if ("Foodie".equals(role)) {
            editTextName.setVisibility(View.VISIBLE);
            editTextRestaurantName.setVisibility(View.GONE);
        } else if ("Chef".equals(role)) {
            editTextName.setVisibility(View.GONE);
            editTextRestaurantName.setVisibility(View.VISIBLE);
        }

        mAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn();

        btnSignup.setOnClickListener(v -> performSignup());
        btnLogin.setOnClickListener(v -> performLogin());
        btnGoogle.setOnClickListener(v -> googleSignIn());
        btnPhone.setOnClickListener(v -> openPhoneAuthActivity());
    }

    private void performSignup() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String restaurantName = editTextRestaurantName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Foodie".equals(role) && name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Chef".equals(role) && restaurantName.isEmpty()) {
            Toast.makeText(this, "Please enter the restaurant name.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String profileImageUrl = ""; // Default or empty string
                            User newUser = new User(uid, email, role.equals("Foodie") ? name : restaurantName, role, profileImageUrl);

                            FirebaseDatabase.getInstance().getReference("users").child(uid)
                                    .setValue(newUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Sign up successful.", Toast.LENGTH_SHORT).show();
                                            redirectToMainActivity();
                                        } else {
                                            Toast.makeText(this, "Failed to save user data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();
                        redirectToMainActivity();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Ensure the client ID is correct
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Fetch the profile image URL from the Google account
                            String profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

                            // Check if the user is signing in for the first time
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                // Save new user data to Firebase Database
                                saveUserDataToFirebase(user, role, profileImageUrl);
                            } else {
                                // User already exists, redirect to MainActivity
                                Toast.makeText(this, "Google Sign-In successful.", Toast.LENGTH_SHORT).show();
                                redirectToMainActivity();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToFirebase(FirebaseUser user, String role, String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        String name = role.equals("Foodie") ? editTextName.getText().toString().trim() : editTextRestaurantName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name or restaurant name.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SaveUserData", "UID: " + user.getUid());
        Log.d("SaveUserData", "Email: " + user.getEmail());
        Log.d("SaveUserData", "Name: " + name);
        Log.d("SaveUserData", "Role: " + role);
        Log.d("SaveUserData", "Profile Image URL: " + profileImageUrl);

        User newUser = new User(user.getUid(), user.getEmail(), name, role, profileImageUrl);

        databaseReference.child(user.getUid()).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SaveUserData", "User data saved successfully.");
                        Toast.makeText(LoginSignupDetailsActivity.this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                        redirectToMainActivity();
                    } else {
                        Log.e("SaveUserData", "Failed to store user data: " + task.getException().getMessage());
                        Toast.makeText(LoginSignupDetailsActivity.this, "Failed to store user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openPhoneAuthActivity() {
        Intent intent = new Intent(this, PhoneAuthActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }
}
