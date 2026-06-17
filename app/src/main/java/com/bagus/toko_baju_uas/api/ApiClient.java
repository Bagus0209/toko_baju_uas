package com.bagus.toko_baju_uas.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 10.0.2.2 adalah IP khusus di emulator Android Studio untuk mengakses localhost komputer
    // Nanti 'api_tokobaju' adalah nama folder PHP yang akan kita buat di htdocs XAMPP
    private static final String BASE_URL = "http://10.0.2.2/api_tokobaju/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}