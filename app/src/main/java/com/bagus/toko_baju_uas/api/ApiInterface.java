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

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<BaseResponse> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("role") String role
    );

    @FormUrlEncoded
    @POST("tambah_barang.php")
    Call<BaseResponse> tambahBarang(
            @Field("nama_barang") String nama,
            @Field("harga") int harga,
            @Field("stok") int stok,
            @Field("gambar") String gambar
    );

    @GET("get_barang.php")
    Call<BarangResponse> getBarang();

    @FormUrlEncoded
    @POST("hapus_barang.php")
    Call<BaseResponse> hapusBarang(
            @Field("id_barang") int idBarang
    );

    @FormUrlEncoded
    @POST("update_barang.php")
    Call<BaseResponse> updateBarang(
            @Field("id_barang") int idBarang,
            @Field("nama_barang") String nama,
            @Field("harga") int harga,
            @Field("stok") int stok,
            @Field("gambar") String gambar
    );
}