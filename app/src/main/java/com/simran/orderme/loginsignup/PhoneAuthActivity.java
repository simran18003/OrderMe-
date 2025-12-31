package com.simran.orderme.loginsignup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simran.orderme.MainActivity;
import com.simran.orderme.R;
import com.simran.orderme.User;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber, editTextOTP;
    private Button btnVerifyPhone, btnVerifyOTP, btnResendOTP;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String verificationId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        role = getIntent().getStringExtra("role");

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextOTP = findViewById(R.id.editTextOTP);
        btnVerifyPhone = findViewById(R.id.btn_verify_phone);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
        btnResendOTP = findViewById(R.id.btn_resend_otp);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnVerifyPhone.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(PhoneAuthActivity.this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            verifyPhoneNumber(phoneNumber);
        });

        btnVerifyOTP.setOnClickListener(v -> {
            String code = editTextOTP.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(PhoneAuthActivity.this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            verifyOTP(code);
        });

        btnResendOTP.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                verifyPhoneNumber(phoneNumber);
            }
        });
    }

    private void verifyPhoneNumber(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)        // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Instant verification or Auto-retrieval case
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PhoneAuthActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PhoneAuth", "onVerificationFailed", e);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // Save verification ID and resending token so we can use them later
                    PhoneAuthActivity.this.verificationId = verificationId;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PhoneAuthActivity.this, "OTP sent.", Toast.LENGTH_SHORT).show();
                    btnVerifyOTP.setVisibility(View.VISIBLE);
                    btnResendOTP.setVisibility(View.VISIBLE);
                    editTextOTP.setVisibility(View.VISIBLE);
                }
            };

    private void verifyOTP(String code) {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String phoneNumber = user.getPhoneNumber();
                            String name = ""; // Default or empty string
                            String restaurantName = ""; // Default or empty string
                            String profileImageUrl = ""; // Default or empty string

                            // Create user object based on role
                            User newUser;
                            if ("Foodie".equals(role)) {
                                newUser = new User(name, phoneNumber, "Foodie", profileImageUrl);
                            } else if ("Chef".equals(role)) {
                                newUser = new User(name, phoneNumber, "Chef", profileImageUrl);
                            } else {
                                newUser = new User(name, phoneNumber, "Unknown", profileImageUrl);
                            }

                            mDatabase.child("users").child(uid).setValue(newUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(PhoneAuthActivity.this, "Phone authentication successful.", Toast.LENGTH_SHORT).show();
                                            redirectToMainActivity();
                                        } else {
                                            Toast.makeText(PhoneAuthActivity.this, "Failed to save user data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(PhoneAuthActivity.this, "Phone authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(PhoneAuthActivity.this, MainActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }
}
