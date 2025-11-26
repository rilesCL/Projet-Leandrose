package ca.cal.leandrose.service.dto.evaluation;

import ca.cal.leandrose.model.SchoolTerm;
import java.time.LocalDate;

public record EligibleEvaluationDto(
    Long agreementId,
    Long studentId,
    Long offerId,
    String studentFirstName,
    String studentLastName,
    String studentProgram,
    String internshipDescription,
    String companyName,
    SchoolTerm internshipTerm,
    LocalDate startDate,
    LocalDate endDate,
    Boolean hasEvaluation,
    Long evaluationId,
    Boolean evaluationSubmitted) {}
