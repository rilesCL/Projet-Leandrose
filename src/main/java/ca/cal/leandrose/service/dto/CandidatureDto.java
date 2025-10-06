package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Candidature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long offerId;
    private String offerDescription;
    private String companyName;
    private Long cvId;
    private Candidature.Status status;
    private LocalDate applicationDate;


    public static CandidatureDto fromEntity(Candidature candidature) {
        return CandidatureDto.builder()
                .id(candidature.getId())
                .studentId(candidature.getStudent().getId())
                .studentName(candidature.getStudent().getFirstName() + " " +
                        candidature.getStudent().getLastName())
                .offerId(candidature.getInternshipOffer().getId())
                .offerDescription(candidature.getInternshipOffer().getDescription())
                .companyName(candidature.getInternshipOffer().getCompanyName())
                .cvId(candidature.getCv().getId())
                .status(candidature.getStatus())
                .applicationDate(candidature.getApplicationDate())
                .build();
    }
}