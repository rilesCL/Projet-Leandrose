package ca.cal.leandrose.service.dto.evaluation.employer;

import ca.cal.leandrose.service.dto.evaluation.InternshipInfoDto;
import ca.cal.leandrose.service.dto.evaluation.StudentInfoDto;

public record EvaluationEmployerInfoDto(
        StudentInfoDto studentInfo,
        InternshipInfoDto internshipInfo
) {}
