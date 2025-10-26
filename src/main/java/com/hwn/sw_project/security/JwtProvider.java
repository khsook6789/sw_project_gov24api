package com.hwn.sw_project.security;

import com.hwn.sw_project.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtProvider(JwtProperties props){
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
        this.accessTtlMillis = props.getAccessTokenExpireSeconds() * 1000L;
        this.refreshTtlMillis = props.getRefreshTokenExpireSeconds() * 1000L;
    }

    public String createAccessToken(String subject,Map<String, ?> claims){
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTtlMillis)))
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String subject){
        var now = Instant.now();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTtlMillis)))
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}

