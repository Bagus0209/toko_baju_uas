package com.bagus.toko_baju_uas.util;

import android.content.Context;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSyncUtil {
    public static void syncUser(Context context, String role) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();
        if (email == null) return;

        android.content.SharedPreferences sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String localName = sp.getString("user_profile_name_" + uid, user.getDisplayName());
        if (localName == null || localName.isEmpty()) {
            localName = email.split("@")[0];
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.syncUser(uid, localName, email, role).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                // Silently succeed
                android.util.Log.d("UserSyncUtil", "User synced successfully: " + (response.body() != null ? response.body().getMessage() : ""));
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                // Silently fail
                android.util.Log.e("UserSyncUtil", "Failed to sync user: " + t.getMessage());
            }
        });
    }
}
