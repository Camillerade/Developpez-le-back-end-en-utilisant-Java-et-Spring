package com.Location.API.exception;

@SuppressWarnings("serial")
public class MessageNotFoundException extends RuntimeException {
    
    public MessageNotFoundException(Integer id) {
        super("Message non trouvé avec l'ID: " + id);
    }
}
