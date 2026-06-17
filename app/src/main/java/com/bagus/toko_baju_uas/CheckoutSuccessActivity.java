package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

public class CheckoutSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_checkout_success);

        MaterialButton btnHome = findViewById(R.id.btnBackToHome);
        btnHome.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Intent intent = new Intent(this, PengunjungActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}