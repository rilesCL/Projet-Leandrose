package ca.cal.leandrose.service.dto.evaluation;

public record GeneratePdfRequest(
    Long studentId, Long internshipOfferId, EvaluationFormData formData) {}
