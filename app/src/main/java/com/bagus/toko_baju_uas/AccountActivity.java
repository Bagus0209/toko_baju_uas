package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AccountActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> pickImageLauncher;
    private com.google.android.material.imageview.ShapeableImageView ivProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_account);

        TextView tvName = findViewById(R.id.tvAccountName);
        TextView tvEmail = findViewById(R.id.tvAccountEmail);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Register Image Picker contract
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadProfileImage(uri, ivProfilePicture);
                    }
                }
        );

        if (ivProfilePicture != null) {
            ivProfilePicture.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                showProfilePhotoOptions(ivProfilePicture);
            });
        }

        if (user != null) {
            tvEmail.setText(user.getEmail());
            
            android.content.SharedPreferences spSettings = getSharedPreferences("app_settings", MODE_PRIVATE);
            String localName = spSettings.getString("user_profile_name_" + user.getUid(), null);
            String localAddress = spSettings.getString("user_profile_address_" + user.getUid(), null);
            String localPhotoUrl = spSettings.getString("user_profile_photo_" + user.getUid(), null);

            if (localName != null) {
                tvName.setText(localName);
            }
            if (localAddress != null) {
                spSettings.edit().putString("user_default_address", localAddress).apply();
            }
            if (localPhotoUrl != null) {
                loadProfileImage(localPhotoUrl, ivProfilePicture);
            }
            
            // Ambil data dari Firestore
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String fsName = doc.getString("nama");
                            String fsAlamat = doc.getString("alamat");
                            String fsPhoto = doc.getString("photoUrl");

                            if (fsName != null) {
                                if (localName == null) {
                                    tvName.setText(fsName);
                                }
                                spSettings.edit().putString("user_profile_name_" + user.getUid(), fsName).apply();
                            }
                            if (fsAlamat != null && !fsAlamat.isEmpty()) {
                                if (localAddress == null) {
                                    spSettings.edit().putString("user_default_address", fsAlamat).apply();
                                }
                                spSettings.edit().putString("user_profile_address_" + user.getUid(), fsAlamat).apply();
                            }
                            if (fsPhoto != null) {
                                if (localPhotoUrl == null) {
                                    loadProfileImage(fsPhoto, ivProfilePicture);
                                }
                                spSettings.edit().putString("user_profile_photo_" + user.getUid(), fsPhoto).apply();
                            }
                        } else {
                            if (localName == null) {
                                tvName.setText("User Luxe");
                            }
                        }
                    });
        }

        // EDIT PROFILE
        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                if (user == null) return;

                android.widget.EditText input = new android.widget.EditText(this);
                input.setText(tvName.getText().toString());

                android.widget.FrameLayout container = new android.widget.FrameLayout(this);
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int margin = (int) (16 * getResources().getDisplayMetrics().density);
                params.setMargins(margin, margin / 2, margin, margin / 2);
                input.setLayoutParams(params);
                container.addView(input);

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Edit Nama Profil")
                        .setView(container)
                        .setPositiveButton("Simpan", (dialog, which) -> {
                            String newName = input.getText().toString().trim();
                            if (!newName.isEmpty()) {
                                // Save locally first
                                getSharedPreferences("app_settings", MODE_PRIVATE)
                                        .edit().putString("user_profile_name_" + user.getUid(), newName).apply();
                                tvName.setText(newName);
                                Toast.makeText(this, "Nama profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

                                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                        .update("nama", newName)
                                        .addOnFailureListener(e -> {
                                            android.util.Log.d("FirestoreSync", "Silently failed to sync name: " + e.getMessage());
                                        });
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });
        }

        // PERSONAL INFORMATION
        View btnPersonalInfo = findViewById(R.id.btnPersonalInfo);
        if (btnPersonalInfo != null) {
            btnPersonalInfo.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                if (user == null) return;
                FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String msg = "Nama: " + doc.getString("nama") + "\nEmail: " + user.getEmail() + "\nRole: " + doc.getString("role");
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Informasi Pribadi")
                                        .setMessage(msg)
                                        .setPositiveButton("Ubah Foto / Avatar", (dialog, which) -> {
                                            showProfilePhotoOptions(ivProfilePicture);
                                        })
                                        .setNegativeButton("Tutup", null)
                                        .show();
                            }
                        });
            });
        }

        // SECURITY
        View btnSecurity = findViewById(R.id.btnSecurity);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                if (user == null) return;
                
                StringBuilder providerStr = new StringBuilder();
                boolean isEmailPassword = false;
                for (com.google.firebase.auth.UserInfo profile : user.getProviderData()) {
                    String providerId = profile.getProviderId();
                    if (providerId.equals("google.com")) {
                        providerStr.append("Google Sign-In");
                    } else if (providerId.equals("password")) {
                        providerStr.append("Email & Password");
                        isEmailPassword = true;
                    }
                }
                if (providerStr.length() == 0) {
                    providerStr.append("Firebase Auth");
                }
                
                String secureMsg = "Metode Otentikasi: " + providerStr.toString() + "\n" +
                                   "Email: " + user.getEmail() + "\n" +
                                   "UID Keamanan: " + user.getUid() + "\n\n" +
                                   "Sistem menggunakan proteksi otentikasi aman dari Google Firebase.";
                
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Keamanan Akun")
                        .setMessage(secureMsg);
                        
                if (isEmailPassword) {
                    builder.setPositiveButton("Reset Password", (dialog, which) -> {
                        sendPasswordReset(user.getEmail());
                    });
                }
                
                builder.setNegativeButton("Tutup", null);
                builder.show();
            });
        }

        // MY ORDERS
        View btnMyOrders = findViewById(R.id.btnMyOrders);
        if (btnMyOrders != null) {
            btnMyOrders.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                startActivity(new Intent(this, PaymentHistoryActivity.class));
            });
        }

        // SHIPPING ADDRESSES
        View btnShippingAddresses = findViewById(R.id.btnShippingAddresses);
        if (btnShippingAddresses != null) {
            btnShippingAddresses.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                if (user == null) return;
                
                android.content.SharedPreferences spSettings = getSharedPreferences("app_settings", MODE_PRIVATE);
                String currentAddress = spSettings.getString("user_default_address", "Jl. Luxe Threads No. 8, Jakarta Pusat");
                
                android.widget.EditText inputAddress = new android.widget.EditText(this);
                inputAddress.setText(currentAddress);
                
                android.widget.FrameLayout container = new android.widget.FrameLayout(this);
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int margin = (int) (16 * getResources().getDisplayMetrics().density);
                params.setMargins(margin, margin / 2, margin, margin / 2);
                inputAddress.setLayoutParams(params);
                container.addView(inputAddress);
                
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Ubah Alamat Pengiriman")
                        .setView(container)
                        .setPositiveButton("Simpan", (dialog, which) -> {
                            String newAddress = inputAddress.getText().toString().trim();
                            if (!newAddress.isEmpty()) {
                                // Save locally first
                                android.content.SharedPreferences.Editor editor = spSettings.edit();
                                editor.putString("user_default_address", newAddress);
                                editor.putString("user_profile_address_" + user.getUid(), newAddress);
                                editor.apply();
                                
                                Toast.makeText(this, "Alamat pengiriman berhasil diperbarui", Toast.LENGTH_SHORT).show();

                                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                        .update("alamat", newAddress)
                                        .addOnFailureListener(e -> {
                                            android.util.Log.d("FirestoreSync", "Silently failed to sync address: " + e.getMessage());
                                        });
                            } else {
                                Toast.makeText(this, "Alamat tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });
        }

        // PAYMENT METHODS
        View btnPaymentMethods = findViewById(R.id.btnPaymentMethods);
        if (btnPaymentMethods != null) {
            btnPaymentMethods.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Metode Pembayaran")
                        .setMessage("Metode Terhubung:\n• QRIS / GOPAY (Default)\n• Bank Transfer (Virtual Account)\n• COD")
                        .setPositiveButton("Tutup", null)
                        .show();
            });
        }

        // NOTIFICATIONS SWITCH
        com.google.android.material.switchmaterial.SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "diaktifkan" : "dinonaktifkan";
                Toast.makeText(this, "Notifikasi push " + status, Toast.LENGTH_SHORT).show();
            });
        }

        // DARK MODE SWITCH
        android.content.SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sp.getBoolean("dark_mode", false);

        com.google.android.material.switchmaterial.SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(isDarkMode);
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                android.content.SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("dark_mode", isChecked);
                editor.apply();

                if (isChecked) {
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        }

        // LOGOUT
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            logout();
        });

        // BOTTOM NAV
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_shop) {
                finish(); // Kembali ke katalog
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, PaymentHistoryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(this, PengunjungActivity.class);
                intent.putExtra("focus_search", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_bag) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void showProfilePhotoOptions(com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        String[] options = {"Ubah Foto Profil (Galeri)", "Pilih Avatar Default", "Pilih Ikon Bawaan", "Hapus Foto Profil", "Batal"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Foto Profil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch("image/*");
                    } else if (which == 1) {
                        showAvatarSelectorDialog(ivProfilePicture);
                    } else if (which == 2) {
                        showIconSelectorDialog(ivProfilePicture);
                    } else if (which == 3) {
                        removeProfileImage(ivProfilePicture);
                    }
                })
                .show();
    }

    private void showAvatarSelectorDialog(com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        String[] avatars = {"Avatar Pria", "Avatar Wanita", "Avatar Unisex"};
        String[] avatarFiles = {"avatar1.png", "avatar2.png", "avatar3.png"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pilih Avatar")
                .setItems(avatars, (dialog, which) -> {
                    String selectedAvatar = avatarFiles[which];
                    saveProfilePhotoToFirestore(selectedAvatar, ivProfilePicture);
                })
                .show();
    }

    private void showIconSelectorDialog(com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        String[] icons = {"Pengguna (Person)", "Keranjang Belanja", "Pencarian", "Beranda", "Kotak Inventaris"};
        String[] iconKeys = {"icon_person", "icon_bag", "icon_search", "icon_home", "icon_inventory"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pilih Ikon Bawaan")
                .setItems(icons, (dialog, which) -> {
                    String selectedIcon = iconKeys[which];
                    saveProfilePhotoToFirestore(selectedIcon, ivProfilePicture);
                })
                .show();
    }

    private void saveProfilePhotoToFirestore(String photoFileName, com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        // Save locally first
        getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putString("user_profile_photo_" + user.getUid(), photoFileName).apply();
        loadProfileImage(photoFileName, ivProfilePicture);
        Toast.makeText(this, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .update("photoUrl", photoFileName)
                .addOnFailureListener(e -> {
                    android.util.Log.d("FirestoreSync", "Silently failed to sync photoUrl: " + e.getMessage());
                });
    }

    private void removeProfileImage(com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        // Save locally first
        getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putString("user_profile_photo_" + user.getUid(), "").apply();
        ivProfilePicture.setImageResource(R.drawable.ic_person);
        ivProfilePicture.setPadding(0, 0, 0, 0);
        Toast.makeText(this, "Foto profil berhasil dihapus", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .update("photoUrl", "")
                .addOnFailureListener(e -> {
                    android.util.Log.d("FirestoreSync", "Silently failed to remove photoUrl: " + e.getMessage());
                });
    }

    private void loadProfileImage(String photoUrl, com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            ivProfilePicture.setImageResource(R.drawable.ic_person);
            ivProfilePicture.setPadding(0, 0, 0, 0);
            return;
        }
        
        if (photoUrl.startsWith("icon_")) {
            int drawableId;
            switch (photoUrl) {
                case "icon_bag":
                    drawableId = R.drawable.ic_bag;
                    break;
                case "icon_search":
                    drawableId = R.drawable.ic_search;
                    break;
                case "icon_home":
                    drawableId = R.drawable.ic_home;
                    break;
                case "icon_inventory":
                    drawableId = R.drawable.ic_inventory;
                    break;
                default:
                    drawableId = R.drawable.ic_person;
                    break;
            }
            ivProfilePicture.setImageResource(drawableId);
            int padding = (int) (20 * getResources().getDisplayMetrics().density);
            ivProfilePicture.setPadding(padding, padding, padding, padding);
            return;
        }
        
        ivProfilePicture.setPadding(0, 0, 0, 0);
        
        String fullUrl;
        if (photoUrl.startsWith("http")) {
            fullUrl = photoUrl;
        } else {
            // Gunakan folder 'uploads' untuk konsistensi di seluruh aplikasi
            fullUrl = "http://" + ApiClient.IP_LAPTOP + "/api_tokobaju/uploads/" + photoUrl;
        }
        
        Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivProfilePicture);
    }

    private void uploadProfileImage(Uri uri, com.google.android.material.imageview.ShapeableImageView ivProfilePicture) {
        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Gagal memproses file gambar!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Mengupload gambar...", Toast.LENGTH_SHORT).show();

        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "image/jpeg";
        
        RequestBody requestFile = RequestBody.create(
                MediaType.parse(mimeType),
                file
        );
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.uploadGambar(body).enqueue(new Callback<com.bagus.toko_baju_uas.model.UploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    String uploadedFileName = response.body().getFileName();
                    saveProfilePhotoToFirestore(uploadedFileName, ivProfilePicture);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal upload";
                    Toast.makeText(AccountActivity.this, "Gagal upload: " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.UploadResponse> call, @NonNull Throwable t) {
                Toast.makeText(AccountActivity.this, "Gagal upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = new File(getCacheDir(), "profile_temp.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
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

    private void sendPasswordReset(String email) {
        if (email == null || email.isEmpty()) return;
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email reset password telah dikirim ke " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Gagal mengirim email reset: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
