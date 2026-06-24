package com.bagus.toko_baju_uas;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProdukActivity extends AppCompatActivity {

    private TextInputEditText etEditNamaBaju, etEditHarga, etEditStok, etEditGambar;
    private ImageView ivPreviewGambar;
    private LinearLayout layoutPlaceholder;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri = null;
    private int idBarang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_edit_produk);

        etEditNamaBaju = findViewById(R.id.etEditNamaBaju);
        etEditHarga = findViewById(R.id.etEditHarga);
        etEditStok = findViewById(R.id.etEditStok);
        etEditGambar = findViewById(R.id.etEditGambar);
        MaterialButton btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        com.google.android.material.card.MaterialCardView cardPilihGambar = findViewById(R.id.cardEditPilihGambar);
        ivPreviewGambar = findViewById(R.id.ivEditPreviewGambar);
        layoutPlaceholder = findViewById(R.id.layoutEditPlaceholder);

        // Register Image Picker contract
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreviewGambar.setImageURI(uri);
                        ivPreviewGambar.setVisibility(View.VISIBLE);
                        layoutPlaceholder.setVisibility(View.GONE);
                        // Disable manual text input as it's automatically uploaded
                        etEditGambar.setEnabled(false);
                        etEditGambar.setText("Upload otomatis (" + uri.getLastPathSegment() + ")");
                    }
                }
        );

        cardPilihGambar.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            pickImageLauncher.launch("image/*");
        });

        if (getIntent().getExtras() != null) {
            idBarang = getIntent().getIntExtra("id_barang", 0);
            etEditNamaBaju.setText(getIntent().getStringExtra("nama_barang"));
            etEditHarga.setText(String.valueOf(getIntent().getIntExtra("harga", 0)));
            etEditStok.setText(String.valueOf(getIntent().getIntExtra("stok", 0)));
            String existingGambar = getIntent().getStringExtra("gambar");
            etEditGambar.setText(existingGambar);

            // Load existing image if present using 'images' folder
            if (existingGambar != null && !existingGambar.isEmpty()) {
                String imageUrl = "http://" + ApiClient.IP_LAPTOP + "/api_tokobaju/images/" + existingGambar;
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivPreviewGambar);
                ivPreviewGambar.setVisibility(View.VISIBLE);
                layoutPlaceholder.setVisibility(View.GONE);
            }
        }

        btnSimpanPerubahan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            String namaBaru = etEditNamaBaju.getText().toString().trim();
            String hargaStr = etEditHarga.getText().toString().trim();
            String stokStr = etEditStok.getText().toString().trim();
            String gambarBaru = etEditGambar.getText().toString().trim();

            if (namaBaru.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                int hargaBaru = Integer.parseInt(hargaStr);
                int stokBaru = Integer.parseInt(stokStr);
                
                // Upload image if a new one is selected, else save changes directly
                uploadImageAndSave(namaBaru, hargaBaru, stokBaru, gambarBaru);
            }
        });
    }

    private java.io.File getFileFromUri(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            java.io.File tempFile = new java.io.File(getCacheDir(), "upload_temp_edit.jpg");
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

    private void uploadImageAndSave(String nama, int harga, int stok, String manualGambar) {
        if (selectedImageUri == null) {
            String finalGambar = manualGambar.isEmpty() ? "default.jpg" : manualGambar;
            saveProductToDatabase(nama, harga, stok, finalGambar);
            return;
        }

        java.io.File file = getFileFromUri(selectedImageUri);
        if (file == null) {
            Toast.makeText(this, "Gagal memproses file gambar!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Mengupload gambar...", Toast.LENGTH_SHORT).show();

        String mimeType = getContentResolver().getType(selectedImageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse(mimeType),
                file
        );
        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.uploadGambar(body).enqueue(new Callback<com.bagus.toko_baju_uas.model.UploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    String uploadedFileName = response.body().getFileName();
                    saveProductToDatabase(nama, harga, stok, uploadedFileName);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal upload";
                    Toast.makeText(EditProdukActivity.this, "Gagal upload: " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Throwable t) {
                Toast.makeText(EditProdukActivity.this, "Gagal upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProductToDatabase(String nama, int harga, int stok, String gambar) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateBarang(idBarang, nama, harga, stok, gambar).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(EditProdukActivity.this, "Berhasil memperbarui barang!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProdukActivity.this, "Gagal memperbarui barang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(EditProdukActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
