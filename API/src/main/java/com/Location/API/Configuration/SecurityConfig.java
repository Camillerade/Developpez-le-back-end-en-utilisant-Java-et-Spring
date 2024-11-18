package com.Location.API.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.Location.API.Service.MyUserDetailsService;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;
    private final JwtProvider jwtProvider;

    public SecurityConfig(MyUserDetailsService myUserDetailsService, JwtProvider jwtProvider) {
        this.myUserDetailsService = myUserDetailsService;
        this.jwtProvider = jwtProvider;
    }

    // Déclaration du PasswordEncoder
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Configuration du SecurityFilterChain
    @Bean
     SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http
        .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (be cautious in production)
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configure CORS
        .authorizeHttpRequests(auth -> auth
        	    .requestMatchers("/api/auth/**", "/error").permitAll() // Autoriser sans authentification
        	    .requestMatchers("/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()  // Accès libre aux ressources statiques
        	    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // Permet l'accès à Swagger

        	    .requestMatchers("/api/messages/").permitAll() // Autorise l'accès aux images sans authentification
        	    .requestMatchers("/api/auth/me").authenticated() // Nécessite une authentification pour /me
        	    .requestMatchers("/api/rentals/**").authenticated() // Nécessite une authentification pour /rentals
                .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, myUserDetailsService), 
                         UsernamePasswordAuthenticationFilter.class) // JWT Filter
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
        );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Frontend Angular
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }


    // Expose AuthenticationManager comme un bean
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        // Utilisation du service de détails utilisateur et du password encoder pour la construction de AuthenticationManager
        authenticationManagerBuilder
            .userDetailsService(myUserDetailsService)
            .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }
}
