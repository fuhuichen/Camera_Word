package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @Size(max = 100)
    @Column(name = "display_name")
    private String displayName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @ManyToMany
    @JoinTable(
        name = "user_platforms",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "platform_code")
    )
    private Set<Platform> authorizedPlatforms = new HashSet<>();
    
    public User() {}
    
    public User(String email, String displayName, UserRole role) {
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }
    
    // Getters and setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Set<Platform> getAuthorizedPlatforms() {
        return authorizedPlatforms;
    }
    
    public void setAuthorizedPlatforms(Set<Platform> authorizedPlatforms) {
        this.authorizedPlatforms = authorizedPlatforms;
    }
    
    public enum UserRole {
        MAIN_ADMIN, PLATFORM_ADMIN
    }
}
