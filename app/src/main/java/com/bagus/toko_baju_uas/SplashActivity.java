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

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            
            // Logika Sesi Baru:
            // Tetap login hanya jika (Sudah Login Firebase) DAN (Aplikasi tidak di-task kill/Cold Start)
            if (currentUser != null && com.bagus.toko_baju_uas.api.ApiClient.isSessionActive) {
                checkUserRoleAndRedirect(currentUser);
            } else {
                // Jika aplikasi baru dibuka (Cold Start) atau belum login, arahkan ke Login
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
                        if (role == null || role.isEmpty()) role = "pengunjung";
                        
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
                        // Jika tidak ada di Firestore, gunakan cache atau logout
                        if (!cachedRole.isEmpty()) {
                            redirectToCachedRole(cachedRole);
                        } else {
                            // Coba buat doc baru sebagai pengunjung jika ini user baru (misal login google sukses tapi splash dipanggil lagi)
                            saveNewUserAndRedirect(user);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!cachedRole.isEmpty()) {
                        redirectToCachedRole(cachedRole);
                    } else {
                        Toast.makeText(this, "Gagal memvalidasi sesi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    private void redirectToCachedRole(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(SplashActivity.this, AdminActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, PengunjungActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void saveNewUserAndRedirect(FirebaseUser user) {
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("nama", (user.getDisplayName() != null) ? user.getDisplayName() : "User");
        userData.put("email", user.getEmail());
        userData.put("role", "pengunjung");

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).set(userData)
                .addOnCompleteListener(task -> {
                    startActivity(new Intent(SplashActivity.this, PengunjungActivity.class));
                    finish();
                });
    }
}