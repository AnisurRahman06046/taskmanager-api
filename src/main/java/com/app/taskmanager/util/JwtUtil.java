package com.app.taskmanager.util;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.app.taskmanager.exception.ErrorCode;
import com.app.taskmanager.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    // TODO: externalize via configuration property (e.g. app.security.jwt.secret)
    // before deploying to production.
    private final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private static final long EXPIRATION_MS = 1000L * 60 * 60;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses and validates the token. Throws a typed {@link UnauthorizedException}
     * with the precise reason so the security layer can surface a meaningful
     * error code to the client.
     */
    public Jws<Claims> parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN,
                    ErrorCode.EXPIRED_TOKEN.getDefaultMessage(), e);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN,
                    ErrorCode.INVALID_TOKEN.getDefaultMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN,
                    "Authentication token is empty or malformed", e);
        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN,
                    ErrorCode.INVALID_TOKEN.getDefaultMessage(), e);
        }
    }

    public String extractEmail(String token) {
        return parse(token).getBody().getSubject();
    }
}
