package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ShippingAddressActivity extends AppCompatActivity {

    private TextInputEditText etAddress, etCity;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnSaveAddress = findViewById(R.id.btnSaveAddress);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnSaveAddress.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            saveAddress();
        });

        loadAddress();
    }

    private void loadAddress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            etAddress.setText(doc.getString("address"));
                            etCity.setText(doc.getString("city"));
                        }
                    });
        }
    }

    private void saveAddress() {
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (address.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("address", address);
            updates.put("city", city);

            db.collection("users").document(user.getUid()).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ShippingAddressActivity.this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ShippingAddressActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}