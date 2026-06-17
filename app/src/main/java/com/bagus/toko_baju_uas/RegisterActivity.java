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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private RadioGroup rgRole;
    private MaterialButton btnSignUp;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Kenalkan komponen dari XML ke Java
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Cek apakah kolom kosong
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tentukan role berdasarkan RadioButton terpilih
            String role = "customer"; // Default
            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            if (selectedRoleId == R.id.rbAdmin) {
                role = "admin";
            } else if (selectedRoleId == R.id.rbPengunjung) {
                role = "customer"; // Maps to customer in DB
            }

            prosesRegistrasi(name, email, password, role);
        });

        tvSignIn.setOnClickListener(v -> finish());
    }

    private void prosesRegistrasi(String name, String email, String password, String role) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.register(name, email, password, role).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke halaman login
                    } else {
                        Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Terjadi kesalahan pada respon server.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
