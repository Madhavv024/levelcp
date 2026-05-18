package com.levelUpZone.levelUpZone_backend.Config;


import com.levelUpZone.levelUpZone_backend.Entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "jabardastcoder-secret-key-very-long";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15;   // 15 minutes
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24;  // 1 day

    public String generateToken(UserEntity user, String tokenType) {

        long expirationTime = tokenType.equalsIgnoreCase("refresh")
                ? REFRESH_EXPIRATION
                : ACCESS_EXPIRATION;

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", tokenType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);

            // Check expiration
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false;
            }

            // Check token type
            String type = claims.get("type", String.class);
            return expectedType.equalsIgnoreCase(type);

        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String refreshToken) {
        try {
            Claims claims = extractClaims(refreshToken);

            return claims.getSubject(); // usually, the subject is the email or username
        }catch (ExpiredJwtException e) {
            // Token is expired, but we can still extract claims
            return e.getClaims().getSubject();
        }
        catch (SignatureException e) {
            // invalid signature
            throw new RuntimeException("Invalid JWT signature");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token");
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
