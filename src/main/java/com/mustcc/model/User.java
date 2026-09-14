package com.mustcc.model;

public class User {

    public enum Role { ADMIN, USER }

    private int userId;
    private String username;
    private String email;
    private Role role;

    public User(int userId, String username, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public boolean isAdmin() { return role == Role.ADMIN; }
}
