package com.bagus.toko_baju_uas.model;

public class LoginResponse {
    private boolean status;
    private String message;
    private UserModel data;

    // Ini adalah bagian Getter yang tadi hilang/tidak terbaca
    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserModel getData() {
        return data;
    }
}