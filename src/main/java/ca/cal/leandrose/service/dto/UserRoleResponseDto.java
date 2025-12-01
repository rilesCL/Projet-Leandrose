package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.auth.Role;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponseDto {
    private Role role;
    private Map<String, String> error;

    public static UserRoleResponseDto withErrorMessage(String message) {
        UserRoleResponseDto dto = new UserRoleResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}

