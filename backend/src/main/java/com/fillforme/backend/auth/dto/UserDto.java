package com.fillforme.backend.auth.dto;

import java.util.UUID;

public class UserDto {
    private UUID id;
    private String email;
    private String fullName;
    private String role;

    public UserDto() {}

    public UserDto(UUID id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static class UserDtoBuilder {
        private UUID id;
        private String email;
        private String fullName;
        private String role;

        UserDtoBuilder() {}

        public UserDtoBuilder id(UUID id) { this.id = id; return this; }
        public UserDtoBuilder email(String email) { this.email = email; return this; }
        public UserDtoBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserDtoBuilder role(String role) { this.role = role; return this; }

        public UserDto build() {
            return new UserDto(id, email, fullName, role);
        }
    }
}
