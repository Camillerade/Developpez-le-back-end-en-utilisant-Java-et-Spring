package com.Location.API.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Location.API.model.User;
import com.Location.API.DTO.RegisterRequest; // Assure-toi que tu importes le bon DTO

import com.Location.API.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Méthode pour vérifier si l'utilisateur existe déjà
    public boolean userExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    // Méthode pour l'enregistrement d'un utilisateur
    public User register(RegisterRequest registerRequest) {
        System.out.println("Enregistrement de l'utilisateur : " + registerRequest.getEmail());

        if (userExists(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Créer un nouvel utilisateur
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName()); // Assurez-vous que le nom correspond
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        try {
            // Enregistrement de l'utilisateur dans la base de données
            User savedUser = userRepository.save(user);
            System.out.println("Utilisateur enregistré avec succès : " + savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("constraint [unique_email]")) {
                throw new IllegalArgumentException("Email is already in use", e);
            }
            throw new RuntimeException("Error saving user to the database", e);
        }
    }

    // Méthode qui sera appelée dans ton contrôleur
    public void registerUser(RegisterRequest registerRequest) {
        // Appeler la méthode register pour enregistrer l'utilisateur
        register(registerRequest);
    }
 

 

   
	
}
