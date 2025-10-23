package ca.cal.leandrose.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntenteStageTest {

    @Test
    void testEntenteStageBuilder() {
        Candidature candidature = new Candidature();
        candidature.setId(1L);

        LocalDate dateDebut = LocalDate.of(2025, 6, 1);
        LocalDate dateFin = LocalDate.of(2025, 8, 31);
        LocalDateTime now = LocalDateTime.now();

        EntenteStage entente = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .missionsObjectifs("Développement web")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(now)
                .cheminDocumentPDF("/path/to/pdf")
                .build();

        assertEquals(1L, entente.getId());
        assertEquals(candidature, entente.getCandidature());
        assertEquals("Développement web", entente.getMissionsObjectifs());
        assertEquals(EntenteStage.StatutEntente.BROUILLON, entente.getStatut());
        assertEquals(now, entente.getDateCreation());
        assertEquals("/path/to/pdf", entente.getCheminDocumentPDF());
    }

    @Test
    void testEntenteStageNoArgsConstructorAndSetters() {
        EntenteStage entente = new EntenteStage();
        Candidature candidature = new Candidature();
        candidature.setId(2L);

        LocalDate dateDebut = LocalDate.of(2025, 9, 1);
        LocalDate dateFin = LocalDate.of(2025, 12, 31);
        LocalDateTime dateCreation = LocalDateTime.now();
        LocalDateTime dateModification = LocalDateTime.now().plusDays(1);

        entente.setId(2L);
        entente.setCandidature(candidature);
        entente.setMissionsObjectifs("Backend development");
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateCreation(dateCreation);
        entente.setDateModification(dateModification);
        entente.setCheminDocumentPDF("/path/to/pdf2");

        assertEquals(2L, entente.getId());
        assertEquals(candidature, entente.getCandidature());
        assertEquals("Backend development", entente.getMissionsObjectifs());
        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, entente.getStatut());
        assertEquals(dateCreation, entente.getDateCreation());
        assertEquals(dateModification, entente.getDateModification());
        assertEquals("/path/to/pdf2", entente.getCheminDocumentPDF());
    }

    @Test
    void testEntenteStageWithSignatureDates() {
        EntenteStage entente = new EntenteStage();
        LocalDateTime signatureEtudiant = LocalDateTime.now();
        LocalDateTime signatureEmployeur = LocalDateTime.now().plusHours(1);
        LocalDateTime signatureGestionnaire = LocalDateTime.now().plusHours(2);

        entente.setDateSignatureEtudiant(signatureEtudiant);
        entente.setDateSignatureEmployeur(signatureEmployeur);
        entente.setDateSignatureGestionnaire(signatureGestionnaire);

        assertEquals(signatureEtudiant, entente.getDateSignatureEtudiant());
        assertEquals(signatureEmployeur, entente.getDateSignatureEmployeur());
        assertEquals(signatureGestionnaire, entente.getDateSignatureGestionnaire());
    }

    @Test
    void testStatutEntenteEnum() {
        assertEquals(3, EntenteStage.StatutEntente.values().length);
        assertEquals(EntenteStage.StatutEntente.BROUILLON, EntenteStage.StatutEntente.valueOf("BROUILLON"));
        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, EntenteStage.StatutEntente.valueOf("EN_ATTENTE_SIGNATURE"));
        assertEquals(EntenteStage.StatutEntente.VALIDEE, EntenteStage.StatutEntente.valueOf("VALIDEE"));
    }

    @Test
    void testEntenteStageEqualsAndHashCode() {
        EntenteStage entente1 = EntenteStage.builder()
                .id(1L)
                .missionsObjectifs("Test")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        EntenteStage entente2 = EntenteStage.builder()
                .id(1L)
                .missionsObjectifs("Test")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(entente1.getDateCreation())
                .build();

        assertEquals(entente1, entente2);
        assertEquals(entente1.hashCode(), entente2.hashCode());
    }
}