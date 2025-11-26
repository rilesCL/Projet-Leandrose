package ca.cal.leandrose.service.dto.evaluation;

public record EvaluationCreationResponse(
    Long evaluationId,
    String message,
    StudentInfoDto studentInfo,
    InternshipInfoDto internshipInfo) {}
