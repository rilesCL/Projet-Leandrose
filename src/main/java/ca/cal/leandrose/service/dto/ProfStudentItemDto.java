package ca.cal.leandrose.service.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfStudentItemDto {
  private Long ententeId;
  private Long studentId;
  private String studentFirstName;
  private String studentLastName;

  private String companyName;
  private String offerTitle;
  private LocalDate startDate;
  private LocalDate endDate;
  private String stageStatus;
  private String evaluationStatus;
}
