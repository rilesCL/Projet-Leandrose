package ca.cal.leandrose.service.dto.evaluation;


import java.time.LocalDate;

public record EvaluationStagiaireDto(
        Long id,
        LocalDate dateEvaluation,
        Long studentId,
        Long employeurId,
        Long professeurId,
        Long internshipOfferId,
        String employerPdfPath,
        String professorPdfPath,
        boolean submittedByEmployer,
        boolean submittedByProfessor
) {
}
