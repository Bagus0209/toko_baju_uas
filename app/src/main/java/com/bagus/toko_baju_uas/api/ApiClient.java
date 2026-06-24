package com.bagus.toko_baju_uas.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    /**
     * PENTING: Ganti IP di bawah ini!
     * - Emulator: "10.0.2.2"
     * - HP Fisik: Gunakan IPv4 dari 'ipconfig' di CMD laptop
     */
    public static String IP_LAPTOP = "192.168.1.12";
    
    // Flag untuk mendeteksi apakah aplikasi baru dibuka (Cold Start) atau resume
    public static boolean isSessionActive = false;
    
    private static String lastUsedIp = "";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null || !IP_LAPTOP.equals(lastUsedIp)) {
            lastUsedIp = IP_LAPTOP;
            String baseUrl = "http://" + IP_LAPTOP + "/api_tokobaju/";
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
