package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText etEmail, etPassword;
    private android.app.ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);

        // Mengambil Web Client ID secara dinamis atau fallback ke ID manual dari google-services.json
        String webClientId = "610252703058-ml0jita58ja9lp5dtu5o4b7hhb4aicvi.apps.googleusercontent.com";
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId != 0) {
                webClientId = getString(resId);
            }
        } catch (Exception ignored) {}

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
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            handleForgotPassword();
        });

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
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserRoleFirestore(user);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Firebase Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void prosesLoginHybrid(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        authMySQL(email, password);
                    } else {
                        progressDialog.dismiss();
                        String errorMessage = "Login failed";
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "Account not found. Please Sign Up.";
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Wrong email or password. Try again.";
                        } else if (e != null) {
                            errorMessage = e.getMessage();
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void authMySQL(String email, String password) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.login(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        // Login MySQL Berhasil
                        redirectToDashboard(response.body().getData().getRole());
                    } else {
                        // Akun ada di Firebase tapi data MySQL mismatch atau role belum diset
                        Toast.makeText(MainActivity.this, "Data Sync: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        checkUserRoleFirestore(mAuth.getCurrentUser());
                    }
                } else {
                    // Masalah server (404/500)
                    String errorMsg = "Server Error (" + response.code() + "). Cek file login.php di XAMPP.";
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    checkUserRoleFirestore(mAuth.getCurrentUser());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                // Masalah koneksi (IP salah / XAMPP mati)
                String errorMsg = "Koneksi ke Database Gagal!\n1. Cek XAMPP (Apache & MySQL harus ON)\n2. Pastikan IP di ApiClient.java (" + ApiClient.IP_LAPTOP + ") sesuai dengan IP Laptop Anda.";
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                checkUserRoleFirestore(mAuth.getCurrentUser());
            }
        });
    }

    private void checkUserRoleFirestore(FirebaseUser user) {
        if (user == null) {
            progressDialog.dismiss();
            return;
        }
        mFirestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    progressDialog.dismiss();
                    if (doc.exists()) {
                        redirectToDashboard(doc.getString("role"));
                    } else {
                        saveNewUserFirestore(user);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Session error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewUserFirestore(FirebaseUser user) {
        progressDialog.show();
        Map<String, Object> userData = new HashMap<>();
        userData.put("nama", user.getDisplayName());
        userData.put("email", user.getEmail());
        userData.put("role", "pengunjung");

        mFirestore.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    redirectToDashboard("pengunjung");
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void redirectToDashboard(String role) {
        String finalRole = (role != null) ? role.toLowerCase() : "pengunjung";
        
        Intent intent;
        if (Objects.equals(finalRole, "admin")) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, PengunjungActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) checkUserRoleFirestore(mAuth.getCurrentUser());
    }
}