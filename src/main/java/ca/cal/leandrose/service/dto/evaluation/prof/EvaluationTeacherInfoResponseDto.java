package ca.cal.leandrose.service.dto.evaluation.prof;

import ca.cal.leandrose.service.dto.ProfDto;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationTeacherInfoResponseDto {
    private EntrepriseTeacherDto entrepriseTeacherDto;
    private StudentTeacherDto studentTeacherDto;
    private ProfDto profDto;
    private Map<String, String> error;

    public static EvaluationTeacherInfoResponseDto fromInfo(EvaluationTeacherInfoDto info) {
        return EvaluationTeacherInfoResponseDto.builder()
                .entrepriseTeacherDto(info.entrepriseTeacherDto())
                .studentTeacherDto(info.studentTeacherDto())
                .profDto(info.profDto())
                .build();
    }

    public static EvaluationTeacherInfoResponseDto withErrorMessage(String message) {
        EvaluationTeacherInfoResponseDto dto = new EvaluationTeacherInfoResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}
