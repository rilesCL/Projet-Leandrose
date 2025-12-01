package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Convocation;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConvocationDto {
  private Long id;
  private Long candidatureId;
  private LocalDateTime convocationDate;
  private String location;
  private String message;
  private Map<String, String> error;

  public static ConvocationDto create(Convocation convocation) {
    return ConvocationDto.builder()
        .id(convocation.getId())
        .candidatureId(
            convocation.getCandidature() != null ? convocation.getCandidature().getId() : null)
        .convocationDate(convocation.getConvocationDate())
        .location(convocation.getLocation())
        .message(convocation.getPersonnalMessage())
        .build();
  }

  public static ConvocationDto withErrorMessage(String message) {
    ConvocationDto dto = new ConvocationDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
