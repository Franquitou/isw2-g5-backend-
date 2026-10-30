package isw2_G5.liga_amateur.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import isw2_G5.liga_amateur.users.entity.Rol;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Genera y verifica tokens HS256 de ocho horas con secreto externo al repositorio. */
@Service
public class JwtService {

    private static final Duration VIGENCIA = Duration.ofHours(8);
    private final SecretKey key;

    /**
     * Requiere un secreto suficientemente largo mediante la variable de entorno LIGA_JWT_SECRET.
     *
     * @param secret valor secreto no versionado, al menos 32 bytes UTF-8 para HS256
     */
    public JwtService(@Value("${LIGA_JWT_SECRET}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Firma un token con identidad y rol para un usuario autenticado.
     *
     * @param username identidad inmutable del usuario
     * @param rol rol del usuario
     * @return JWT firmado con vencimiento a las ocho horas
     */
    public String generate(String username, Rol rol) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(VIGENCIA)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifica firma y vencimiento; no concede acceso por sí mismo sin el filtro de seguridad.
     *
     * @param token token Bearer sin el prefijo
     * @return identidad y rol declarados en el token válido
     * @throws io.jsonwebtoken.JwtException si el token no supera la verificación
     */
    public JwtIdentity verify(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String username = claims.getSubject();
        String role = claims.get("rol", String.class);
        if (username == null || username.isBlank() || role == null) {
            throw new JwtException("Token sin identidad o rol");
        }
        try {
            return new JwtIdentity(username, Rol.valueOf(role));
        } catch (IllegalArgumentException exception) {
            throw new JwtException("Rol inválido en el token", exception);
        }
    }

    /** Datos verificados del token, no un DTO de respuesta. */
    public record JwtIdentity(String username, Rol rol) {
    }
}
