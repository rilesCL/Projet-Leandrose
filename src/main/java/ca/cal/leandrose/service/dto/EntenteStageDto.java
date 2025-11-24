package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntenteStageDto {

  private Long id;
  private Long candidatureId;

  private StudentDto student;
  private InternshipOfferDto internshipOffer;
  private ProfDto prof;
  private GestionnaireDto gestionnaire;

  private String missionsObjectifs;
  private EntenteStage.StatutEntente statut;

  private LocalDate dateDebut;
  private int duree;
  private String lieu;
  private Float remuneration;
  private String schoolTerm;

  private LocalDateTime dateCreation;
  private LocalDateTime dateModification;
  private String contactEntreprise;

  private String cheminDocumentPDF;

  private LocalDateTime dateSignatureEtudiant;
  private LocalDateTime dateSignatureEmployeur;
  private LocalDateTime dateSignatureGestionnaire;

  private Map<String, String> error;
  private boolean employeurASigner;

  public static EntenteStageDto withError(Map<String, String> error) {
    EntenteStageDto dto = new EntenteStageDto();
    dto.setError(error);
    return dto;
  }

  public static EntenteStageDto fromEntity(EntenteStage entente) {
    if (entente == null) return null;

    Candidature candidature = entente.getCandidature();
    Student student = candidature.getStudent();
    InternshipOffer offer = candidature.getInternshipOffer();

    return EntenteStageDto.builder()
        .id(entente.getId())
        .candidatureId(candidature.getId())
        .student(StudentDto.create(student))
        .internshipOffer(
            InternshipOfferDto.builder()
                .id(offer.getId())
                .description(offer.getDescription())
                .startDate(offer.getStartDate())
                .durationInWeeks(offer.getDurationInWeeks())
                .address(offer.getAddress())
                .remuneration(offer.getRemuneration())
                    .schoolTerm(offer.getSchoolTerm().getTermAsString())
                .status(offer.getStatus().name())
                .employeurId(offer.getEmployeur() != null ? offer.getEmployeur().getId() : null)
                .companyName(offer.getCompanyName())
                .pdfPath(offer.getPdfPath())
                .validationDate(offer.getValidationDate())
                .rejectionComment(offer.getRejectionComment())
                .employeurDto(
                    offer.getEmployeur() != null ? EmployeurDto.create(offer.getEmployeur()) : null)
                .build())
        .prof(entente.getProf() != null ? ProfDto.create(entente.getProf()) : null)
        .gestionnaire(
            entente.getGestionnaire() != null
                ? GestionnaireDto.create(entente.getGestionnaire())
                : null)
        .missionsObjectifs(entente.getMissionsObjectifs())
        .statut(entente.getStatut())
        .dateDebut(offer.getStartDate())
        .duree(offer.getDurationInWeeks())
        .remuneration(offer.getRemuneration())
            .schoolTerm(offer.getSchoolTerm().getTermAsString())
        .lieu(offer.getAddress())
        .contactEntreprise(offer.getEmployeurEmail())
        .dateCreation(entente.getDateCreation())
        .dateModification(entente.getDateModification())
        .cheminDocumentPDF(entente.getCheminDocumentPDF())
        .dateSignatureEtudiant(entente.getDateSignatureEtudiant())
        .dateSignatureEmployeur(entente.getDateSignatureEmployeur())
        .dateSignatureGestionnaire(entente.getDateSignatureGestionnaire())
        .employeurASigner(entente.getDateSignatureEmployeur() != null)
        .build();
  }

  public static EntenteStageDto withErrorMessage(String message) {
    EntenteStageDto dto = new EntenteStageDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
