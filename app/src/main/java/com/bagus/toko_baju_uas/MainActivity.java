package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mengenalkan komponen dari XML ke Java
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Memberikan perintah saat tombol ditekan
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mengambil teks dan memastikan tidak null
                String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

                // Cek apakah kolom kosong
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                } else {
                    // Jika terisi, jalankan proses login ke server
                    prosesLogin(email, password);
                }
            }
        });
    }

    private void prosesLogin(String email, String password) {
        // Memanggil interface API
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LoginResponse> call = apiInterface.login(email, password);

        // Mengeksekusi panggilan ke server
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Mengambil status dari JSON balasan
                    boolean status = response.body().isStatus();
                    String message = response.body().getMessage();

                    if (status) {
                        // Jika login berhasil, cek role-nya
                        String role = response.body().getData().getRole();
                        Toast.makeText(MainActivity.this, "Selamat datang, " + role, Toast.LENGTH_SHORT).show();

                        // Logika pindah halaman berdasarkan Role
                        if (role.equals("admin")) {
                            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish(); // Menutup halaman login agar tidak bisa di-back
                        } else if (role.equals("pengunjung")) {
                            Intent intent = new Intent(MainActivity.this, PengunjungActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        // Jika login gagal (username/password salah)
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan pada respon server.", Toast.LENGTH_SHORT).show();
                }
            } // Nah, kurung kurawal ini yang sebelumnya terhapus!

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Jika koneksi internet mati atau server XAMPP mati
                Toast.makeText(MainActivity.this, "Koneksi ke server gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}