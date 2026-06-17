package com.bagus.toko_baju_uas.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    private int id_user;
    
    @SerializedName("nama")
    private String username; // Tetap gunakan variabel username agar tidak merusak kode lain, tapi map ke "nama" dari JSON

    private String role;

    // Getter untuk mengambil nilai
    public int getIdUser() { return id_user; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}