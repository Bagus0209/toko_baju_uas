package com.bagus.toko_baju_uas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Jika sudah login, cek role dan arahkan langsung
                checkUserRoleAndRedirect(currentUser);
            } else {
                // Jika belum login, ke halaman Login
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);
    }

    private void checkUserRoleAndRedirect(FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent;
                        if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(SplashActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, PengunjungActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // Jika data di firestore tidak ada, paksa login ulang
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memvalidasi sesi", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
    }
}