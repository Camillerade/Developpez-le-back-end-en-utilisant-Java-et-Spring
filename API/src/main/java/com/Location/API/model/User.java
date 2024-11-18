package com.Location.API.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // Utilisation de LocalDateTime

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;  // Utilisation de LocalDateTime

    // Constructeur sans argument
    public User() {
        this.createdAt = LocalDateTime.now();  // Initialisation à la date et heure actuelles
        this.updatedAt = LocalDateTime.now();  // Initialisation à la date et heure actuelles
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Implémentation de UserDetails

    @Override
    public String getUsername() {
        return email;  // Utilise l'email comme nom d'utilisateur
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Le compte n'est pas expiré
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Le compte n'est pas verrouillé
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Les identifiants ne sont pas expirés
    }

    @Override
    public boolean isEnabled() {
        return true;  // L'utilisateur est activé
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Pas de rôle, mais une collection vide
    }

    // Méthode pour mettre à jour automatiquement `updatedAt` à chaque modification
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthode pour initialiser `createdAt` lors de la persistance initiale
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
