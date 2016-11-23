package com.kyvlabs.beaconadvertiser.network.model;

public class LoginModel {
    private String auth_key;
    private String message;

    public String getAuth_key() {
        return auth_key;
    }

    public void setAuth_key(String auth_key) {
        this.auth_key = auth_key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
