package com.Location.API.Configuration;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
	
	@Value("${jwt.secret}")
	private String jwtSecret;
    private final long jwtExpirationInMs = 86400000; // 1 jour

    // Vérifier la validité du token
    public boolean isTokenValid(String token) {
        try {
           
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token);
       
            return true;  
        } catch (JwtException e) {
            
            return false;  
        }
    }    
    
    // Générer un JWT à partir des informations utilisateur
    public String generateJwtToken(String username) {
      
        String token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationInMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
       
        return token;
    }

    // Extraire le nom d'utilisateur du token JWT
    public String getUsernameFromToken(String token) {

        String username = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
   
        return username;
    }

    // Méthode pour extraire le token de l'en-tête Authorization
    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
           
            return token;
        }
     
        return null;
    }

    public boolean validateToken(String token) {
     
        try {
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token);
            
            return true;
        } catch (SignatureException e) {
          
        } catch (MalformedJwtException e) {
            
        } catch (ExpiredJwtException e) {
           
        } catch (UnsupportedJwtException e) {
         
        } catch (IllegalArgumentException e) {
  
        }
        return false;
    }

    // Extraire l'authentification à partir du token
    public Authentication getAuthentication(String token) {
       
        Claims claims = getClaims(token);
        String username = claims.getSubject();

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); 
        UserDetails userDetails = new User(username, "", authorities);

       
        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }

    // Extraire les claims du token
    private Claims getClaims(String token) {
      
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        
        return claims;
    }
}  
