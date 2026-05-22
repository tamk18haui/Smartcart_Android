package com.gr6.smartcart_android.auth.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("id")
    private Long id;

    @SerializedName("token")
    private String token;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("jwt")
    private String jwt;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("role")
    private String role;

    @SerializedName("user")
    private UserDto user;

    public LoginResponse() {
    }

    public String getToken() {
        if (token != null && !token.trim().isEmpty()) return token;
        if (accessToken != null && !accessToken.trim().isEmpty()) return accessToken;
        if (jwt != null && !jwt.trim().isEmpty()) return jwt;
        return null;
    }

    public String getRole() {
        if (role != null && !role.trim().isEmpty()) return role;
        if (user != null) return user.getRole();
        return null;
    }

    public UserDto getUser() {
        if (user != null) {
            return user;
        }

        UserDto dto = new UserDto();
        dto.userId = userId;
        dto.id = id;
        dto.fullName = fullName;
        dto.name = name;
        dto.email = email;
        dto.avatarUrl = avatarUrl;
        dto.role = role;

        return dto;
    }

    public static class UserDto {

        @SerializedName("userId")
        private Long userId;

        @SerializedName("id")
        private Long id;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        @SerializedName("role")
        private String role;

        public Long getUserId() {
            if (userId != null) return userId;
            return id;
        }

        public String getFullName() {
            if (fullName != null && !fullName.trim().isEmpty()) return fullName;
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getRole() {
            return role;
        }
    }
}