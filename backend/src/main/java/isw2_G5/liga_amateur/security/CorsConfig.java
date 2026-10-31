package isw2_G5.liga_amateur.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Habilita CORS para la app web de desarrollo (Expo) sin abrir el backend a cualquier origen.
 *
 * <p>Por defecto solo acepta orígenes locales y de la red LAN, que es donde corre Expo durante la
 * cursada y la demostración. Los orígenes se pueden ampliar con la propiedad
 * {@code liga.cors.allowed-origin-patterns}.</p>
 */
@Configuration
public class CorsConfig {

    private final List<String> origenesPermitidos;

    public CorsConfig(
            @Value("${liga.cors.allowed-origin-patterns:http://localhost:[*],http://127.0.0.1:[*],http://192.168.*.*:[*]}")
            String origenesPermitidos) {
        this.origenesPermitidos = Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();
    }

    /**
     * Configura CORS únicamente para las rutas de la API.
     *
     * @return fuente de configuración usada por Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(origenesPermitidos);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}
