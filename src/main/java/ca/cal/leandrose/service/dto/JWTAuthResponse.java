package ca.cal.leandrose.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTAuthResponse {
  private final String tokenType = "BEARER";
  private String accessToken;
}
