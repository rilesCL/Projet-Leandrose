package ca.cal.leandrose.service.dto.evaluation.employer;

import ca.cal.leandrose.service.dto.evaluation.InternshipInfoDto;
import ca.cal.leandrose.service.dto.evaluation.StudentInfoDto;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationEmployerInfoResponseDto {
  private StudentInfoDto studentInfo;
  private InternshipInfoDto internshipInfo;
  private Map<String, String> error;

  public static EvaluationEmployerInfoResponseDto fromInfo(EvaluationEmployerInfoDto info) {
    return EvaluationEmployerInfoResponseDto.builder()
        .studentInfo(info.studentInfo())
        .internshipInfo(info.internshipInfo())
        .build();
  }

  public static EvaluationEmployerInfoResponseDto withErrorMessage(String message) {
    EvaluationEmployerInfoResponseDto dto = new EvaluationEmployerInfoResponseDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
