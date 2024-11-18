package com.Location.API.Service;

import com.Location.API.DTO.AuthSuccess;
import com.Location.API.DTO.LoginRequest;
import com.Location.API.DTO.RegisterRequest;
import com.Location.API.model.User;
import com.Location.API.Repository.UserRepository;
import com.Location.API.Configuration.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Service
public class AuthService {

		@Autowired
		private UserRepository userRepository;

	  @Autowired
	    private AuthenticationManager authenticationManager;

	    @Autowired
	    private JwtProvider JwtProvider;

	    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    
    public String authenticate(LoginRequest loginRequest) {
        try {
            // Chercher l'utilisateur par email
            User user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                throw new BadCredentialsException("User not found");
            }

            // Vérification du mot de passe
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Incorrect password");
            }

            // Authentification réussie
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
            );

            // Récupérer les détails de l'utilisateur et générer un token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = JwtProvider.generateJwtToken(userDetails.getUsername());
           
            return token;

        } catch (BadCredentialsException e) {
            // Retourner null si les identifiants sont incorrects
            return null;
        }
    }



    // Méthode pour enregistrer un utilisateur
    public AuthSuccess register(RegisterRequest registerRequest) {
        // Hachage du mot de passe
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Création de l'utilisateur
        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setName(registerRequest.getName());

        // Enregistrement de l'utilisateur dans la base de données
        userRepository.save(newUser);

        // Retourne la réponse avec un message de succès
        return new AuthSuccess("User registered successfully");
    }
}
