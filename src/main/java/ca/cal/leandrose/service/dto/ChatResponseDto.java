package ca.cal.leandrose.service.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
  private String response;
  private String sessionId;
  private Map<String, String> error;

  public static ChatResponseDto withErrorMessage(String message) {
    ChatResponseDto dto = new ChatResponseDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
