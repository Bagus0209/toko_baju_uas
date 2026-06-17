package com.bagus.toko_baju_uas.model;

import java.util.List;

public class OrderResponse {
    private boolean status;
    private List<OrderModel> data;

    public boolean isStatus() { return status; }
    public List<OrderModel> getData() { return data; }
}