package com.Location.API.Repository;

import com.Location.API.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Pas besoin de définir findAll(), car JpaRepository le fournit par défaut.
}
