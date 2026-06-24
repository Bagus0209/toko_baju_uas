package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.AdminStatsResponse;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvTotalProduk, tvTotalOrders, tvTotalRevenue, tvTotalUsers;

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
        super.setContentView(R.layout.activity_admin);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        
        tvTotalProduk = findViewById(R.id.tvTotalProduk); 
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);

        MaterialButton btnViewInventory = findViewById(R.id.btnViewInventory);
        MaterialButton btnVisitShop = findViewById(R.id.btnVisitShop);

        loadAdminDashboard();

        btnMenu.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            drawerLayout.openDrawer(GravityCompat.START);
        });

        btnViewInventory.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(AdminActivity.this, InventoryActivity.class));
        });

        btnVisitShop.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(AdminActivity.this, PengunjungActivity.class));
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_dashboard) {
                loadAdminDashboard();
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, TransactionsAdminActivity.class));
            } else if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomersAdminActivity.class));
            } else if (id == R.id.nav_settings) {
                showAdminSettingsDialog();
            } else if (id == R.id.nav_add_admin) {
                showAddAdminDialog();
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadAdminDashboard() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminStats().enqueue(new Callback<AdminStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminStatsResponse> call, @NonNull Response<AdminStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    AdminStatsResponse.Data data = response.body().getData();
                    if (data != null) {
                        if (tvTotalProduk != null) tvTotalProduk.setText(String.valueOf(data.total_products));
                        if (tvTotalOrders != null) tvTotalOrders.setText(String.valueOf(data.total_orders));
                        if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(data.total_users));
                        
                        if (tvTotalRevenue != null) {
                            Locale localeID = new Locale("id", "ID");
                            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                            tvTotalRevenue.setText(formatRupiah.format(data.total_revenue));
                        }
                    }
                } else {
                    // Jika data kosong atau status false, set ke 0
                    if (tvTotalProduk != null) tvTotalProduk.setText("0");
                    if (tvTotalOrders != null) tvTotalOrders.setText("0");
                    if (tvTotalUsers != null) tvTotalUsers.setText("0");
                    if (tvTotalRevenue != null) tvTotalRevenue.setText("Rp 0");
                }
            }
            @Override
            public void onFailure(@NonNull Call<AdminStatsResponse> call, @NonNull Throwable t) {
                // Jangan tampilkan toast terus-menerus di dashboard, cukup log saja
                android.util.Log.e("AdminDashboard", "Stats error: " + t.getMessage());
            }
        });
    }

    private void logout() {
        // Sign out dari Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Sign out dari Google agar pilihan akun muncul lagi
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso).signOut();

        // Reset status sesi dan bersihkan cache
        com.bagus.toko_baju_uas.api.ApiClient.isSessionActive = false;
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        sp.edit().remove("cached_user_role").apply();

        // Kembali ke Login
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminDashboard();
    }

    private void showAdminSettingsDialog() {
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sp.getBoolean("dark_mode", false);
        boolean isNotifications = sp.getBoolean("admin_notifications", true);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_settings, null);

        TextView tvEmail = dialogView.findViewById(R.id.tvAdminEmail);
        EditText etNama = dialogView.findViewById(R.id.etAdminNama);
        EditText etIp = dialogView.findViewById(R.id.etServerIp);
        MaterialButton btnTest = dialogView.findViewById(R.id.btnTestConnection);
        TextView tvStatus = dialogView.findViewById(R.id.tvConnectionStatus);
        SwitchMaterial switchDark = dialogView.findViewById(R.id.switchAdminDarkMode);
        SwitchMaterial switchNotif = dialogView.findViewById(R.id.switchAdminNotifications);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            String localName = sp.getString("user_profile_name_" + user.getUid(), null);
            if (localName != null) etNama.setText(localName);
            
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && localName == null) {
                            etNama.setText(doc.getString("nama"));
                        }
                    });
        }

        etIp.setText(com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP);
        switchDark.setChecked(isDarkMode);
        switchNotif.setChecked(isNotifications);

        btnTest.setOnClickListener(v -> {
            String testIp = etIp.getText().toString().trim();
            if (testIp.isEmpty()) return;
            tvStatus.setText("Menghubungkan...");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.luxe_gold));

            String testUrl = "http://" + testIp + "/api_tokobaju/";
            retrofit2.Retrofit testRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(testUrl)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            ApiInterface testApi = testRetrofit.create(ApiInterface.class);
            testApi.getBarang().enqueue(new Callback<BarangResponse>() {
                @Override
                public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                    if (response.isSuccessful()) {
                        tvStatus.setText("Status: Sukses");
                        tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_completed));
                    } else {
                        tvStatus.setText("Status: Gagal (" + response.code() + ")");
                        tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_cancelled));
                    }
                }
                @Override
                public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                    tvStatus.setText("Status: Error");
                    tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_cancelled));
                }
            });
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    boolean darkChecked = switchDark.isChecked();
                    String newIp = etIp.getText().toString().trim();

                    android.content.SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("dark_mode", darkChecked);
                    editor.putBoolean("admin_notifications", switchNotif.isChecked());

                    if (!newIp.isEmpty()) {
                        com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP = newIp;
                        editor.putString("server_ip", newIp);
                    }
                    editor.apply();

                    if (darkChecked) {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    }

                    String newName = etNama.getText().toString().trim();
                    if (user != null && !newName.isEmpty()) {
                        editor.putString("user_profile_name_" + user.getUid(), newName).apply();
                        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).update("nama", newName);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAddAdminDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin, null);
        EditText etNewAdminNama = dialogView.findViewById(R.id.etNewAdminNama);
        EditText etNewAdminEmail = dialogView.findViewById(R.id.etNewAdminEmail);
        EditText etNewAdminPassword = dialogView.findViewById(R.id.etNewAdminPassword);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Tambah", null)
                .setNegativeButton("Batal", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nama = etNewAdminNama.getText().toString().trim();
                String email = etNewAdminEmail.getText().toString().trim();
                String password = etNewAdminPassword.getText().toString().trim();

                if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || password.length() < 6) {
                    Toast.makeText(AdminActivity.this, "Data tidak valid atau password < 6 karakter", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                checkAdminLimitAndRegister(nama, email, password);
            });
        });
        dialog.show();
    }

    private void checkAdminLimitAndRegister(String name, String email, String password) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.checkAdminCount().enqueue(new Callback<com.bagus.toko_baju_uas.model.BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    registerNewAdmin(name, email, password);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Limit tercapai";
                    Toast.makeText(AdminActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Throwable t) {}
        });
    }

    private void registerNewAdmin(String name, String email, String password) {
        try {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            String tempAppName = "TempAdminReg_" + System.currentTimeMillis();
            FirebaseApp tempApp = FirebaseApp.initializeApp(this, options, tempAppName);
            FirebaseAuth tempAuth = FirebaseAuth.getInstance(tempApp);

            tempAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().getUser() != null) {
                            String uid = task.getResult().getUser().getUid();
                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                            userMap.put("nama", name);
                            userMap.put("email", email);
                            userMap.put("role", "admin");

                            FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap)
                                    .addOnCompleteListener(fsTask -> {
                                        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                                        api.register(uid, name, email, password, "admin").enqueue(new Callback<com.bagus.toko_baju_uas.model.BaseResponse>() {
                                            @Override
                                            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.BaseResponse> response) {
                                                Toast.makeText(AdminActivity.this, "Admin berhasil ditambahkan!", Toast.LENGTH_LONG).show();
                                                tempApp.delete();
                                                loadAdminDashboard();
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Throwable t) { tempApp.delete(); }
                                        });
                                    });
                        } else {
                            tempApp.delete();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
