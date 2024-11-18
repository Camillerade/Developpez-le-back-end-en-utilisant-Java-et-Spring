package com.Location.API.Service;

import com.Location.API.DTO.MessageDTO;
import com.Location.API.DTO.RentalDTO;
import com.Location.API.model.Message;
import com.Location.API.model.Rental;
import com.Location.API.model.User;
import com.Location.API.Repository.MessageRepository;
import com.Location.API.Repository.RentalRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service  // Déclare cette classe comme un service Spring, qui est une classe métier.
public class RentalService {
    @Autowired
    private MessageRepository messageRepository;
    private final RentalRepository rentalRepository;  // Référence au repository pour interagir avec la base de données.

    @Value("${file.upload-dir}")  // Injection de la valeur du répertoire pour sauvegarder les images téléchargées.
    private String uploadDir;

    // Constructeur pour l'injection de dépendance de RentalRepository.
    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    /**
     * Récupère toutes les locations.
     * @return une liste de RentalDTO avec les informations des locations.
     */
    public List<RentalDTO> getAllRentals() {
        List<Rental> rentals = rentalRepository.findAll();  // Récupère toutes les locations depuis la base de données.
        
        // Convertit chaque objet Rental en un RentalDTO et ajoute l'URL complète pour l'image.
        return rentals.stream()
                .map(rental -> {
                    RentalDTO dto = new RentalDTO(rental);
                    dto.setPicture("http://localhost:8080/images/" + dto.getPicture());  // Ajoute l'URL complète de l'image.
                    return dto;
                }).toList();  // Retourne la liste des locations sous forme de DTO.
    }

    /**
     * Récupère une location par son ID.
     * @param id l'ID de la location.
     * @return un objet RentalDTO ou null si la location n'existe pas.
     */
    public RentalDTO getRentalById(Long id) {
        return rentalRepository.findById(id)  // Recherche la location dans la base par son ID.
                .map(RentalDTO::new)  // Si trouvée, transforme la location en DTO.
                .orElse(null);  // Si non trouvée, renvoie null.
    }

    /**
     * Met à jour une location existante.
     * @param id l'ID de la location à mettre à jour.
     * @param updatedData les nouvelles données pour la location.
     * @param file un fichier image facultatif à mettre à jour.
     * @return la location mise à jour, ou null si la location n'est pas trouvée.
     */
    public Rental updateRental(Long id, RentalDTO updatedData, MultipartFile file) throws IOException {
        // Cherche la location existante par ID.
        Optional<Rental> existingRentalOpt = rentalRepository.findById(id);
        
        // Si la location n'est pas trouvée, retourne null.
        if (existingRentalOpt.isEmpty()) {
            return null;
        }

        Rental rental = existingRentalOpt.get();  // Récupère la location existante.

        // Mise à jour des champs si de nouvelles données sont fournies.
        if (updatedData.getName() != null) rental.setName(updatedData.getName());
        if (updatedData.getSurface() != null) rental.setSurface(updatedData.getSurface());
        if (updatedData.getPrice() != null) rental.setPrice(updatedData.getPrice());
        if (updatedData.getDescription() != null) rental.setDescription(updatedData.getDescription());

        // Si un nouveau fichier image est fourni, sauvegarde l'image et met à jour le nom de l'image.
        if (file != null && !file.isEmpty()) {
            String fileName = saveImage(file);  // Sauvegarde l'image et récupère son nom.
            rental.setPicture(fileName);  // Met à jour le nom de l'image dans l'objet Rental.
        }

        rental.setUpdatedAt(new Date());  // Met à jour la date de mise à jour.

        // Sauvegarde la location mise à jour dans la base de données.
        return rentalRepository.save(rental);  // Renvoie l'objet Rental mis à jour.
    }

    /**
     * Supprime une location par son ID.
     * @param id l'ID de la location à supprimer.
     */
    public void deleteRental(Long id) {
        rentalRepository.deleteById(id);  // Supprime la location de la base de données.
    }

    /**
     * Crée une nouvelle location.
     * @param pictureName le nom du fichier image associé à la location.
     * @param name le nom de la location.
     * @param surface la surface de la location.
     * @param price le prix de la location.
     * @param description la description de la location.
     * @param ownerId l'ID du propriétaire de la location.
     * @param createdAt la date de création de la location.
     * @param updatedAt la date de mise à jour de la location.
     * @return la location sauvegardée.
     */
    public Rental saveRental(String pictureName, String name, BigDecimal surface, BigDecimal price,
                             String description, Long ownerId, Date createdAt, Date updatedAt) {

        // Crée un objet Rental avec les données fournies.
        Rental rental = new Rental(
            null,  // L'ID est généré automatiquement par la base de données (pas besoin de le fournir).
            name,
            surface,
            price,
            pictureName,  // Nom de l'image.
            description,
            ownerId,  // ID du propriétaire.
            createdAt,  // Date de création.
            updatedAt  // Date de mise à jour.
        );

        // Sauvegarde la location dans la base de données.
        return rentalRepository.save(rental);  // Renvoie la location enregistrée.
    }

    /**
     * Crée une location dans la base de données.
     * @param rental l'objet Rental à sauvegarder.
     * @return la location sauvegardée.
     */
    public Rental createRental(Rental rental) {
        return rentalRepository.save(rental);  // Enregistre la location dans la base de données.
    }

    /**
     * Sauvegarde une image sur le serveur et renvoie son nom.
     * @param file le fichier image à sauvegarder.
     * @return le nom du fichier image.
     */
    public String saveImage(MultipartFile file) throws IOException {
        // Récupère le nom du fichier original.
        String filename = file.getOriginalFilename();
        
        // Spécifie le répertoire où les fichiers seront sauvegardés.
        Path uploadPath = Paths.get(uploadDir);

        // Si le répertoire n'existe pas, crée-le.
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Spécifie le chemin complet du fichier.
        Path filePath = uploadPath.resolve(filename);

        // Sauvegarde le fichier sur le serveur.
        file.transferTo(filePath);

        // Retourne le nom du fichier pour l'enregistrer dans la base de données.
        return filename;
    }
    
    
    
    
    public MessageDTO createMessage(Long rentalId, MessageDTO messageDTO) {
        // Vérifier si la location existe
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location not found for id: " + rentalId));

        // Récupérer l'utilisateur authentifié à partir du contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        // Vérifier si l'utilisateur est authentifié (c'est un objet UserDetails)
        if (principal instanceof User) {
            // Si l'objet principal est une instance de User, récupérer l'utilisateur
            User userDetails = (User) principal;

           

            // Créer l'objet Message à partir du MessageDTO
            Message message = new Message();
            message.setRental(rental);  // Associer la location
            message.setUser(userDetails);      // Associer l'utilisateur (ici on utilise `userDetails`)
            message.setMessage(messageDTO.getMessage()); // Contenu du message
            message.setCreatedAt(LocalDateTime.now()); // Date de création
            message.setUpdatedAt(LocalDateTime.now()); // Date de mise à jour

            // Sauvegarder le message dans la base de données
            Message savedMessage = messageRepository.save(message);

            // Convertir le message en MessageDTO avant de le renvoyer
            return new MessageDTO(
                    savedMessage.getId(),
                    savedMessage.getRental().getId(),
                    savedMessage.getUser().getId(),
                    savedMessage.getMessage(),
                    savedMessage.getCreatedAt(),
                    savedMessage.getUpdatedAt()
            );
        } else {
            throw new RuntimeException("User is not authenticated");
        }
    }

}
