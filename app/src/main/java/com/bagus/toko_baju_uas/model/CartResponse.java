package com.bagus.toko_baju_uas.model;

import java.util.List;

public class CartResponse {
    private boolean status;
    private String message;
    private List<CartModel> data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<CartModel> getData() { return data; }
}