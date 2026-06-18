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
        // Initialize Dark Mode from preferences
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sp.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Initialize Server IP from preferences
        String savedIp = sp.getString("server_ip", "");
        if (!savedIp.isEmpty()) {
            com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP = savedIp;
        }

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
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        String cachedRole = sp.getString("cached_user_role", "");

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        sp.edit().putString("cached_user_role", role.toLowerCase()).apply();
                        
                        // Sync user to MySQL
                        com.bagus.toko_baju_uas.util.UserSyncUtil.syncUser(SplashActivity.this, role);
                        
                        Intent intent;
                        if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(SplashActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, PengunjungActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        if (!cachedRole.isEmpty()) {
                            // Sync user to MySQL using cached role
                            com.bagus.toko_baju_uas.util.UserSyncUtil.syncUser(SplashActivity.this, cachedRole);
                            
                            Intent intent;
                            if ("admin".equalsIgnoreCase(cachedRole)) {
                                intent = new Intent(SplashActivity.this, AdminActivity.class);
                            } else {
                                intent = new Intent(SplashActivity.this, PengunjungActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!cachedRole.isEmpty()) {
                        // Sync user to MySQL using cached role
                        com.bagus.toko_baju_uas.util.UserSyncUtil.syncUser(SplashActivity.this, cachedRole);
                        
                        Intent intent;
                        if ("admin".equalsIgnoreCase(cachedRole)) {
                            intent = new Intent(SplashActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, PengunjungActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Gagal memvalidasi sesi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }
}