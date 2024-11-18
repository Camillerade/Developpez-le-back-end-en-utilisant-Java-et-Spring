package com.Location.API.Controller;

import com.Location.API.DTO.AuthFailure;
import com.Location.API.DTO.AuthSuccess;
import com.Location.API.DTO.LoginRequest;
import com.Location.API.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login user", description = "Authenticates the user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthSuccess.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthFailure.class)))
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String jwt = authService.authenticate(loginRequest);
        
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthFailure("Invalid username or password"));
        }

        AuthSuccess authSuccess = new AuthSuccess(jwt);
        return ResponseEntity.ok(authSuccess);
    }
}
