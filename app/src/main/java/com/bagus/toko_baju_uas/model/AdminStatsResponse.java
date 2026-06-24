package com.bagus.toko_baju_uas.model;

import com.google.gson.annotations.SerializedName;

public class AdminStatsResponse {
    private boolean status;
    private Data data;

    public static class Data {
        @SerializedName(value = "total_products", alternate = {"total_produk", "products_count"})
        public int total_products;
        
        @SerializedName(value = "total_orders", alternate = {"total_pesanan", "orders_count"})
        public int total_orders;
        
        @SerializedName(value = "total_users", alternate = {"total_customers", "total_pengunjung", "users_count"})
        public int total_users;
        
        @SerializedName(value = "total_revenue", alternate = {"total_pendapatan", "revenue"})
        public long total_revenue;
    }

    public boolean isStatus() { return status; }
    public Data getData() { return data; }
}
