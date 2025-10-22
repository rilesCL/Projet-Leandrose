package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.EntenteStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntenteStageDto {
    private Long id;
    private Long candidatureId;

    private Long studentId;
    private String studentNom;
    private String studentPrenom;

    private Long internshipOfferId;
    private String internshipOfferDescription;

    private String nomEntreprise;
    private String contactEntreprise;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String duree;
    private String horaires;
    private String lieu;
    private String modalitesTeletravail;
    private BigDecimal remuneration;
    private String missionsObjectifs;

    private EntenteStage.StatutEntente statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    private LocalDateTime dateSignatureEtudiant;
    private LocalDateTime dateSignatureEmployeur;
    private LocalDateTime dateSignatureGestionnaire;

    public static EntenteStageDto fromEntity(EntenteStage entente) {
        if (entente == null) {
            return null;
        }

        var candidature = entente.getCandidature();
        var student = candidature.getStudent();
        var offer = candidature.getInternshipOffer();

        return EntenteStageDto.builder()
                .id(entente.getId())
                .candidatureId(candidature.getId())
                .studentId(student.getId())
                .studentNom(student.getLastName())
                .studentPrenom(student.getFirstName())
                .internshipOfferId(offer.getId())
                .internshipOfferDescription(offer.getDescription())
                .nomEntreprise(offer.getCompanyName())
                .contactEntreprise(offer.getEmployeurEmail())
                .dateDebut(entente.getDateDebut())
                .dateFin(entente.getDateFin())
                .duree(entente.getDuree())
                .horaires(entente.getHoraires())
                .lieu(entente.getLieu())
                .modalitesTeletravail(entente.getModalitesTeletravail())
                .remuneration(entente.getRemuneration())
                .missionsObjectifs(entente.getMissionsObjectifs())
                .statut(entente.getStatut())
                .dateCreation(entente.getDateCreation())
                .dateModification(entente.getDateModification())
                .dateSignatureEtudiant(entente.getDateSignatureEtudiant())
                .dateSignatureEmployeur(entente.getDateSignatureEmployeur())
                .dateSignatureGestionnaire(entente.getDateSignatureGestionnaire())
                .build();
    }
}