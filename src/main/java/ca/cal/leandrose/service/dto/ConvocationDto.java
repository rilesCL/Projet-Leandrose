package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Convocation;
import java.time.LocalDateTime;
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
}
