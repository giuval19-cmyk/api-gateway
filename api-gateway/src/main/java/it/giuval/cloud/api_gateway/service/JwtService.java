package it.giuval.cloud.api_gateway.service;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {
	
	@Value("${jwt.secret}")
    private String secretKey;
	
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

	public Claims extractClaims(String token) {
		return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
	}
}
