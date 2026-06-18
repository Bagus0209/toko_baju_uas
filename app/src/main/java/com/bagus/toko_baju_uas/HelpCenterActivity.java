package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

public class HelpCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnContactWA = findViewById(R.id.btnContactWA);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnContactWA.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            openWhatsApp();
        });
    }

    private void openWhatsApp() {
        try {
            String phoneNumber = "628123456789"; // Example number
            String message = "Hello Luxe Support, I need help with the Admin Panel.";
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}