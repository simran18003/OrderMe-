package com.simran.orderme.loginsignup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.simran.orderme.R;

public class LoginSignupActivity extends AppCompatActivity {

    private Button btnSignup, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSignupActivity.this, UserRoleActivity.class);
            intent.putExtra("role", "signup");
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSignupActivity.this, UserRoleActivity.class);
            intent.putExtra("role", "login");
            startActivity(intent);
        });
    }
}
