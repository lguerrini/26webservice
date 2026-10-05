package com.example.webservice.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45)
    private String firstname;

    @Column(nullable = false, length = 45)
    private String lastname;

    @Column(nullable = false, length = 45)
    private String username;

    @Column(name = "password", nullable = false, length = 60)
    private String passwordHash;

    @Column(length = 45)
    private String email;

    @Column(nullable = false, length = 45)
    private String role;

    @Column(name = "t_usercol", length = 45)
    private String tUsercol;

    protected User() {
    }

    User(String firstname, String lastname, String username, String passwordHash,
            String email, String role, String tUsercol) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.tUsercol = tUsercol;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTUsercol() {
        return tUsercol;
    }

    public void setTUsercol(String tUsercol) {
        this.tUsercol = tUsercol;
    }
}