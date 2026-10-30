package isw2_G5.liga_amateur.security;

import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Delimita las consultas públicas y las operaciones por rol conforme al contrato de la API.
 * El filtro Bearer verifica la firma y el usuario actual antes de autorizar cada operación.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Configura sesiones sin estado y autorización por método y ruta; niega rutas no acordadas.
     *
     * @param http configuración HTTP de Spring Security
     * @param errorWriter escritor común de errores 401/403
     * @param jwt servicio de generación y validación de tokens
     * @param users repositorio para comprobar actividad y rol actuales
     * @return cadena de filtros de seguridad
     * @throws Exception si Spring Security no puede construir la cadena
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorResponseWriter errorWriter,
            JwtService jwt, UsuarioRepository users) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, exception) ->
                errorWriter.write(request, response, HttpStatus.UNAUTHORIZED);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwt, users, unauthorized);
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((request, response, exception) ->
                                errorWriter.write(request, response, HttpStatus.FORBIDDEN)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorization -> authorization
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/torneos", "/api/canchas", "/api/arbitros",
                                "/api/torneos/*/equipos", "/api/equipos/*/jugadores",
                                "/api/torneos/*/partidos", "/api/partidos/*",
                                "/api/torneos/*/tabla", "/api/torneos/*/suspendidos").permitAll()
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/torneos/*/equipos",
                                "/api/equipos/*/jugadores", "/api/partidos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipos/*", "/api/jugadores/*",
                                "/api/partidos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipos/*", "/api/jugadores/*",
                                "/api/partidos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/partidos/*/estado")
                                .hasAnyRole("ADMIN", "DELEGADO")
                        .requestMatchers(HttpMethod.POST, "/api/partidos/*/eventos")
                                .hasAnyRole("ADMIN", "DELEGADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/partidos/*/eventos/*")
                                .hasAnyRole("ADMIN", "DELEGADO")
                        .anyRequest().denyAll());
        return http.build();
    }
}
