package linktic_store.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

// Servicio que centraliza toda la lógica de creación y validación de tokens JWT.
@Service
public class JwtService {

    // Valores leídos de application.yaml en la sección security.jwt
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;

    // Crea un token JWT firmado con el email como subject y una fecha de expiración.
    // Se usa el email (no el name) porque el UserDetailsService carga al usuario por email.
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    // Verifica que el token pertenezca al usuario y no haya expirado.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    // Extrae el username (claim "sub") del token.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    // Permite extraer cualquier claim del token usando una función lambda.
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Parsea y valida la firma del token; lanza excepción si es inválido o fue manipulado.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Decodifica la clave secreta en Base64 y genera una clave HMAC-SHA para firmar.
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
