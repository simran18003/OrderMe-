package com.simran.orderme.loginsignup;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.simran.orderme.R;

public class UserRoleActivity extends AppCompatActivity {

    private Button tabUser, tabAdmin;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role);

        role = getIntent().getStringExtra("role");

        tabUser = findViewById(R.id.tabUser);
        tabAdmin = findViewById(R.id.tabAdmin);

        // Set default fragment
        setFragment(new UserFragment());

        tabUser.setOnClickListener(v -> setFragment(new UserFragment()));
        tabAdmin.setOnClickListener(v -> setFragment(new AdminFragment()));
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
