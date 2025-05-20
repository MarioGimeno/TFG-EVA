package com.example.appGrabacion.models;

public class RegisterRequest {
    public String email;
    public String password;
    public RegisterRequest(String email, String password, String s) {
        this.email = email;
        this.password = password;
    }
}
