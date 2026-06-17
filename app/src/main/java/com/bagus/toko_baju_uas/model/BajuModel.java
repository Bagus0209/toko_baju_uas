package com.bagus.toko_baju_uas.model;

import com.bagus.toko_baju_uas.api.ApiClient;

public class BajuModel {
    private int id_barang;
    private String nama_barang;
    private int harga;
    private int stok;
    private String gambar;

    public int getIdBarang() { return id_barang; }
    public String getNamaBarang() { return nama_barang; }
    public int getHarga() { return harga; }
    public int getStok() { return stok; }
    
    public String getGambar() {
        if (gambar != null && !gambar.trim().isEmpty() && !gambar.startsWith("http")) {
            // Trim() untuk menghapus spasi yang tidak sengaja terketik di database
            return "http://" + ApiClient.IP_LAPTOP + "/api_tokobaju/images/" + gambar.trim();
        }
        return gambar;
    }

    public String getGambarRaw() {
        return (gambar != null) ? gambar.trim() : "";
    }
}