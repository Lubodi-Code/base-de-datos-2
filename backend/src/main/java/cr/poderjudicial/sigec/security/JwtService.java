package cr.poderjudicial.sigec.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/** Emision y validacion de tokens JWT (HS256). */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${sigec.security.jwt.secret}") String secret,
                      @Value("${sigec.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMinutes = expirationMinutes;
    }

    public String generar(Integer idUsuario, String username, String rol) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);
        return Jwts.builder()
                .subject(username)
                .claim("uid", idUsuario)
                .claim("rol", rol)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
