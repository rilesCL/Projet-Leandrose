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

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private String duree;

    @Column(nullable = false)
    private String horaires;

    private String lieu;

    private String modalitesTeletravail;

    private BigDecimal remuneration;

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
}