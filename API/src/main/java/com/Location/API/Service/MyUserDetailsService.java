package com.Location.API.Service;

import com.Location.API.model.User;
import com.Location.API.Repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructeur pour injecter le UserRepository
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Récupérer l'utilisateur de la base de données en utilisant l'email (username)
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Retourner un objet MyUserDetails avec l'utilisateur récupéré
        return new MyUserDetails(user);  // Retourne un MyUserDetails personnalisé
    }

    // Méthode pour charger un utilisateur par son ID
    public UserDetails loadUserById(long id) throws UsernameNotFoundException {
        // Récupérer l'utilisateur par ID depuis la base de données
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        // Retourner un MyUserDetails personnalisé
        return new MyUserDetails(user);
    }
}
