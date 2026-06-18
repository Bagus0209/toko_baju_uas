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
            @Field("uid_firebase") String uid,
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

    @retrofit2.http.Multipart
    @POST("tambah_barang_v2.php")
    Call<BaseResponse> tambahBarangV2(
            @retrofit2.http.Part("nama_barang") okhttp3.RequestBody nama,
            @retrofit2.http.Part("harga") okhttp3.RequestBody harga,
            @retrofit2.http.Part("stok") okhttp3.RequestBody stok,
            @retrofit2.http.Part okhttp3.MultipartBody.Part gambar
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

    // KERANJANG & TRANSAKSI
    @FormUrlEncoded
    @POST("add_to_cart.php")
    Call<BaseResponse> addToCart(
            @Field("id_user") String uid,
            @Field("id_barang") int idBarang,
            @Field("jumlah") int jumlah
    );

    @GET("get_cart.php")
    Call<com.bagus.toko_baju_uas.model.CartResponse> getCart(
            @retrofit2.http.Query("id_user") String uid
    );

    @FormUrlEncoded
    @POST("delete_cart.php")
    Call<BaseResponse> deleteCart(
            @Field("id_cart") int idCart
    );

    @FormUrlEncoded
    @POST("checkout.php")
    Call<BaseResponse> checkout(
            @Field("id_user") String uid,
            @Field("total_harga") int totalHarga
    );

    // ADMIN SERVICES
    @GET("admin_service.php?action=get_stats")
    Call<com.bagus.toko_baju_uas.model.AdminStatsResponse> getAdminStats();

    @GET("admin_service.php?action=check_admin_count")
    Call<com.bagus.toko_baju_uas.model.BaseResponse> checkAdminCount();

    @GET("admin_service.php?action=get_orders")
    Call<com.bagus.toko_baju_uas.model.OrderResponse> getAdminOrders();

    @FormUrlEncoded
    @POST("admin_service.php?action=update_order_status")
    Call<BaseResponse> updateOrderStatus(
            @Field("id_transaksi") int idTransaksi,
            @Field("status") String status
    );

    @FormUrlEncoded
    @POST("admin_service.php?action=delete_order")
    Call<BaseResponse> deleteOrder(
            @Field("id_transaksi") int idTransaksi
    );

    @GET("admin_service.php?action=get_customers")
    Call<com.bagus.toko_baju_uas.model.UsersResponse> getAdminCustomers();

    @GET("admin_service.php?action=get_history")
    Call<com.bagus.toko_baju_uas.model.OrderResponse> getUserHistory(
            @retrofit2.http.Query("id_user") String uid
    );

    @retrofit2.http.Multipart
    @POST("update_profile_pic.php")
    Call<BaseResponse> updateProfilePic(
            @retrofit2.http.Part("uid_firebase") okhttp3.RequestBody uid,
            @retrofit2.http.Part okhttp3.MultipartBody.Part image
    );
}