package com.bagus.toko_baju_uas.api;

import com.bagus.toko_baju_uas.model.BarangResponse;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    // Endpoint untuk proses Login
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // Endpoint untuk Tambah Barang (Perhatikan tanda kurung yang lengkap)
    @FormUrlEncoded
    @POST("tambah_barang.php")
    Call<BaseResponse> tambahBarang(
            @Field("nama_barang") String nama,
            @Field("harga") int harga,
            @Field("stok") int stok
    );

    // Endpoint untuk mengambil daftar semua baju
    @GET("get_barang.php")
    Call<BarangResponse> getBarang();
}