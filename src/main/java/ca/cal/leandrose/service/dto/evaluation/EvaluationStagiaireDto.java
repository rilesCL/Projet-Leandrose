package ca.cal.leandrose.service.dto.evaluation;

import java.time.LocalDate;

public record EvaluationStagiaireDto(
    Long id,
    LocalDate dateEvaluation,
    Long studentId,
    Long employeurId,
    Long internshipOfferId,
    String pdfFilePath,
    boolean submitted) {}
