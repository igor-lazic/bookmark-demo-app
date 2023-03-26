package biz.asio.bookmark.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    //The JWT signature algorithm we will be using to sign the token
    private SignatureAlgorithm signatureAlgorithm;
    private Key signingKey;

    @PostConstruct
    public void init() {
        //The JWT signature algorithm we will be using to sign the token
        signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = secret.getBytes(StandardCharsets.UTF_8);
        signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
    }

    /**
     * Tries to parse specified String as a JWT token. If successful, returns User object with username, id and role prefilled (extracted from token).
     * If unsuccessful (token is invalid or not containing all required user properties), simply returns null.
     *
     * @param token the JWT token to parse
     * @return the User object extracted from specified token or null if a token is invalid.
     */
    public User parseToken(String token) {
        try {
            Claims body = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<GrantedAuthority> authorities = new ArrayList<>();

            return new User(body.getSubject(), "", authorities);

        } catch (JwtException | ClassCastException e) {
            log.error("Failed to parse token.", e);
            return null;
        }
    }

    /**
     * Generates a JWT token containing username as subject.
     *
     * @param authentication authentication object containing user details
     * @return the JWT token
     */
    public String generateToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Claims claims = Jwts.claims().setSubject(userPrincipal.getUsername());

        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        // issue token valid for 4 hours
        Instant expiration = issuedAt.plus(4, ChronoUnit.HOURS);

        claims.put(Claims.ISSUED_AT, Date.from(issuedAt));
        claims.put(Claims.EXPIRATION, Date.from(expiration));

        return Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }
}
