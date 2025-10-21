package ca.cal.leandrose.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String nomEntreprise;

    @Column(nullable = false)
    private String contactEntreprise;

    @Column(nullable = false)
    private String titreStage;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Column(nullable = false)
    private String duree;

    @Column(nullable = false)
    private String horaires;

    private String lieu;

    private String modalitesTeletravail;

    private Float remuneration;

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

    public Student getStudent() {
        return candidature.getStudent();
    }

    public InternshipOffer getInternshipOffer() {
        return candidature.getInternshipOffer();
    }

    public Long getEmployeurId() {
        return candidature.getEmployeurId();
    }

    public enum StatutEntente {
        BROUILLON,              // En cours de création
        EN_ATTENTE_SIGNATURE,   // PDF généré, en attente de signatures
        SIGNE_ETUDIANT,         // Étudiant a signé
        SIGNE_EMPLOYEUR,        // Employeur a signé
        SIGNE_GESTIONNAIRE,     // Gestionnaire a signé
        VALIDEE                 // Toutes les signatures complètes
    }
}