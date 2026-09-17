package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.Role;

public class LoginResponse {

    private String token;
    private String tokenType;
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;

    public LoginResponse(
            String token,
            String tokenType,
            Long id,
            String name,
            String email,
            Role role,
            boolean active) {

        this.token = token;
        this.tokenType = tokenType;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}