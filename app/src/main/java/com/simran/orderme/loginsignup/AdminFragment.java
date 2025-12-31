package com.simran.orderme.loginsignup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simran.orderme.R;

public class AdminFragment extends Fragment {

    private Button btnLoginSignup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        btnLoginSignup = view.findViewById(R.id.btn_login_signup);

        // Passing "Chef" as role for Admin
        btnLoginSignup.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginSignupDetailsActivity.class);
            intent.putExtra("role", "Chef");
            startActivity(intent);
        });

        return view;
    }
}
