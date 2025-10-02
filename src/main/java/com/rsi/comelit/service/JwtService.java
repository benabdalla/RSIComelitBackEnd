package com.rsi.comelit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.rsi.comelit.common.AppConstants;
import com.rsi.comelit.common.UserPrincipal;
import com.rsi.comelit.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final Key SECRET_KEY;

    public JwtService(@Value("${jwt.secret}") String secret) {
        // Decode Base64 string to bytes
        this.SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Durée de validité du token (1 jour)
    private static final long TOKEN_VALIDITY = 3 * 60 * 60 * 1000; // 3 hours in ms

    // Génère un token JWT pour un utilisateur
    public String generateToken(String email, String role, String nom, String prenom, Long id) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("nom", nom)
                .claim("prenom", prenom)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extrait l'email (username) du token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    // Extrait l'email (username) du token
    public Long extractId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }

    // Vérifie si le token est valide pour l'utilisateur
    public boolean validateToken(String token, User utilisateur) {
        String tokenEmail = extractUsername(token); // returns the subject, i.e., email
        return tokenEmail.equals(utilisateur.getEmail()) && !isTokenExpired(token);
    }


    // Vérifie si le token est expiré
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    public String extractClaim(String token, String claimName) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName);
    }


    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        String[] claims = getClaimsFromToken(token);
        return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public String getSubjectFromToken(String token) {
        JWTVerifier jwtVerifier = getJwtVerifier();
        return jwtVerifier.verify(token).getSubject();
    }

    public Boolean isTokenValid(String email, String token) {
        JWTVerifier jwtVerifier = getJwtVerifier();
        return StringUtils.isNotEmpty(email) && !isTokenExpired(jwtVerifier, token);
    }

    private Boolean isTokenExpired(JWTVerifier jwtVerifier, String token) {
        Date expiration = jwtVerifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        List<String> authorities = new ArrayList<>();
        userPrincipal.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));
        return authorities.toArray(new String[0]);
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier jwtVerifier = getJwtVerifier();
        return jwtVerifier.verify(token).getClaim(AppConstants.AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJwtVerifier() {
        JWTVerifier jwtVerifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(SECRET_KEY.getEncoded());
            jwtVerifier = JWT.require(algorithm).build();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException(AppConstants.TOKEN_UNVERIFIABLE);
        }
        return jwtVerifier;
    }

    public String generateToken(UserPrincipal userPrincipal) {
            return Jwts.builder()
                    .setSubject(userPrincipal.getEmail())
                    .claim("role", userPrincipal.getRole())
                    .claim("nom", userPrincipal.getUsername())
                    .claim("avatarUrl",userPrincipal.getProfilePhoto())
                    .claim("username",userPrincipal.getFirstName())
                    .claim("lastname", userPrincipal.getLastName())
                    .claim("id",userPrincipal.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                    .signWith(SECRET_KEY)
                    .compact();
        }


}


