package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.BajuAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.AdminStatsResponse;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvBajuAdmin;
    private DrawerLayout drawerLayout;
    private BajuAdminAdapter adapter;
    private List<BajuModel> listBaju = new ArrayList<>();
    
    private TextView tvTotalProduk, tvTotalOrders, tvTotalRevenue;

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

        rvBajuAdmin = findViewById(R.id.rvBajuAdmin);
        MaterialButton btnTambahProduk = findViewById(R.id.btnTambahProduk);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        
        tvTotalProduk = findViewById(R.id.tvTotalProduk); 
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);

        rvBajuAdmin.setLayoutManager(new LinearLayoutManager(this));
        
        loadAdminDashboard();
        fetchDataBarang();

        btnTambahProduk.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(AdminActivity.this, TambahProdukActivity.class));
        });

        btnMenu.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            drawerLayout.openDrawer(GravityCompat.START);
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_dashboard) {
                // Already here, just refresh
                loadAdminDashboard();
                fetchDataBarang();
            } else if (id == R.id.nav_inventory) {
                // Focus on inventory list
                rvBajuAdmin.smoothScrollToPosition(0);
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
            } else {
                Toast.makeText(this, "Fitur " + item.getTitle() + " akan segera hadir!", Toast.LENGTH_SHORT).show();
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
                    tvTotalProduk.setText(String.valueOf(data.total_products));
                    tvTotalOrders.setText(String.valueOf(data.total_orders));
                    
                    Locale localeID = Locale.forLanguageTag("id-ID");
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                    tvTotalRevenue.setText(formatRupiah.format(data.total_revenue));
                }
            }
            @Override
            public void onFailure(@NonNull Call<AdminStatsResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchDataBarang() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterface.getBarang().enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listBaju = response.body().getData();
                    adapter = new BajuAdminAdapter(AdminActivity.this, listBaju);
                    rvBajuAdmin.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataBarang();
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

        // Populate Current User Details
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            
            // Load local cached name first
            String localName = sp.getString("user_profile_name_" + user.getUid(), null);
            if (localName != null) {
                etNama.setText(localName);
            }
            
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String fsName = doc.getString("nama");
                            if (fsName != null && localName == null) {
                                etNama.setText(fsName);
                            }
                        }
                    });
        }

        // Populate inputs
        etIp.setText(com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP);
        switchDark.setChecked(isDarkMode);
        switchNotif.setChecked(isNotifications);

        // Connection Test Logic
        btnTest.setOnClickListener(v -> {
            String testIp = etIp.getText().toString().trim();
            if (testIp.isEmpty()) {
                tvStatus.setText("Status: IP kosong");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_cancelled));
                return;
            }
            tvStatus.setText("Menghubungkan...");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.luxe_gold));

            String testUrl = "http://" + testIp + "/toko%20baju/";
            retrofit2.Retrofit testRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(testUrl)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            com.bagus.toko_baju_uas.api.ApiInterface testApi = testRetrofit.create(com.bagus.toko_baju_uas.api.ApiInterface.class);
            testApi.getBarang().enqueue(new retrofit2.Callback<com.bagus.toko_baju_uas.model.BarangResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<com.bagus.toko_baju_uas.model.BarangResponse> call, @NonNull retrofit2.Response<com.bagus.toko_baju_uas.model.BarangResponse> response) {
                    if (response.isSuccessful()) {
                        tvStatus.setText("Status: Sukses Terhubung");
                        tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_completed));
                    } else {
                        tvStatus.setText("Status: Gagal (HTTP " + response.code() + ")");
                        tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_cancelled));
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<com.bagus.toko_baju_uas.model.BarangResponse> call, @NonNull Throwable t) {
                    tvStatus.setText("Status: Gagal Terhubung");
                    tvStatus.setTextColor(ContextCompat.getColor(AdminActivity.this, R.color.status_cancelled));
                }
            });
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    boolean darkChecked = switchDark.isChecked();
                    boolean notifChecked = switchNotif.isChecked();
                    String newIp = etIp.getText().toString().trim();

                    android.content.SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("dark_mode", darkChecked);
                    editor.putBoolean("admin_notifications", notifChecked);

                    if (!newIp.isEmpty()) {
                        com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP = newIp;
                        editor.putString("server_ip", newIp);
                    }
                    editor.apply();

                    // Runnable to apply dark mode after any async tasks
                    Runnable applyTheme = () -> {
                        if (darkChecked) {
                            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    };

                    // Save Name
                    String newName = etNama.getText().toString().trim();
                    if (user != null && !newName.isEmpty()) {
                        editor.putString("user_profile_name_" + user.getUid(), newName);
                        editor.apply();
                        
                        Toast.makeText(AdminActivity.this, "Pengaturan & nama berhasil disimpan", Toast.LENGTH_SHORT).show();
                        applyTheme.run();

                        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                .update("nama", newName)
                                .addOnFailureListener(e -> {
                                    android.util.Log.d("FirestoreSync", "Silently failed to update admin name: " + e.getMessage());
                                });
                    } else {
                        Toast.makeText(AdminActivity.this, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show();
                        applyTheme.run();
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
            android.widget.Button btnPositive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(v -> {
                String nama = etNewAdminNama.getText().toString().trim();
                String email = etNewAdminEmail.getText().toString().trim();
                String password = etNewAdminPassword.getText().toString().trim();

                if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(AdminActivity.this, "Password minimal harus 6 karakter!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.contains("@")) {
                    Toast.makeText(AdminActivity.this, "Email tidak valid!", Toast.LENGTH_SHORT).show();
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
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        registerNewAdmin(name, email, password);
                    } else {
                        Toast.makeText(AdminActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Gagal mengecek limit admin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Koneksi error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerNewAdmin(String name, String email, String password) {
        Toast.makeText(this, "Mendaftarkan admin baru...", Toast.LENGTH_SHORT).show();
        
        try {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            String tempAppName = "TempAdminReg_" + System.currentTimeMillis();
            FirebaseApp tempApp = FirebaseApp.initializeApp(this, options, tempAppName);
            com.google.firebase.auth.FirebaseAuth tempAuth = com.google.firebase.auth.FirebaseAuth.getInstance(tempApp);

            tempAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                            String uid = task.getResult().getUser().getUid();

                            // 1. Simpan ke Firestore
                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                            userMap.put("nama", name);
                            userMap.put("email", email);
                            userMap.put("role", "admin");

                            FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap)
                                    .addOnCompleteListener(fsTask -> {
                                        // 2. Register ke MySQL
                                        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                                        api.register(uid, name, email, password, "admin").enqueue(new Callback<com.bagus.toko_baju_uas.model.BaseResponse>() {
                                            @Override
                                            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.BaseResponse> response) {
                                                Toast.makeText(AdminActivity.this, "Admin baru berhasil ditambahkan!", Toast.LENGTH_LONG).show();
                                                tempApp.delete();
                                                loadAdminDashboard();
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Throwable t) {
                                                Toast.makeText(AdminActivity.this, "Sinkronisasi ke database MySQL gagal, namun akun Firebase terdaftar", Toast.LENGTH_LONG).show();
                                                tempApp.delete();
                                            }
                                        });
                                      });
                          } else {
                              String errorMsg = task.getException() != null ? task.getException().getMessage() : "Kesalahan tidak diketahui";
                              Toast.makeText(AdminActivity.this, "Gagal membuat akun admin: " + errorMsg, Toast.LENGTH_LONG).show();
                              tempApp.delete();
                          }
                      });
          } catch (Exception e) {
              Toast.makeText(this, "Error in Firebase secondary instance: " + e.getMessage(), Toast.LENGTH_LONG).show();
          }
      }
  }