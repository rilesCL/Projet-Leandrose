package ca.cal.leandrose.service.dto.evaluation;

public record CreateEvaluationRequest(
        Long studentId,
        Long internshipOfferId
) {
}
