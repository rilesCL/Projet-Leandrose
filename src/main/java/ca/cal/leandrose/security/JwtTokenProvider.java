package ca.cal.leandrose.security;

import ca.cal.leandrose.security.exception.InvalidJwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
  @Value("${application.security.jwt.expiration}")
  private int expirationInMs;

  @Value("${application.security.jwt.secret-key}")
  private final String jwtSecret =
      "2B7E151628AED2A6ABF7158809CF4F3C2B7E151628AED2A6ABF7158809CF4F3C";

  public String generateToken(Authentication authentication) {
    long nowMillis = System.currentTimeMillis();
    JwtBuilder builder =
        Jwts.builder()
            .setSubject(authentication.getName())
            .setIssuedAt(new Date(nowMillis))
            .setExpiration(new Date(nowMillis + expirationInMs))
            .claim("authorities", authentication.getAuthorities())
            .signWith(key());
    return builder.compact();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public String getEmailFromJWT(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public void validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    } catch (SecurityException ex) {
      throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "JWT claims string is empty");
    }
  }
}
