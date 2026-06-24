package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private RadioGroup rgRole;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        MaterialButton btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvSignIn = findViewById(R.id.tvSignIn);

        btnSignUp.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            validateAndRegister();
        });

        tvSignIn.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });
    }

    private void validateAndRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedRole = "pengunjung";
        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == R.id.rbAdmin) {
            selectedRole = "admin";
            checkAdminLimitThenRegister(name, email, password, "admin");
        } else {
            prosesRegistrasiHybrid(name, email, password, "pengunjung");
        }
    }

    private void checkAdminLimitThenRegister(String name, String email, String password, String role) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.checkAdminCount().enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        prosesRegistrasiHybrid(name, email, password, role);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Slot Admin sudah penuh (Maks 3)!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Gagal cek limit: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prosesRegistrasiHybrid(String name, String email, String password, String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), name, email, role);
                            registerToMySQL(user.getUid(), name, email, password, role);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Pendaftaran Gagal: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerToMySQL(String uid, String name, String email, String password, String role) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.register(uid, name, email, password, role).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isStatus()) {
                    Toast.makeText(RegisterActivity.this, "Peringatan: Gagal menyimpan data ke MySQL. Cek database Anda.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Kesalahan Jaringan: Gagal menghubungkan ke MySQL server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserToFirestore(String uid, String name, String email, String role) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nama", name);
        userMap.put("email", email);
        userMap.put("role", role);

        mFirestore.collection("users").document(uid).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil sebagai " + role, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}