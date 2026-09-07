package model;

import model.enums.UserRole;

/**
 * A registered user of the system - either a passenger looking up routes
 * or an admin who manages stops and routes.
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String password;
    private UserRole role;

    public User() {
    }

    public User(int userId, String fullName, String email, String password, UserRole role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
