package com.example.digitaltwin.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity @Table(name="users")
public class UserAccount implements UserDetails {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String email;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    @Column(nullable=false) private boolean active=true;
    @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
    protected UserAccount() {}
    public UserAccount(String email, String passwordHash, Role role) { this.email=email; this.passwordHash=passwordHash; this.role=role; }
    @PreUpdate void touch() { updatedAt=LocalDateTime.now(); }
    public Long getId(){return id;} public String getEmail(){return email;} public Role getRole(){return role;}
    @Override public String getUsername(){return email;} @Override public String getPassword(){return passwordHash;}
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));}
    @Override public boolean isAccountNonExpired(){return true;} @Override public boolean isAccountNonLocked(){return active;}
    @Override public boolean isCredentialsNonExpired(){return true;} @Override public boolean isEnabled(){return active;}
}
