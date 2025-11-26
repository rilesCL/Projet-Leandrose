package ca.cal.leandrose.service.dto.evaluation.prof;

import java.time.LocalDate;

public record StudentTeacherDto(
        String fullname,
        LocalDate internshipStartDate
) {
}
