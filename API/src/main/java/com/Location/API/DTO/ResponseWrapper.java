package com.Location.API.DTO;

import java.util.List;

public class ResponseWrapper<T> {
  


   
        private T data;  // Pour un objet unique
        private List<T> rentals;  // Pour une liste d'objets
        private String message;

        // Constructeur pour un seul objet
        public ResponseWrapper(T data) {
            this.data = data;
            this.message = null; // Message peut être null si on ne le spécifie pas
        }

        // Constructeur pour une liste d'objets
        public ResponseWrapper(List<T> rentals) {
            this.rentals = rentals;
            this.message = null; // Message peut être null si on ne le spécifie pas
        }

        // Constructeur avec message et données
        public ResponseWrapper(String message, T data) {
            this.message = message;
            this.data = data;
        }

        public ResponseWrapper(String message, List<T> rentals) {
            this.message = message;
            this.rentals = rentals;
        }

        // Getters et setters
        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public List<T> getRentals() {
            return rentals;
        }

        public void setRentals(List<T> rentals) {
            this.rentals = rentals;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


