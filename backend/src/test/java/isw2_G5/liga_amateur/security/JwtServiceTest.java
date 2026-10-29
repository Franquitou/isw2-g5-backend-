package isw2_G5.liga_amateur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import isw2_G5.liga_amateur.users.entity.Rol;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.Test;

/** Pruebas criptográficas aisladas; generan secretos efímeros y no usan H2. */
class JwtServiceTest {

    @Test
    void signedTokenContainsIdentityRoleAndEightHourExpiration() {
        String secret = randomSecret();
        JwtService jwt = new JwtService(secret);
        String token = jwt.generate("delegado1", Rol.DELEGADO);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo("delegado1");
        assertThat(claims.get("rol", String.class)).isEqualTo("DELEGADO");
        assertThat(Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
                .isEqualTo(Duration.ofHours(8));
        assertThat(jwt.verify(token).rol()).isEqualTo(Rol.DELEGADO);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtService(randomSecret()).generate("admin", Rol.ADMIN);
        assertThatThrownBy(() -> new JwtService(randomSecret()).verify(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String secret = randomSecret();
        String expired = Jwts.builder().subject("admin").claim("rol", "ADMIN")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> new JwtService(secret).verify(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void insufficientSecretIsRejected() {
        assertThatThrownBy(() -> new JwtService("corto"))
                .isInstanceOf(WeakKeyException.class);
    }

    private String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
