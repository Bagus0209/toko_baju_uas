package com.bagus.toko_baju_uas.model;

import java.util.List;

public class UsersResponse {
    private boolean status;
    private List<UserModel> data;

    public boolean isStatus() { return status; }
    public List<UserModel> getData() { return data; }
}