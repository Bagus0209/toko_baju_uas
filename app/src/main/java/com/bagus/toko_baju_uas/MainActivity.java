package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.LoginResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText etEmail, etPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mFirestore;

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
        super.setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Gunakan Web Client ID eksplisit dari google-services.json untuk menghindari mismatch
        String webClientId = "610252703058-ml0jita58ja9lp5dtu5o4b7hhb4aicvi.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId) 
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        MaterialButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        tvSignUp.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                prosesLoginHybrid(email, password);
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            signInWithGoogle();
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                if (e.getStatusCode() == 10) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Google Sign In Developer Mismatch")
                            .setMessage("Gagal masuk dengan Google (Error 10: SHA-1 Mismatch).\n\n" +
                                    "Package: " + getPackageName() + "\n" +
                                    "SHA-1 debug: " + getDebugSha1() + "\n\n" +
                                    "Tambahkan SHA-1 ini ke Firebase Console untuk package tersebut, lalu download ulang google-services.json.")
                            .setPositiveButton("Masuk Demo", (dialog, which) -> {
                                prosesLoginHybrid("pengunjung@luxethreads.com", "user123");
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                } else {
                    Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserRoleFirestore(user);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        Toast.makeText(MainActivity.this, "Firebase Auth Gagal: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void prosesLoginHybrid(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        authMySQL(email, password);
                    } else {
                        // Fallback: Check local MySQL database first
                        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                        api.login(email, password).enqueue(new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                    // Local MySQL login succeeded!
                                    String userRole = response.body().getData().getRole();
                                    String nama = response.body().getData().getUsername();
                                    
                                    // Try to register user dynamically in Firebase Auth so they can use Firebase features
                                    mAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(MainActivity.this, regTask -> {
                                                if (regTask.isSuccessful()) {
                                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                                    if (firebaseUser != null) {
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("nama", nama);
                                                        userData.put("email", email);
                                                        userData.put("role", userRole);
                                                        mFirestore.collection("users").document(firebaseUser.getUid()).set(userData)
                                                                .addOnCompleteListener(fsTask -> redirectToDashboard(userRole));
                                                    } else {
                                                        redirectToDashboard(userRole);
                                                    }
                                                } else {
                                                    // User might already exist in Firebase Auth or registration failed.
                                                    // Proceed using MySQL locally
                                                    redirectToDashboard(userRole);
                                                }
                                            });
                                } else {
                                    // Both Firebase and MySQL failed. Show original Firebase login error.
                                    Toast.makeText(MainActivity.this, "Login Gagal: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                                // Diagnostik detail agar Anda tahu IP mana yang salah
                                String currentIp = com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP;
                                String errorType = (t instanceof java.net.ConnectException) ? "Server Mati di " + currentIp : 
                                                 (t instanceof java.net.SocketTimeoutException) ? "Timeout/Firewall di " + currentIp : "Error: " + t.getMessage();
                                
                                Toast.makeText(MainActivity.this, "Koneksi Gagal: " + errorType, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
    }

    private void authMySQL(String email, String password) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.login(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    redirectToDashboard(response.body().getData().getRole());
                } else {
                    checkUserRoleFirestore(mAuth.getCurrentUser());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                checkUserRoleFirestore(mAuth.getCurrentUser());
            }
        });
    }

    private void checkUserRoleFirestore(FirebaseUser user) {
        if (user == null) return;
        mFirestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        redirectToDashboard(doc.getString("role"));
                    } else {
                        saveNewUserFirestore(user);
                    }
                })
                .addOnFailureListener(e -> {
                    android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
                    String cachedRole = sp.getString("cached_user_role", "");
                    if (!cachedRole.isEmpty()) {
                        redirectToDashboard(cachedRole);
                    } else {
                        Toast.makeText(this, "Session error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNewUserFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nama", (user.getDisplayName() != null) ? user.getDisplayName() : "User");
        userData.put("email", user.getEmail());
        userData.put("role", "pengunjung");

        mFirestore.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Berhasil mendaftarkan akun baru", Toast.LENGTH_SHORT).show();
                    redirectToDashboard("pengunjung");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal simpan ke Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    redirectToDashboard("pengunjung");
                });
    }

    private void redirectToDashboard(String role) {
        // Tandai sesi sebagai aktif karena login berhasil
        com.bagus.toko_baju_uas.api.ApiClient.isSessionActive = true;

        // Normalisasi role agar tidak null dan selalu huruf kecil
        String finalRole = (role != null && !role.isEmpty()) ? role.toLowerCase().trim() : "pengunjung";
        
        // Cache user role untuk mempercepat loading berikutnya
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        sp.edit().putString("cached_user_role", finalRole).apply();
        
        // Sinkronisasi data user ke MySQL lokal
        com.bagus.toko_baju_uas.util.UserSyncUtil.syncUser(this, finalRole);
        
        Intent intent;
        // Hanya kirim ke Admin jika role benar-benar 'admin'
        if ("admin".equals(finalRole)) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, PengunjungActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private String getDebugSha1() {
        try {
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (packageInfo.signingInfo != null && packageInfo.signingInfo.getApkContentsSigners().length > 0) {
                    byte[] cert = packageInfo.signingInfo.getApkContentsSigners()[0].toByteArray();
                    return sha1FromBytes(cert);
                }
            } else {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    return sha1FromBytes(packageInfo.signatures[0].toByteArray());
                }
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    private String sha1FromBytes(byte[] certBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] sha1 = digest.digest(certBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : sha1) {
                hexString.append(String.format("%02X:", b));
            }
            if (hexString.length() > 0) {
                hexString.setLength(hexString.length() - 1);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) checkUserRoleFirestore(mAuth.getCurrentUser());
    }
}
