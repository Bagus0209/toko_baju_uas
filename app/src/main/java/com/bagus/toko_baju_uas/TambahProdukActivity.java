package com.bagus.toko_baju_uas;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahProdukActivity extends AppCompatActivity {

    private TextInputEditText etNamaBaju, etHarga, etStok;
    private ImageView ivPreviewGambar;
    private LinearLayout layoutPlaceholder;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri = null;
    private com.google.android.material.checkbox.MaterialCheckBox cbKonfirmasiTambah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_tambah_produk);

        etNamaBaju = findViewById(R.id.etNamaBaju);
        etHarga = findViewById(R.id.etHarga);
        etStok = findViewById(R.id.etStok);
        MaterialButton btnSimpan = findViewById(R.id.btnSimpan);
        cbKonfirmasiTambah = findViewById(R.id.cbKonfirmasiTambah);

        com.google.android.material.card.MaterialCardView cardPilihGambar = findViewById(R.id.cardPilihGambar);
        ivPreviewGambar = findViewById(R.id.ivPreviewGambar);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        // Register Image Picker contract
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreviewGambar.setImageURI(uri);
                        ivPreviewGambar.setVisibility(View.VISIBLE);
                        layoutPlaceholder.setVisibility(View.GONE);
                    }
                }
        );

        cardPilihGambar.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            pickImageLauncher.launch("image/*");
        });

        btnSimpan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (!cbKonfirmasiTambah.isChecked()) {
                Toast.makeText(this, "Silakan centang konfirmasi terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            String nama = etNamaBaju.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String stokStr = etStok.getText().toString().trim();

            if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Nama, Harga, dan Stok wajib diisi!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int harga = Integer.parseInt(hargaStr);
                    int stok = Integer.parseInt(stokStr);
                    uploadImageAndSave(nama, harga, stok);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Harga dan Stok harus berupa angka!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private java.io.File getFileFromUri(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            // Nama file unik agar tidak bentrok
            String fileName = "prod_" + System.currentTimeMillis() + ".jpg";
            java.io.File tempFile = new java.io.File(getCacheDir(), fileName);
            
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImageAndSave(String nama, int harga, int stok) {
        if (selectedImageUri == null) {
            saveProductToDatabase(nama, harga, stok, "default.jpg");
            return;
        }

        java.io.File file = getFileFromUri(selectedImageUri);
        if (file == null) {
            Toast.makeText(this, "Gagal memproses gambar!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading agar user tahu proses sedang berjalan
        Toast.makeText(this, "Sedang mengunggah...", Toast.LENGTH_SHORT).show();

        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/*"),
                file
        );
        
        // Pastikan field name "image" sesuai dengan yang ada di script PHP Anda
        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.uploadGambar(body).enqueue(new Callback<com.bagus.toko_baju_uas.model.UploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        String uploadedFileName = response.body().getFileName();
                        saveProductToDatabase(nama, harga, stok, uploadedFileName);
                    } else {
                        Toast.makeText(TambahProdukActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorDetail = "";
                    try {
                        if (response.errorBody() != null) {
                            errorDetail = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    
                    android.util.Log.e("UPLOAD_ERROR", "Code: " + response.code() + " Body: " + errorDetail);
                    Toast.makeText(TambahProdukActivity.this, "Server Error (" + response.code() + "). Cek Logcat!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Throwable t) {
                Toast.makeText(TambahProdukActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProductToDatabase(String nama, int harga, int stok, String gambar) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.tambahBarang(nama, harga, stok, gambar).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(TambahProdukActivity.this, "Berhasil tambah barang!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TambahProdukActivity.this, "Gagal tambah barang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(TambahProdukActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}