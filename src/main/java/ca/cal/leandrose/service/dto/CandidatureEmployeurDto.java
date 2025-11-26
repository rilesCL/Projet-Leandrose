package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Candidature;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureEmployeurDto {
  private Long id;

  private Long studentId;
  private String studentFirstName;
  private String studentLastName;
  private String studentProgram;

  private LocalDateTime applicationDate;
  private Candidature.Status status;

  private Long cvId;
  private String cvStatus;

  private Long offerId;
  private String offerDescription;

  public static CandidatureEmployeurDto fromEntity(Candidature candidature) {
    return CandidatureEmployeurDto.builder()
        .id(candidature.getId())
        .studentId(candidature.getStudent().getId())
        .studentFirstName(candidature.getStudent().getFirstName())
        .studentLastName(candidature.getStudent().getLastName())
        .studentProgram(candidature.getStudent().getProgram())
        .applicationDate(candidature.getApplicationDate())
        .status(candidature.getStatus())
        .cvId(candidature.getCv().getId())
        .cvStatus(candidature.getCv().getStatus().name())
        .offerId(candidature.getInternshipOffer().getId())
        .offerDescription(candidature.getInternshipOffer().getDescription())
        .build();
  }
}
