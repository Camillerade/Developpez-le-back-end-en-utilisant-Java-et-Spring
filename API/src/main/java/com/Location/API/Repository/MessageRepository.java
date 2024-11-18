package com.Location.API.Repository;

import com.Location.API.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Cette méthode permet de récupérer tous les messages associés à une location spécifique
    List<Message> findByRentalId(Long rentalId);

    // Cette méthode permet de récupérer tous les messages d'un utilisateur spécifique (optionnel)
    List<Message> findByUserId(Long userId);

    // Cette méthode permet de récupérer un message en particulier à partir de son ID et de l'ID de la location
    Message findByIdAndRentalId(Long id, Long rentalId);
}
