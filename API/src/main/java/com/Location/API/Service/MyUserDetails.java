package com.Location.API.Service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import com.Location.API.model.User;

import java.io.Serializable;
import java.util.Collection;

public class MyUserDetails implements UserDetails, Serializable {

    private final User user;
    private static final long serialVersionUID = 1L;

    // Constructeur pour initialiser l'utilisateur
    public MyUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;  // Retourner l'objet User associé
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // Utiliser l'email comme username
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // Retourner le mot de passe de l'utilisateur
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourner les rôles de l'utilisateur ici si nécessaire (par exemple avec `GrantedAuthority`)
        return null;  // Aucun rôle ici pour l'exemple, mais tu peux le personnaliser
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Si l'utilisateur n'est pas expiré
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Si le compte n'est pas verrouillé
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Si les informations d'identification ne sont pas expirées
    }

    @Override
    public boolean isEnabled() {
        return true;  // Si l'utilisateur est activé
    }
}
