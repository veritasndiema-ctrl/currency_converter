package com.mustcc.model;

public class User {

    public enum Role { ADMIN, USER }

    private int userId;
    private String username;
    private String email;
    private String passwordHash;   // bcrypt hash — never the raw password
    private Role role;

    /** Full constructor — used when loading a user back from the DB. */
    public User(int userId, String username, String email, String passwordHash, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** Convenience constructor for callers that never touch the hash directly. */
    public User(int userId, String username, String email, Role role) {
        this(userId, username, email, null, role);
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
