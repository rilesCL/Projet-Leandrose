package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Candidature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureDto {

  private Long id;

  private StudentDto student;
  private InternshipOfferDto internshipOffer;
  private CvDto cv;

  private Candidature.Status status;
  private LocalDateTime applicationDate;

  public static CandidatureDto fromEntity(Candidature candidature) {
    if (candidature == null) return null;

    return CandidatureDto.builder()
        .id(candidature.getId())
        .student(StudentDto.create(candidature.getStudent()))
        .internshipOffer(
            InternshipOfferDto.builder()
                .id(candidature.getInternshipOffer().getId())
                .description(candidature.getInternshipOffer().getDescription())
                .startDate(candidature.getInternshipOffer().getStartDate())
                .durationInWeeks(candidature.getInternshipOffer().getDurationInWeeks())
                .address(candidature.getInternshipOffer().getAddress())
                .remuneration(candidature.getInternshipOffer().getRemuneration())
                .companyName(candidature.getInternshipOffer().getCompanyName())
                .employeurId(candidature.getInternshipOffer().getEmployeurId())
                .build())
        .cv(CvDto.create(candidature.getCv()))
        .status(candidature.getStatus())
        .applicationDate(candidature.getApplicationDate())
        .build();
  }

  public Long getEmployeurId() {
    return internshipOffer.getEmployeurId();
  }
}
