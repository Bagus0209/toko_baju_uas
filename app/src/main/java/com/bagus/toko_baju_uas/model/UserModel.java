package com.bagus.toko_baju_uas.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    private int id_user;
    
    @SerializedName("nama")
    private String username;
    
    private String email;
    private String role;
    private int onboarding_seen_count;
    private String onboarding_last_seen;
    private int onboarding_completed;
    private int onboarding_skipped;

    // Getter
    public int getIdUser() { return id_user; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public int getOnboardingSeenCount() { return onboarding_seen_count; }
    public String getOnboardingLastSeen() { return onboarding_last_seen; }
    public boolean isOnboardingCompleted() { return onboarding_completed == 1; }
    public boolean isOnboardingSkipped() { return onboarding_skipped == 1; }
}