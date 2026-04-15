package com.kaldi.dto;

public class LoginResponse {
    public String token;
    public String username;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}