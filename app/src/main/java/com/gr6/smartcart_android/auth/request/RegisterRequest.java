package com.gr6.smartcart_android.auth.request;

public class RegisterRequest {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;

    public RegisterRequest() {
    }

    public RegisterRequest(
            String fullName,
            String email,
            String phoneNumber,
            String password,
            String confirmPassword
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}