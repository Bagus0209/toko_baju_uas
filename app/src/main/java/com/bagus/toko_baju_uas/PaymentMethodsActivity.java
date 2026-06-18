package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

public class PaymentMethodsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnAddMethod = findViewById(R.id.btnAddMethod);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnAddMethod.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Toast.makeText(this, "Adding new payment methods will be available in the next update.", Toast.LENGTH_LONG).show();
        });
    }
}