package ca.cal.leandrose.service;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.Candidature;
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

    public String genererEntentePDF(EntenteStage entente) {
        try {
            Path ententeDir = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(ententeDir)) {
                Files.createDirectories(ententeDir);
            }

            String filename = "entente_" + entente.getId() + "_" + UUID.randomUUID() + PDF_EXTENSION;
            Path targetPath = ententeDir.resolve(filename);

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile()));

            document.open();

            addHeader(document);
            addTitle(document);
            addPartiesInfo(document, entente);
            addStageDetails(document, entente);
            addMissionsObjectifs(document, entente);
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

    public void supprimerFichierPDF(String cheminFichier) {
        try {
            Path path = Paths.get(cheminFichier);
            Files.deleteIfExists(path);
            log.info("PDF supprimé : {}", cheminFichier);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier PDF : {}", cheminFichier, e);
        }
    }

    private void addHeader(Document document) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
        Paragraph header = new Paragraph("COLLÈGE ANDRÉ-LAURENDEAU", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(10f);
        document.add(header);

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
        Candidature candidature = entente.getCandidature();
        var student = candidature.getStudent();
        InternshipOffer offer = candidature.getInternshipOffer();

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        Paragraph partiesTitle = new Paragraph("LES PARTIES", sectionFont);
        partiesTitle.setSpacingBefore(10f);
        partiesTitle.setSpacingAfter(10f);
        document.add(partiesTitle);

        document.add(new Paragraph("L'ÉTUDIANT :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Nom : " + student.getLastName() + " " + student.getFirstName(), normalFont));
        document.add(new Paragraph("Email : " + student.getCredentials().getEmail(), normalFont));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("L'ENTREPRISE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Nom de l'entreprise : " + offer.getCompanyName(), normalFont));
        document.add(new Paragraph("Personne contact : " + offer.getEmployeurEmail(), normalFont));
        if (entente.getAddress() != null) {
            document.add(new Paragraph("Adresse : " + entente.getAddress(), normalFont));
        }
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("LE COLLÈGE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        document.add(new Paragraph("Collège André-Laurendeau", normalFont));
        document.add(new Paragraph("1111, rue Lapierre, Montréal (Québec) H8N 2J4", normalFont));
        document.add(Chunk.NEWLINE);
    }

    private void addStageDetails(Document document, EntenteStage entente) throws DocumentException {
        Candidature candidature = entente.getCandidature();
        InternshipOffer offer = candidature.getInternshipOffer();

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        Paragraph detailsTitle = new Paragraph("DÉTAILS DU STAGE", sectionFont);
        detailsTitle.setSpacingBefore(15f);
        detailsTitle.setSpacingAfter(10f);
        document.add(detailsTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        addTableRow(table, "Description", offer.getDescription(), cellBoldFont, cellFont);
        addTableRow(table, "Date de début", entente.getStartDate().format(DATE_FORMATTER), cellBoldFont, cellFont);
        addTableRow(table, "Durée", String.valueOf(entente.getDurationInWeeks()), cellBoldFont, cellFont);


        if (entente.getAddress() != null && !entente.getAddress().isBlank()) {
            addTableRow(table, "Lieu", entente.getAddress(), cellBoldFont, cellFont);
        }

        addTableRow(table, "Rémunération", entente.getRemuneration() + " $", cellBoldFont, cellFont);

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

        Paragraph missionsTitle = new Paragraph("MISSIONS ET OBJECTIFS DU STAGE", sectionFont);
        missionsTitle.setSpacingBefore(15f);
        missionsTitle.setSpacingAfter(10f);
        document.add(missionsTitle);

        Paragraph missions = new Paragraph(entente.getMissionsObjectifs(), normalFont);
        missions.setAlignment(Element.ALIGN_JUSTIFIED);
        missions.setSpacingAfter(15f);
        document.add(missions);
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

        Paragraph footer = new Paragraph(
                "\nCe document constitue une entente officielle entre les parties mentionnées ci-dessus. " +
                        "Toute modification doit faire l'objet d'un avenant signé par toutes les parties.\n\n" +
                        "Document généré le " + LocalDate.now().format(DATE_FORMATTER),
                footerFont
        );
        footer.setAlignment(Element.ALIGN_JUSTIFIED);
        footer.setSpacingBefore(30f);
        document.add(footer);
    }
}