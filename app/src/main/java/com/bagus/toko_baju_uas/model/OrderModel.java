package com.bagus.toko_baju_uas.model;

public class OrderModel {
    private int id_transaksi;
    private String nama; // Nama user dari JOIN
    private int total_harga;
    private String tanggal;
    private String status;

    public int getIdTransaksi() { return id_transaksi; }
    public String getNama() { return nama; }
    public int getTotalHarga() { return total_harga; }
    public String getTanggal() { return tanggal; }
    public String getStatus() { return status; }
}