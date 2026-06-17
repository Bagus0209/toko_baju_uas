package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_cart);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            // Feature implementation coming soon
        });

        MaterialButton btnStartShopping = findViewById(R.id.btnStartShopping);
        if (btnStartShopping != null) {
            btnStartShopping.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                finish();
            });
        }
    }
}