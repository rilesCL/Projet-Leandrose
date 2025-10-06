package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Student;
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
    private Long employeurId;
    private Long cvId;
    private Candidature.Status status;
    private LocalDate applicationDate;

    public static Student getStudentFromCandidature(Candidature candidature) {
        return candidature.getStudent();
    }

    public static InternshipOffer getOfferFromCandidature(Candidature candidature) {
        return candidature.getInternshipOffer();
    }

    public static Cv getCvFromCandidature(Candidature candidature) {
        return candidature.getCv();
    }

    public static CandidatureDto fromEntity(Candidature candidature) {
        return CandidatureDto.builder()
                .id(candidature.getId())
                .studentId(getStudentFromCandidature(candidature).getId())
                .studentName(getStudentFromCandidature(candidature).getFirstName() + " " +
                        getStudentFromCandidature(candidature).getLastName())
                .offerId(getOfferFromCandidature(candidature).getId())
                .offerDescription(getOfferFromCandidature(candidature).getDescription())
                .companyName(getOfferFromCandidature(candidature).getCompanyName())
                .cvId(getCvFromCandidature(candidature).getId())
                .status(candidature.getStatus())
                .applicationDate(candidature.getApplicationDate())
                .employeurId(candidature.getEmployeurId())  // ✅ Loi de Déméter respectée
                .build();
    }
}