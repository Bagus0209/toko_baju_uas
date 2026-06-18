package com.bagus.toko_baju_uas.model;

import java.util.List;

public class HistoryResponse {
    private boolean status;
    private String message;
    private List<Data> data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<Data> getData() { return data; }

    public static class Data {
        public int id_transaksi;
        public int total_harga;
        public String alamat;
        public String tanggal;
        public String status;
    }
}
