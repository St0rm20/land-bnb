package com.labndbnb.landbnb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JWTutils {

   @Value("${jwt.password.security}")
    private String SECRET_KEY;


    public String generateToken(String id, Map<String, String> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(id)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                // 👇 Asegura que se firme con el mismo algoritmo HS512
                .signWith(getKey(), Jwts.SIG.HS512)
                .compact();
    }


    public Jws<Claims> parseJwt(String jwtString)
            throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, IllegalArgumentException {
        JwtParser parser = Jwts.parser()
                .verifyWith(getKey())
                .build();
        return parser.parseSignedClaims(jwtString);
    }


    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
