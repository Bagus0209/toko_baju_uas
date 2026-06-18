package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_account);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView tvName = findViewById(R.id.tvAccountName);
            TextView tvEmail = findViewById(R.id.tvAccountEmail);
            
            tvEmail.setText(user.getEmail());
            
            // Ambil nama dari Firestore agar lebih akurat
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            tvName.setText(doc.getString("nama"));
                        } else {
                            tvName.setText("User Luxe");
                        }
                    });
        }

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            logout();
        });

        // Settings Click Listeners
        findViewById(R.id.btnEditProfileTop).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.btnPersonalInfo).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, PersonalInformationActivity.class));
        });

        findViewById(R.id.btnSecurity).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, SecurityActivity.class));
        });

        findViewById(R.id.btnMyOrders).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, PaymentHistoryActivity.class));
        });

        findViewById(R.id.btnShippingAddress).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, ShippingAddressActivity.class));
        });

        findViewById(R.id.btnPaymentMethods).setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(this, PaymentMethodsActivity.class));
        });

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

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}