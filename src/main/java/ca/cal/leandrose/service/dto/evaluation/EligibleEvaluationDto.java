package ca.cal.leandrose.service.dto.evaluation;

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
        LocalDate startDate,
        LocalDate endDate
) {
}
