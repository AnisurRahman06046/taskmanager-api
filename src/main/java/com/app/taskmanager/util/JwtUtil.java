package com.app.taskmanager.util;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final String SECRET = "mysecretkeymysecretkeymysecretkey";

    private Key getSignInKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email){
        return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis()+1000*60*60))
        .signWith(getSignInKey(),SignatureAlgorithm.HS256)
        .compact();
    }

    public String extractEmail(String token){
        return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    }
    public boolean isValid(String token){
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parse(token);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            return  false;
        }
    }
}
