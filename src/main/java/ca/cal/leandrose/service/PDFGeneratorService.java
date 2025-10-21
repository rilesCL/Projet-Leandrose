package ca.cal.leandrose.service;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.model.InternshipOffer;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class PDFGeneratorService {

    private static final String PDF_EXTENSION = ".pdf";
    private static final String DEFAULT_BASE_DIR = "uploads/ententes";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.entente.base-dir:" + DEFAULT_BASE_DIR + "}")
    private String baseUploadDir;

    /**
     * Génère le PDF de l'entente de stage
     * @param entente L'entente à générer
     * @return Le chemin complet du fichier PDF généré
     */
    public String genererEntentePDF(EntenteStage entente) {
        try {
            // Créer le répertoire si nécessaire
            Path ententeDir = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(ententeDir)) {
                Files.createDirectories(ententeDir);
            }

            // Nom du fichier unique
            String filename = "entente_" + entente.getId() + "_" + UUID.randomUUID() + PDF_EXTENSION;
            Path targetPath = ententeDir.resolve(filename);

            // Créer le document PDF
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile()));

            document.open();

            // Générer le contenu
            addHeader(document);
            addTitle(document);
            addPartiesInfo(document, entente);
            addStageDetails(document, entente);
            addMissionsObjectifs(document, entente);
            addSignatures(document);
            addFooter(document);

            document.close();
            writer.close();

            log.info("PDF généré avec succès : {}", targetPath);
            return targetPath.toString();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'entente {}", entente.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF de l'entente", e);
        }
    }

    /**
     * Lit et retourne le contenu d'un fichier PDF
     */
    public byte[] lireFichierPDF(String cheminFichier) {
        try {
            Path path = Paths.get(cheminFichier);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Fichier PDF non trouvé : " + cheminFichier);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier PDF : {}", cheminFichier, e);
            throw new RuntimeException("Erreur lors de la lecture du PDF", e);
        }
    }

    /**
     * Supprime un fichier PDF
     */
    public void supprimerFichierPDF(String cheminFichier) {
        try {
            Path path = Paths.get(cheminFichier);
            Files.deleteIfExists(path);
            log.info("PDF supprimé : {}", cheminFichier);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier PDF : {}", cheminFichier, e);
        }
    }

    // ========== MÉTHODES PRIVÉES POUR LA GÉNÉRATION DU PDF ==========

    private void addHeader(Document document) throws DocumentException {
        // Logo ou en-tête de l'établissement (optionnel)
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
        Paragraph header = new Paragraph("COLLÈGE ANDRÉ-LAURENDEAU", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(10f);
        document.add(header);

        // Ligne de séparation
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("ENTENTE DE STAGE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
    }

    private void addPartiesInfo(Document document, EntenteStage entente) throws DocumentException {
        Student student = entente.getStudent();
        InternshipOffer offer = entente.getInternshipOffer();

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // SECTION : LES PARTIES
        Paragraph partiesTitle = new Paragraph("LES PARTIES", sectionFont);
        partiesTitle.setSpacingBefore(10f);
        partiesTitle.setSpacingAfter(10f);
        document.add(partiesTitle);

        // L'ÉTUDIANT
        document.add(new Paragraph("L'ÉTUDIANT :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Nom : " + student.getLastName() + " " + student.getFirstName(), normalFont));
        document.add(new Paragraph("Numéro étudiant : " + student.getStudentNumber(), normalFont));
        document.add(new Paragraph("Programme : " + student.getProgram(), normalFont));
        document.add(new Paragraph("Email : " + student.getCredentials().getEmail(), normalFont));
        document.add(Chunk.NEWLINE);

        // L'ENTREPRISE
        document.add(new Paragraph("L'ENTREPRISE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Nom de l'entreprise : " + entente.getNomEntreprise(), normalFont));
        document.add(new Paragraph("Personne contact : " + entente.getContactEntreprise(), normalFont));
        if (entente.getLieu() != null) {
            document.add(new Paragraph("Adresse : " + entente.getLieu(), normalFont));
        }
        document.add(Chunk.NEWLINE);

        // LE COLLÈGE
        document.add(new Paragraph("LE COLLÈGE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Collège André-Laurendeau", normalFont));
        document.add(new Paragraph("1111, rue Lapierre, Montréal (Québec) H8N 2J4", normalFont));
        document.add(Chunk.NEWLINE);
    }

    private void addStageDetails(Document document, EntenteStage entente) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // SECTION : DÉTAILS DU STAGE
        Paragraph detailsTitle = new Paragraph("DÉTAILS DU STAGE", sectionFont);
        detailsTitle.setSpacingBefore(15f);
        detailsTitle.setSpacingAfter(10f);
        document.add(detailsTitle);

        // Créer un tableau pour les détails
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Style des cellules
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        // Ajouter les lignes
        addTableRow(table, "Titre du stage", entente.getTitreStage(), cellBoldFont, cellFont);
        addTableRow(table, "Date de début", entente.getDateDebut().format(DATE_FORMATTER), cellBoldFont, cellFont);
        addTableRow(table, "Date de fin", entente.getDateFin().format(DATE_FORMATTER), cellBoldFont, cellFont);
        addTableRow(table, "Durée", entente.getDuree(), cellBoldFont, cellFont);
        addTableRow(table, "Horaires", entente.getHoraires(), cellBoldFont, cellFont);

        if (entente.getLieu() != null && !entente.getLieu().isBlank()) {
            addTableRow(table, "Lieu", entente.getLieu(), cellBoldFont, cellFont);
        }

        if (entente.getModalitesTeletravail() != null && !entente.getModalitesTeletravail().isBlank()) {
            addTableRow(table, "Télétravail", entente.getModalitesTeletravail(), cellBoldFont, cellFont);
        }

        if (entente.getRemuneration() != null) {
            addTableRow(table, "Rémunération", entente.getRemuneration() + " $", cellBoldFont, cellFont);
        }

        document.add(table);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5f);
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addMissionsObjectifs(Document document, EntenteStage entente) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // SECTION : MISSIONS ET OBJECTIFS
        Paragraph missionsTitle = new Paragraph("MISSIONS ET OBJECTIFS DU STAGE", sectionFont);
        missionsTitle.setSpacingBefore(15f);
        missionsTitle.setSpacingAfter(10f);
        document.add(missionsTitle);

        Paragraph missions = new Paragraph(entente.getMissionsObjectifs(), normalFont);
        missions.setAlignment(Element.ALIGN_JUSTIFIED);
        missions.setSpacingAfter(15f);
        document.add(missions);
    }

    private void addSignatures(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // SECTION : SIGNATURES
        Paragraph signaturesTitle = new Paragraph("SIGNATURES", sectionFont);
        signaturesTitle.setSpacingBefore(20f);
        signaturesTitle.setSpacingAfter(15f);
        document.add(signaturesTitle);

        // Créer un tableau à 3 colonnes pour les signatures
        PdfPTable signatureTable = new PdfPTable(3);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(10f);

        // Cellule Étudiant
        PdfPCell etudiantCell = createSignatureCell("L'ÉTUDIANT", normalFont);
        signatureTable.addCell(etudiantCell);

        // Cellule Employeur
        PdfPCell employeurCell = createSignatureCell("L'EMPLOYEUR", normalFont);
        signatureTable.addCell(employeurCell);

        // Cellule Gestionnaire
        PdfPCell gestionnaireCell = createSignatureCell("LE GESTIONNAIRE", normalFont);
        signatureTable.addCell(gestionnaireCell);

        document.add(signatureTable);

        // Date de signature
        Paragraph dateSignature = new Paragraph(
                "\nDate : " + LocalDate.now().format(DATE_FORMATTER),
                FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY)
        );
        dateSignature.setAlignment(Element.ALIGN_RIGHT);
        dateSignature.setSpacingBefore(30f);
        document.add(dateSignature);
    }

    private PdfPCell createSignatureCell(String label, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);
        cell.setMinimumHeight(80f);

        Paragraph labelPara = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9));
        labelPara.setAlignment(Element.ALIGN_CENTER);
        labelPara.setSpacingAfter(40f);

        Paragraph signatureLine = new Paragraph("_______________________", font);
        signatureLine.setAlignment(Element.ALIGN_CENTER);

        Paragraph dateLabel = new Paragraph("Date : ______________", FontFactory.getFont(FontFactory.HELVETICA, 8));
        dateLabel.setAlignment(Element.ALIGN_CENTER);
        dateLabel.setSpacingBefore(5f);

        cell.addElement(labelPara);
        cell.addElement(signatureLine);
        cell.addElement(dateLabel);

        return cell;
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
                "\nCe document constitue une entente officielle entre les parties mentionnées ci-dessus. " +
                        "Toute modification doit faire l'objet d'un avenant signé par toutes les parties.",
                footerFont
        );
        footer.setAlignment(Element.ALIGN_JUSTIFIED);
        footer.setSpacingBefore(30f);
        document.add(footer);
    }
}