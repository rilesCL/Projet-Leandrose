package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationListResponseDto {
    private List<EvaluationStagiaireDto> evaluations;
    private Map<String, String> error;

    public static EvaluationListResponseDto withErrorMessage(String message) {
        EvaluationListResponseDto dto = new EvaluationListResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}

