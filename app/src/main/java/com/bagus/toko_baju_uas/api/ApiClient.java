package com.bagus.toko_baju_uas.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Gunakan IP Wi-Fi laptop Anda agar bisa diakses dari HP Fisik maupun Emulator
    public static String IP_LAPTOP = "192.168.56.1";
    
    private static String lastUsedIp = "";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null || !IP_LAPTOP.equals(lastUsedIp)) {
            lastUsedIp = IP_LAPTOP;
            String baseUrl = "http://" + IP_LAPTOP + "/toko%20baju/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}