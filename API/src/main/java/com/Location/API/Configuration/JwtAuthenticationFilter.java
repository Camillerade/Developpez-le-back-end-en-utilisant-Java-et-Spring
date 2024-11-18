package com.Location.API.Configuration;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.Location.API.Service.MyUserDetailsService;


import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MyUserDetailsService myUserDetailsService;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, MyUserDetailsService myUserDetailsService) {
        this.jwtProvider = jwtProvider;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extraire le token du header Authorization
        String token = extractToken(request);

        // Vérification si le token est valide
        if (token != null && jwtProvider.validateToken(token)) {
            try {
                // Extraire le nom d'utilisateur du token JWT
                String username = jwtProvider.getUsernameFromToken(token);
                // Charger les détails de l'utilisateur en utilisant le service UserDetailsService
                UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                if (userDetails == null) {
                  
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    return;
                }
                // Créer un objet AuthenticationToken
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                // En cas d'erreur, loguer l'erreur et renvoyer un code 401 Unauthorized
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                return;
            }
        }

        // Passer la requête et la réponse à la chaîne de filtres suivante
        filterChain.doFilter(request, response);
    }

    // Méthode pour extraire le token de l'en-tête Authorization
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Retire "Bearer " du début du token
        }
        return null;
    }
}