package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonalInformationActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvJoinDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnEditInfo = findViewById(R.id.btnEditInfo);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnEditInfo.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            
            // Format join date
            long creationTimestamp = user.getMetadata().getCreationTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            tvJoinDate.setText(sdf.format(new Date(creationTimestamp)));

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            tvName.setText(doc.getString("nama"));
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}