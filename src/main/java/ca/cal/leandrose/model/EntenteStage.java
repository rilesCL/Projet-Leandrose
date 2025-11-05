package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntenteStage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Candidature candidature;

  @ManyToOne
  @JoinColumn(name = "prof_id")
  private Prof prof;

  @ManyToOne
  @JoinColumn
  private Gestionnaire gestionnaire;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String missionsObjectifs;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatutEntente statut;

  @Column(nullable = false)
  private LocalDateTime dateCreation;

  private LocalDateTime dateModification;

  private String cheminDocumentPDF;

  private LocalDateTime dateSignatureEtudiant;
  private LocalDateTime dateSignatureEmployeur;
  private LocalDateTime dateSignatureGestionnaire;

  public enum StatutEntente {
    BROUILLON,
    EN_ATTENTE_SIGNATURE,
    VALIDEE
  }

  public InternshipOffer getOffer() {
    return candidature.getInternshipOffer();
  }

  public Employeur getEmployeur() {
    return getOffer().getEmployeur();
  }

  public Student getStudent() {
    return candidature.getStudent();
  }

  public float getRemuneration() {
    Float remuneration = getOffer().getRemuneration();
    return remuneration != null ? remuneration : 0f;
  }

  public String getAddress() {
    return getOffer().getAddress();
  }

  public int getDurationInWeeks() {
    return getOffer().getDurationInWeeks();
  }

  public LocalDate getStartDate() {
    return getOffer().getStartDate();
  }
}
