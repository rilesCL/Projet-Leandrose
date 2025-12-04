package ca.cal.leandrose.service.dto.evaluation;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckExistingEvaluationResponseDto {
  private Boolean exists;
  private EvaluationStagiaireDto evaluation;
  private String message;
  private Map<String, String> error;

  public static CheckExistingEvaluationResponseDto withErrorMessage(String message) {
    CheckExistingEvaluationResponseDto dto = new CheckExistingEvaluationResponseDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
