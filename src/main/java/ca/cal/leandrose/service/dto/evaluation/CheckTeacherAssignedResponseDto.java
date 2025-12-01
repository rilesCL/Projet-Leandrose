package ca.cal.leandrose.service.dto.evaluation;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckTeacherAssignedResponseDto {
    private Boolean teacherAssigned;
    private Map<String, String> error;

    public static CheckTeacherAssignedResponseDto withErrorMessage(String message) {
        CheckTeacherAssignedResponseDto dto = new CheckTeacherAssignedResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}

