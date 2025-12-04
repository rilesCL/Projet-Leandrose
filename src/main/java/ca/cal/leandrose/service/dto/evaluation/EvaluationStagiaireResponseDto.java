package ca.cal.leandrose.service.dto.evaluation;

import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationStagiaireResponseDto {
  private Long id;
  private LocalDate dateEvaluation;
  private Long studentId;
  private Long employeurId;
  private Long professeurId;
  private Long internshipOfferId;
  private String employerPdfPath;
  private String professorPdfPath;
  private boolean submittedByEmployer;
  private boolean submittedByProfessor;
  private Map<String, String> error;

  public static EvaluationStagiaireResponseDto fromDto(EvaluationStagiaireDto dto) {
    return EvaluationStagiaireResponseDto.builder()
        .id(dto.id())
        .dateEvaluation(dto.dateEvaluation())
        .studentId(dto.studentId())
        .employeurId(dto.employeurId())
        .professeurId(dto.professeurId())
        .internshipOfferId(dto.internshipOfferId())
        .employerPdfPath(dto.employerPdfPath())
        .professorPdfPath(dto.professorPdfPath())
        .submittedByEmployer(dto.submittedByEmployer())
        .submittedByProfessor(dto.submittedByProfessor())
        .build();
  }

  public static EvaluationStagiaireResponseDto withErrorMessage(String message) {
    EvaluationStagiaireResponseDto dto = new EvaluationStagiaireResponseDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
