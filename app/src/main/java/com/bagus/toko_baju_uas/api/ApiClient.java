package com.bagus.toko_baju_uas.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Gunakan IP Wi-Fi laptop Anda agar bisa diakses dari HP Fisik maupun Emulator
    public static final String IP_LAPTOP = "192.168.1.16";
    
    private static final String BASE_URL = "http://" + IP_LAPTOP + "/api_tokobaju/";
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