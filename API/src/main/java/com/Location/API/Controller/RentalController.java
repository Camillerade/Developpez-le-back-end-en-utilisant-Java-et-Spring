package com.Location.API.Controller;

import com.Location.API.DTO.RentalDTO;
import com.Location.API.Service.MyUserDetails;
import com.Location.API.Service.RentalService;
import com.Location.API.model.Rental;
import com.Location.API.model.User;
import com.Location.API.DTO.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController // Spécifie que cette classe est un contrôleur REST.
@RequestMapping("/api/rentals") // Déclare le chemin de base pour l'accès aux locations (par exemple, http://localhost:8080/api/rentals)
@CrossOrigin(origins = "http://localhost:4200") // Permet d'accepter les requêtes CORS provenant de http://localhost:4200 (Frontend Angular)
public class RentalController {

    private final RentalService rentalService; // Service pour gérer les locations.

    @Value("${file.upload-dir}") // Charge le répertoire de téléchargement des fichiers à partir du fichier de configuration.
    private String uploadDir;

    // Constructeur d'injection de dépendances pour le service de location.
    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /**
     * Récupère la liste de toutes les locations disponibles.
     */
    @Operation(summary = "Récupère la liste de toutes les locations", description = "Renvoie la liste des locations disponibles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des locations récupérée avec succès"),  // Réponse de succès (200 OK).
        @ApiResponse(responseCode = "500", description = "Erreur serveur interne")  // Réponse en cas d'erreur serveur (500).
    })
    @GetMapping // Annotation pour une requête HTTP GET.
    public ResponseEntity<ResponseWrapper<RentalDTO>> getAllRentals() {
        try {
            // Récupère toutes les locations sous forme de DTOs.
            List<RentalDTO> rentals = rentalService.getAllRentals();
            ResponseWrapper<RentalDTO> response = new ResponseWrapper<>(rentals);  // Emballe les données dans un wrapper générique.
            return ResponseEntity.ok(response); // Retourne la liste avec un code HTTP 200.
        } catch (Exception e) {
            return ResponseEntity.status(500).build();  // En cas d'erreur, retourne une erreur serveur (500).
        }
    }

    /**
     * Récupère une location spécifique par son identifiant.
     */
    @Operation(summary = "Récupère une location par son identifiant", description = "Renvoie les détails d'une location spécifique.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location récupérée avec succès"),  // Réponse de succès (200 OK).
        @ApiResponse(responseCode = "404", description = "Location non trouvée")  // Réponse en cas de location non trouvée (404).
    })
    @GetMapping("/{id}")  // Paramètre dynamique {id} dans le chemin de l'URL.
    public ResponseEntity<RentalDTO> getRentalById(
            @Parameter(description = "ID de la location", required = true) @PathVariable Long id) {  // Paramètre dynamique passé via l'URL.
        RentalDTO rentalDTO = rentalService.getRentalById(id);  // Récupère la location par ID.
        if (rentalDTO != null) {
            return ResponseEntity.ok(rentalDTO);  // Si la location est trouvée, retourne un code 200 avec la location.
        } else {
            return ResponseEntity.status(404).build();  // Sinon, retourne un code 404 (Non trouvé).
        }
    }

    /**
     * Crée une nouvelle location avec les informations données.
     */
    @Operation(summary = "Crée une nouvelle location", description = "Permet de créer une nouvelle location avec les détails fournis.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location créée avec succès"),  // Réponse de succès (201 Created).
        @ApiResponse(responseCode = "500", description = "Erreur serveur interne")  // Réponse en cas d'erreur serveur (500).
    })
    @PostMapping  // Annotation pour une requête HTTP POST.
    public ResponseEntity<Rental> createRental(
            @RequestParam String name,  // Paramètres de la location, reçus via la requête HTTP.
            @RequestParam BigDecimal surface,
            @RequestParam BigDecimal price,
            @RequestParam String description,
            @RequestParam MultipartFile picture,  // Paramètre pour l'image téléchargée.
            @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur actuellement authentifié.
        try {
            // Sauvegarde l'image téléchargée et récupère son nom.
            String pictureName = rentalService.saveImage(picture);
            Long ownerId = null;
            // Si un utilisateur est authentifié, récupère son ID pour l'assigner comme propriétaire de la location.
            if (userDetails != null && userDetails instanceof MyUserDetails) {
                MyUserDetails myUserDetails = (MyUserDetails) userDetails;
                User user = myUserDetails.getUser();
                ownerId = user.getId();
            }
            Rental rental = new Rental();  // Crée une nouvelle instance de location.
            rental.setName(name);
            rental.setSurface(surface);
            rental.setPrice(price);
            rental.setDescription(description);
            rental.setPicture(pictureName);  // Attribue le nom du fichier image à la location.
            rental.setCreatedAt(new Date());  // Définit la date de création.
            rental.setUpdatedAt(new Date());  // Définit la date de mise à jour.
            rental.setOwnerId(ownerId);  // Attribue l'ID du propriétaire (utilisateur connecté).

            // Enregistre la location dans la base de données via le service.
            Rental savedRental = rentalService.createRental(rental);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRental);  // Retourne un code 201 avec la location créée.
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // Retourne une erreur 500 en cas d'exception.
        }
    }

    /**
     * Met à jour une location existante par son identifiant.
     */
    @Operation(summary = "Met à jour une location existante", description = "Met à jour les détails d'une location par son ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location mise à jour avec succès"),  // Réponse de succès (200 OK).
        @ApiResponse(responseCode = "404", description = "Location non trouvée")  // Réponse en cas de location non trouvée (404).
    })
    @PutMapping("/{id}")  // Paramètre dynamique {id} pour l'ID de la location.
    public ResponseEntity<Rental> updateRental(
            @PathVariable Long id,  // Récupère l'ID de la location à partir de l'URL.
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal surface,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile picture,  // Paramètres optionnels pour la mise à jour.
            @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur connecté.
        try {
            // Crée un DTO avec les nouvelles données reçues.
            RentalDTO updatedData = new RentalDTO();
            updatedData.setName(name);
            updatedData.setSurface(surface);
            updatedData.setPrice(price);
            updatedData.setDescription(description);
            // Appelle le service pour mettre à jour la location avec les nouvelles données.
            Rental updatedRental = rentalService.updateRental(id, updatedData, picture);

            if (updatedRental != null) {
                return ResponseEntity.ok(updatedRental);  // Si la mise à jour est réussie, retourne un code 200 avec la location mise à jour.
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // Si la location n'existe pas, retourne une erreur 404.
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // En cas d'exception, retourne une erreur 500.
        }
    }
}
