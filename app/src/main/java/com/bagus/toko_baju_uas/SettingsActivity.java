package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        View btnEditProfile = findViewById(R.id.btnEditProfile);
        View btnSecurity = findViewById(R.id.btnSecurity);
        View btnHelp = findViewById(R.id.btnHelp);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnSecurity.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, SecurityActivity.class));
        });

        btnHelp.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, HelpCenterActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            logout();
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}