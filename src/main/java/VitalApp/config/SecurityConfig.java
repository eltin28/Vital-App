package VitalApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de CORS usando corsConfigurationSource()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Deshabilita CSRF (no necesario para APIs REST con JWT)
                .csrf(csrf -> csrf.disable())

                // Configuración de autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**","/actuator/**",
                                "/api/pacientes/**",
                                "/api/medicos/**",
                                "/api/citaMedica/**").permitAll() // Endpoints públicos
                        .anyRequest().authenticated() // Todo lo demás requiere autenticación
                );


        return http.build();
    }


    /**
     * Configuración de CORS (Cross-Origin Resource Sharing).
     * Permite que el frontend pueda comunicarse con el backend
     * sin bloqueos de política de mismo origen.
     *
     * @return CorsConfigurationSource con la configuración de orígenes, métodos y headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (frontend desplegado en Render y localhost para dev)
        configuration.setAllowedOriginPatterns(List.of(
                "https://vitalcare-jt3p.onrender.com",
                "http://localhost:*"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos en la petición
        configuration.setAllowedHeaders(List.of("*"));

        // Headers expuestos en la respuesta (JWT)
        configuration.setExposedHeaders(List.of("Authorization"));

        // Permitir envío de cookies y credenciales
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}