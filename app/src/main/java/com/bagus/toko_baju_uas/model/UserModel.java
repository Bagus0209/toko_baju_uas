package com.bagus.toko_baju_uas.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    private int id_user;
    
    @SerializedName("nama")
    private String username;
    
    private String email;
    private String role;

    // Getter
    public int getIdUser() { return id_user; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}