package com.yedam.scm.security;

import com.yedam.scm.dto.LoginRes;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expirationTime;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.expirationTime = jwtProperties.getExpiration();
    }

    public String generateToken(LoginRes res) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(res.getAccountId())
                .claim("name", res.getName())
                .claim("code", res.getCode())
                .claim("role", res.getRole())
                .claim("tempPassword", res.getTempPassword())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTempToken(String accountId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000);

        return Jwts.builder()
                .setSubject(accountId)
                .claim("type", "temp")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료됨");
        } catch (JwtException e) {
            System.out.println("토큰이 유효하지 않음");
        }
        return false;
    }

    public boolean validateTempToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"temp".equals(type)) {
                System.out.println("임시 토큰 타입 불일치");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("임시 토큰 만료됨");
        } catch (JwtException e) {
            System.out.println("임시 토큰이 유효하지 않음");
        }
        return false;
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public long getExpiration(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration().getTime();
    }

}
