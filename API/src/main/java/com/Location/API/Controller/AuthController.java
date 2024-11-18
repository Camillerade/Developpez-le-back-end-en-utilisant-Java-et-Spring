package com.Location.API.Controller;

import com.Location.API.DTO.AuthFailure;
import com.Location.API.DTO.AuthSuccess;
import com.Location.API.DTO.LoginRequest;
import com.Location.API.DTO.RegisterRequest;
import com.Location.API.DTO.UserDTO;
import com.Location.API.Repository.UserRepository;
import com.Location.API.Service.AuthService;
import com.Location.API.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // Injection des dépendances par le constructeur
    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // Méthode de connexion
    @Operation(summary = "Login user", description = "Authenticates the user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthSuccess.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthFailure.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthFailure.class)))
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authentifier l'utilisateur et générer le JWT
            String jwt = authService.authenticate(loginRequest);
            
            if (jwt == null) {
                // Retourner une réponse d'échec avec un message clair
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthFailure("Échec de l'authentification, utilisateur ou mot de passe incorrect"));
            }

            // Créer l'objet de réponse avec le JWT
            AuthSuccess authSuccess = new AuthSuccess(jwt);
            return ResponseEntity.ok(authSuccess);
            
        } catch (Exception e) {
            // Retourner une réponse serveur en cas d'exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthFailure("Erreur serveur lors de l'authentification"));
        }
    }

    @Operation(summary = "Register a new user", description = "Registers a new user and returns a success message")
    @ApiResponse(responseCode = "200", description = "User registered successfully", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthSuccess.class)))
    @ApiResponse(responseCode = "400", description = "Bad request, invalid data", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthFailure.class)))
    @PostMapping("/register")
    public ResponseEntity<AuthSuccess> register(@RequestBody RegisterRequest registerRequest) {
        // Appelle le service d'enregistrement
        AuthSuccess response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    // Endpoint pour récupérer l'utilisateur authentifié
    @Operation(summary = "Get authenticated user", description = "Returns the details of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Authenticated user details", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized, user not authenticated", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthFailure.class)))
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Utilisateur non authentifié
        }

        // Récupérer l'utilisateur à partir du repository avec l'email
        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Utilisateur non trouvé
        }

        // Convertir les LocalDateTime en Date
        Date createdAt = Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        Date updatedAt = Date.from(user.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());

        // Créer un UserDTO avec les dates converties
        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), createdAt, updatedAt);

        return ResponseEntity.ok(userDTO); // Retourner l'UserDTO avec dates converties
    }
}
