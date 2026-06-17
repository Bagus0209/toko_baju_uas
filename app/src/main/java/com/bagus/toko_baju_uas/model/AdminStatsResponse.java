package com.bagus.toko_baju_uas.model;

public class AdminStatsResponse {
    private boolean status;
    private Data data;

    public class Data {
        public int total_products;
        public int total_orders;
        public int total_users;
        public int total_revenue;
    }

    public boolean isStatus() { return status; }
    public Data getData() { return data; }
}