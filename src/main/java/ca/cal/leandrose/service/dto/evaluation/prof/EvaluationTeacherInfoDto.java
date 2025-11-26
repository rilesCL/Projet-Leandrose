package ca.cal.leandrose.service.dto.evaluation.prof;

import ca.cal.leandrose.service.dto.ProfDto;

public record EvaluationTeacherInfoDto(
        EntrepriseTeacherDto entrepriseTeacherDto,
        StudentTeacherDto studentTeacherDto,
        ProfDto profDto
) {
}
