package com.bagus.toko_baju_uas.model;

import java.util.List;

public class BarangResponse {
    private boolean status;
    private String message;
    private List<BajuModel> data; // List digunakan karena barangnya banyak

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<BajuModel> getData() { return data; }
}