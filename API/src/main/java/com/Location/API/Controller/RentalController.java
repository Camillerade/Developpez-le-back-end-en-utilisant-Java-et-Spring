package com.Location.API.Controller;

import com.Location.API.DTO.RentalDTO;
import com.Location.API.Service.MyUserDetails;
import com.Location.API.Service.RentalService;
import com.Location.API.model.Rental;
import com.Location.API.model.User;
import com.Location.API.DTO.ResponseWrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "http://localhost:4200")
public class RentalController {

    private final RentalService rentalService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /**
     * Récupère la liste de toutes les locations disponibles.
     */
    @Operation(summary = "Get all rentals", description = "Fetches a list of all available rentals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched all rentals",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentalDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<ResponseWrapper<RentalDTO>> getAllRentals() {
        try {
            List<RentalDTO> rentals = rentalService.getAllRentals();
            ResponseWrapper<RentalDTO> response = new ResponseWrapper<>(rentals);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Récupère une location spécifique par son identifiant avec le nom de l'image associée.
     */
    @Operation(summary = "Get rental by ID", description = "Fetches a specific rental by its ID along with the associated image name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched the rental by ID",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentalDTO.class))),
        @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RentalDTO> getRentalById(@PathVariable Long id) {
        RentalDTO rentalDTO = rentalService.getRentalById(id);

        // Vérifie si la location est trouvée
        if (rentalDTO != null) {
            // Récupère le nom de l'image associé à la location et ajoute l'URL complète
            String pictureName = rentalDTO.getPicture();
            
            // Si une image est présente, ajoute l'URL complète de l'image
            if (pictureName != null && !pictureName.isEmpty()) {
                rentalDTO.setPicture("http://localhost:8080/images/" + pictureName);
            }
            
            // Affiche l'URL pour déboguer si nécessaire
            System.out.println("URL de l'image : http://localhost:8080/images/" + pictureName);
            
            return ResponseEntity.ok(rentalDTO);
        } else {
            return ResponseEntity.status(404).build();
        }
    }


    /**
     * Crée une nouvelle location avec les informations données.
     */
    @Operation(summary = "Create a new rental", description = "Creates a new rental with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the rental"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping
    public ResponseEntity<Rental> createRental(
            @RequestParam String name,
            @RequestParam BigDecimal surface,
            @RequestParam BigDecimal price,
            @RequestParam String description,
            @RequestParam MultipartFile picture,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String pictureName = rentalService.saveImage(picture);
            Long ownerId = null;
            if (userDetails != null && userDetails instanceof MyUserDetails) {
                MyUserDetails myUserDetails = (MyUserDetails) userDetails;
                User user = myUserDetails.getUser();
                ownerId = user.getId();
            }
            Rental rental = new Rental();
            rental.setName(name);
            rental.setSurface(surface);
            rental.setPrice(price);
            rental.setDescription(description);
            rental.setPicture(pictureName);
            rental.setCreatedAt(new Date());
            rental.setUpdatedAt(new Date());
            rental.setOwnerId(ownerId);

            Rental savedRental = rentalService.createRental(rental);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRental);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Met à jour une location existante par son identifiant.
     */
    @Operation(summary = "Update rental", description = "Updates an existing rental by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the rental"),
        @ApiResponse(responseCode = "404", description = "Rental not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Rental> updateRental(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal surface,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile picture,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            RentalDTO updatedData = new RentalDTO();
            updatedData.setName(name);
            updatedData.setSurface(surface);
            updatedData.setPrice(price);
            updatedData.setDescription(description);

            Rental updatedRental = rentalService.updateRental(id, updatedData, picture);

            if (updatedRental != null) {
                return ResponseEntity.ok(updatedRental);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
