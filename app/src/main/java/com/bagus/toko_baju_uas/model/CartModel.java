package com.bagus.toko_baju_uas.model;

import com.bagus.toko_baju_uas.api.ApiClient;

public class CartModel {
    private int id_cart;
    private int id_barang;
    private String nama_barang;
    private int harga;
    private int jumlah;
    private String gambar;

    public int getIdCart() { return id_cart; }
    public int getIdBarang() { return id_barang; }
    public String getNamaBarang() { return nama_barang; }
    public int getHarga() { return harga; }
    public int getJumlah() { return jumlah; }
    
    public String getGambar() {
        if (gambar != null && !gambar.trim().isEmpty() && !gambar.startsWith("http")) {
            return "http://" + ApiClient.IP_LAPTOP + "/api_tokobaju/images/" + gambar.trim();
        }
        return gambar;
    }
}