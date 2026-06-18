package com.bagus.toko_baju_uas;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahProdukActivity extends AppCompatActivity {

    private TextInputEditText etNamaBaju, etHarga, etStok, etGambar, etKategori;
    private ImageView ivPreview;
    private View placeholderLayout;
    private Uri selectedImageUri;
    private static final int REQUEST_PERMISSION = 100;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    ivPreview.setAlpha(1.0f);
                    placeholderLayout.setVisibility(View.GONE);
                    
                    String fileName = getFileNameFromUri(selectedImageUri);
                    etGambar.setText(fileName);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    // For demo purposes, we'll just show the toast. 
                    // In real app, we'd save the bitmap to a file and get the URI.
                    Toast.makeText(this, "Camera capture successful", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);

        etNamaBaju = findViewById(R.id.etNamaBaju);
        etHarga = findViewById(R.id.etHarga);
        etStok = findViewById(R.id.etStok);
        etGambar = findViewById(R.id.etGambar);
        etKategori = findViewById(R.id.etKategori);
        ivPreview = findViewById(R.id.ivPreview);
        placeholderLayout = findViewById(R.id.placeholderLayout);
        MaterialCardView cardImage = findViewById(R.id.cardImage);
        
        MaterialButton btnSimpan = findViewById(R.id.btnSimpan);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView btnReset = findViewById(R.id.btnReset);

        cardImage.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            showImagePickDialog();
        });

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            onBackPressed();
        });

        btnReset.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            clearFields();
        });

        btnSimpan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            saveProduct();
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Gallery", "Camera"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkPermissionAndOpenGallery();
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void saveProduct() {
        String nama = Objects.requireNonNull(etNamaBaju.getText()).toString().trim();
        String hargaStr = Objects.requireNonNull(etHarga.getText()).toString().trim();
        String stokStr = Objects.requireNonNull(etStok.getText()).toString().trim();

        if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            Toast.makeText(this, "Please complete the mandatory fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            RequestBody rbNama = RequestBody.create(MediaType.parse("text/plain"), nama);
            RequestBody rbHarga = RequestBody.create(MediaType.parse("text/plain"), hargaStr);
            RequestBody rbStok = RequestBody.create(MediaType.parse("text/plain"), stokStr);

            File file = new File(getRealPathFromURI(selectedImageUri));
            RequestBody rbFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part partGambar = MultipartBody.Part.createFormData("gambar", file.getName(), rbFile);

            ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
            api.tambahBarangV2(rbNama, rbHarga, rbStok, partGambar).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(TambahProdukActivity.this, "Successfully published collection!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(TambahProdukActivity.this, "Failed to upload: " + (response.body() != null ? response.body().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                    Toast.makeText(TambahProdukActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = getRealPathFromURI(uri);
        if (path != null) {
            return new File(path).getName();
        }
        return "selected_image.jpg";
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void clearFields() {
        etNamaBaju.setText("");
        etHarga.setText("");
        etStok.setText("");
        etGambar.setText("");
        etKategori.setText("");
        ivPreview.setImageResource(R.drawable.ic_add_circle);
        ivPreview.setAlpha(0.2f);
        placeholderLayout.setVisibility(View.VISIBLE);
        selectedImageUri = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to read gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }
}