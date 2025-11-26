package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerFormData;
import ca.cal.leandrose.service.dto.evaluation.prof.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PDFGeneratorService {
    private static final String PDF_EXTENSION = ".pdf";
    private static final String DEFAULT_BASE_DIR = "uploads/ententes";
    private static final String DEFAULT_DIR_STAGES = "uploads/evalations";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.entente.base-dir:" + DEFAULT_BASE_DIR + "}")
    private String baseUploadDir;

    @Value("${app.evaluation.base-dir:" + DEFAULT_DIR_STAGES + "}")
    private String baseEvaluationsDir;

    private transient PdfWriter pdfWriter;

    
    public String genererEntentePDF(EntenteStage entente) {
        try {
            Path ententeDir = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(ententeDir)) {
                Files.createDirectories(ententeDir);
            }
            String filename = "entente_" + entente.getId() + "_" + UUID.randomUUID() + PDF_EXTENSION;
            Path targetPath = ententeDir.resolve(filename);
            Document document = new Document(PageSize.A4, 50, 50, 40, 40);
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
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier PDF : {}", cheminFichier, e);
        }
    }

    private void addHeader(Document document) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.GRAY);
        Paragraph header = new Paragraph("COLLÈGE ANDRÉ-LAURENDEAU", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(6f);
        document.add(header);
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph title = new Paragraph("ENTENTE DE STAGE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6f);
        document.add(title);
    }

    private void addPartiesInfo(Document document, EntenteStage entente) throws DocumentException {
        Candidature candidature = entente.getCandidature();
        var student = candidature.getStudent();
        InternshipOffer offer = candidature.getInternshipOffer();
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Paragraph partiesTitle = new Paragraph("LES PARTIES", sectionFont);
        partiesTitle.setSpacingBefore(6f);
        partiesTitle.setSpacingAfter(6f);
        document.add(partiesTitle);
        document.add(new Paragraph("L'ÉTUDIANT :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        document.add(new Paragraph("Nom : " + student.getLastName() + " " + student.getFirstName(), normalFont));
        document.add(new Paragraph("Email : " + student.getCredentials().getEmail(), normalFont));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("L'ENTREPRISE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        document.add(new Paragraph("Nom de l'entreprise : " + offer.getCompanyName(), normalFont));
        document.add(new Paragraph("Personne contact : " + offer.getEmployeurEmail(), normalFont));
        if (entente.getAddress() != null) {
            document.add(new Paragraph("Adresse : " + entente.getAddress(), normalFont));
        }
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("LE COLLÈGE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        document.add(new Paragraph("Collège André-Laurendeau", normalFont));
        document.add(new Paragraph("1111, rue Lapierre, Montréal (Québec) H8N 2J4", normalFont));
        document.add(Chunk.NEWLINE);
    }

    private void addStageDetails(Document document, EntenteStage entente) throws DocumentException {
        Candidature candidature = entente.getCandidature();
        InternshipOffer offer = candidature.getInternshipOffer();
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Paragraph detailsTitle = new Paragraph("DÉTAILS DU STAGE", sectionFont);
        detailsTitle.setSpacingBefore(8f);
        detailsTitle.setSpacingAfter(6f);
        document.add(detailsTitle);
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6f);
        table.setSpacingAfter(6f);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
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
        labelCell.setPadding(2f);
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2f);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addMissionsObjectifs(Document document, EntenteStage entente) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Paragraph missionsTitle = new Paragraph("MISSIONS ET OBJECTIFS DU STAGE", sectionFont);
        missionsTitle.setSpacingBefore(8f);
        missionsTitle.setSpacingAfter(6f);
        document.add(missionsTitle);
        Paragraph missions = new Paragraph(entente.getMissionsObjectifs(), normalFont);
        missions.setAlignment(Element.ALIGN_JUSTIFIED);
        missions.setSpacingAfter(8f);
        document.add(missions);
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        Paragraph footer =
                new Paragraph(
                        "\nCe document constitue une entente officielle entre les parties mentionnées ci-dessus. "
                                + "Toute modification doit faire l'objet d'un avenant signé par toutes les parties.\n\n"
                                + "Document généré le "
                                + LocalDate.now().format(DATE_FORMATTER),
                        footerFont);
        footer.setAlignment(Element.ALIGN_JUSTIFIED);
        footer.setSpacingBefore(10f);
        document.add(footer);
    }

    public String generatedEvaluationByEmployer(EvaluationStagiaire evaluationStagiaire, EvaluationEmployerFormData formData, String language,
                                                String profFirstName, String profLastName,
                                                String nameCollege, String address, String fax_machine ) {
        try {
            Path evaluationDir = Paths.get(baseEvaluationsDir).toAbsolutePath().normalize();
            if (!Files.exists(evaluationDir)) {
                Files.createDirectories(evaluationDir);
            }
            String filename = "evaluation_" + evaluationStagiaire.getId() + "_" + UUID.randomUUID() + PDF_EXTENSION;
            Path targetPath = evaluationDir.resolve(filename);

            Document document = new Document(PageSize.A4, 40, 40, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile()));

            this.pdfWriter = writer;
            document.open();

            addHeaderEvaluation(document, language, nameCollege);
            addTitleEvaluation(document, language);
            addStudentAndCompanyTitle(document, evaluationStagiaire, language);

            addRatingLegendAligned(document, language);

            addGenericEvaluationContent(document, formData, language, getEvaluationCategoriesEmployer(language),
                    true);

            addGeneralComments(document, formData, language);

            
            document.newPage();
            addTraineeEvaluationPage(document, formData, language, profFirstName, profLastName, nameCollege, address, fax_machine);

            addFooterEvaluation(document, language);

            document.close();
            this.pdfWriter = null;
            writer.close();

            log.info("PDF d'évaluation généré avec succès : {}", targetPath);
            return targetPath.toString();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF d'évaluation {}", evaluationStagiaire.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF d'évaluation", e);
        }
    }

    public String generatedEvaluationByTeacher(EvaluationStagiaire evaluationStagiaire,
                                               EvaluationProfFormDto formData,
                                               EvaluationTeacherInfoDto teacherInfo,
                                               String language) {
        try {
            Path targetPath = prepareOutputPath(evaluationStagiaire.getId());
            generateTeacherPdfDocument(targetPath, evaluationStagiaire, formData, teacherInfo, language);

            log.info("PDF d'évaluation du prof généré avec succès : {}", targetPath);
            return targetPath.toString();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF d'évaluation du prof {}", evaluationStagiaire.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF d'évaluation du milieu de stage", e);
        }
    }

    private Path prepareOutputPath(Long evaluationId) throws IOException {
        Path evaluationDir = Paths.get(baseEvaluationsDir).toAbsolutePath().normalize();

        if (!Files.exists(evaluationDir)) {
            Files.createDirectories(evaluationDir);
        }

        String filename = String.format("evaluation_%d_%s%s",
                evaluationId,
                UUID.randomUUID(),
                PDF_EXTENSION
        );

        return evaluationDir.resolve(filename);
    }

    private void generateTeacherPdfDocument(Path targetPath,
                                            EvaluationStagiaire evaluationStagiaire,
                                            EvaluationProfFormDto formData,
                                            EvaluationTeacherInfoDto teacherInfo,
                                            String language) throws DocumentException, IOException {
        Document document = null;
        PdfWriter writer = null;

        try {
            document = new Document(PageSize.A4, 40, 40, 36, 36);
            writer = PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile()));
            this.pdfWriter = writer;

            document.open();

            buildTeacherDocumentContent(document, evaluationStagiaire, formData, teacherInfo, language);

        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            this.pdfWriter = null;
        }
    }

    private void buildTeacherDocumentContent(Document document,
                                             EvaluationStagiaire evaluationStagiaire,
                                             EvaluationProfFormDto formData,
                                             EvaluationTeacherInfoDto teacherInfo,
                                             String language) throws DocumentException {
        
        addHeaderEvaluationParProf(document, language);

        
        addEmployerSection(document, evaluationStagiaire, teacherInfo.entrepriseTeacherDto(), language);
        addStudentSection(document, formData, teacherInfo.studentTeacherDto(), language);

        addRatingLegendAligned(document, language);

        
        addGenericEvaluationContent(
                document,
                formData,
                language,
                getEvaluationCategoriesTeacher(language),
                false
        );

        addObservationsGeneralesSection(document, formData, language);
        
        addTeacherSignature(document, language, teacherInfo.profDto().getFirstName() + " " + teacherInfo.profDto().getLastName(), teacherInfo.studentTeacherDto().fullname());
    }

    private Image createCheckboxImage(boolean checked) {
        try {
            if (this.pdfWriter == null) return null;
            PdfContentByte cb = this.pdfWriter.getDirectContent();
            float size = 10f; 
            PdfTemplate tpl = cb.createTemplate(size, size);
            tpl.setLineWidth(0.7f);
            tpl.rectangle(0.6f, 0.6f, size - 1.2f, size - 1.2f);
            tpl.stroke();
            if (checked) {
                tpl.setLineWidth(1.0f);
                tpl.moveTo(size * 0.18f, size * 0.55f);
                tpl.lineTo(size * 0.45f, size * 0.2f);
                tpl.lineTo(size * 0.85f, size * 0.75f);
                tpl.stroke();
            }
            Image img = Image.getInstance(tpl);
            img.scaleAbsolute(size, size);
            return img;
        } catch (Exception e) {
            log.warn("Erreur createCheckboxImage: {}", e.getMessage());
            return null;
        }
    }

    
    
    
    private void addRatingLegendAligned(Document document, String language) throws DocumentException {
        Font legendTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font legendDescFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, BaseColor.DARK_GRAY);

        Paragraph legendTitle = new Paragraph(
                "en".equals(language) ? "Rating scale (columns correspond to options below)" : "Échelle d'évaluation (colonnes correspondant aux cases)",
                legendTitleFont);
        legendTitle.setSpacingAfter(4f);
        document.add(legendTitle);

        PdfPTable legendTable = new PdfPTable(4);
        legendTable.setWidthPercentage(100);
        legendTable.setSpacingAfter(6f);

        List<String> keys = List.of("EXCELLENT", "TRES_BIEN", "SATISFAISANT", "A_AMELIORER");
        for (String k : keys) {
            String desc = getRatingDisplayText(k, language);
            PdfPCell cell = new PdfPCell(new Phrase(desc, legendDescFont));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4f);
            legendTable.addCell(cell);
        }
        document.add(legendTable);
    }

    private Integer getGlobalAssessmentValue(EvaluationEmployerFormData formData) {
        Object ga = invokeGetter(formData, "globalAssessment", "getGlobalAssessment");
        if (ga == null) return null;
        if (ga instanceof Number) return ((Number) ga).intValue();
        try {
            return Integer.parseInt(String.valueOf(ga));
        } catch (Exception e) {
            return null;
        }
    }

    private String getGlobalAppreciation(EvaluationEmployerFormData formData) {
        Object val = invokeGetter(formData, "globalAppreciation", "getGlobalAppreciation", "generalComment", "getGeneralComment");
        return val != null ? String.valueOf(val) : null;
    }

    private Boolean getDiscussedWithTrainee(EvaluationEmployerFormData formData) {
        Object val = invokeGetter(formData, "discussedWithTrainee", "getDiscussedWithTrainee", "discussed", "isDiscussedWithTrainee");
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        String s = String.valueOf(val);
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s)) return true;
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "0".equals(s)) return false;
        return null;
    }

    private String getSupervisionHours(EvaluationEmployerFormData formData) {
        Object val = invokeGetter(formData, "supervisionHours", "getSupervisionHours");
        return val != null ? String.valueOf(val) : null;
    }

    private String getWelcomeNextInternship(EvaluationEmployerFormData formData) {
        Object val = invokeGetter(formData, "welcomeNextInternship", "getWelcomeNextInternship");
        return val != null ? String.valueOf(val) : null;
    }

    private Boolean getTechnicalTrainingSufficient(EvaluationEmployerFormData formData) {
        Object val = invokeGetter(formData, "technicalTrainingSufficient", "getTechnicalTrainingSufficient", "technicalTraining", "isTechnicalTrainingSufficient");
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        String s = String.valueOf(val);
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s)) return true;
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "0".equals(s)) return false;
        return null;
    }

    private Object invokeGetter(Object obj, String... possibleNames) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();
        for (String name : possibleNames) {
            try {
                Method m = null;
                try {
                    m = cls.getMethod(name);
                } catch (NoSuchMethodException nsme) {
                    String camel = name;
                    if (!name.startsWith("get") && !name.startsWith("is")) {
                        camel = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    }
                    try {
                        m = cls.getMethod(camel);
                    } catch (NoSuchMethodException ex) {
                        String isName = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                        try {
                            m = cls.getMethod(isName);
                        } catch (NoSuchMethodException ex2) {
                        }
                    }
                }
                if (m != null) {
                    m.setAccessible(true);
                    return m.invoke(obj);
                }
            } catch (Exception e) {
                
            }
        }
        return null;
    }


    
    
    
    private void addTraineeEvaluationPage(Document document, EvaluationEmployerFormData formData,
                                          String language, String profFirstName, String profLastName,
                                          String nameCollege, String address, String fax_machine) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);

        Paragraph title = new Paragraph(
                "en".equals(language) ? "OVERALL ASSESSMENT OF THE TRAINEE" : "APPRÉCIATION GLOBALE DU STAGIAIRE",
                titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8f);
        document.add(title);

        
        document.add(new Paragraph("en".equals(language) ? "Overall Assessment:" : "Appréciation globale :", normalFont));
        document.add(Chunk.NEWLINE);

        Integer gaValue = getGlobalAssessmentValue(formData);
        String selectedGlobalKey = getGlobalAssessmentRatingKey(gaValue);
        List<String> globalKeys = List.of("EXCELLENT", "TRES_BIEN", "SATISFAISANT", "A_AMELIORER");

        PdfPTable globalTable = new PdfPTable(globalKeys.size());
        globalTable.setWidthPercentage(100);
        globalTable.setSpacingAfter(6f);

        for (String k : globalKeys) {
            boolean checked = k.equals(selectedGlobalKey);
            Image checkImg = createCheckboxImage(checked);
            PdfPCell c;
            if (checkImg != null) {
                Paragraph p = new Paragraph();
                p.add(new Chunk(checkImg, 0, -2, true));
                c = new PdfPCell(p);
            } else {
                c = new PdfPCell(new Phrase(checked ? "[x]" : "[ ]"));
            }
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(4f);
            globalTable.addCell(c);
        }
        document.add(globalTable);
        document.add(Chunk.NEWLINE);

        
        
        List<String> fullDescriptions = new ArrayList<>();
        for (String k : globalKeys) {
            fullDescriptions.add(getRatingDisplayText(k, language));
        }

        for (int i = 0; i < fullDescriptions.size(); i++) {
            String desc = fullDescriptions.get(i);
            String correspondingKey = globalKeys.get(i);
            boolean isSelected = correspondingKey.equals(selectedGlobalKey);
            Image img = createCheckboxImage(isSelected);
            Phrase p = new Phrase();
            if (img != null) p.add(new Chunk(img, 0, -2, true));
            else p.add(new Chunk(isSelected ? "[x] " : "[ ] "));
            p.add(new Chunk(" "));
            p.add(new Chunk(desc, descFont));
            Paragraph para = new Paragraph();
            para.add(p);
            para.setSpacingAfter(4f);
            document.add(para);
        }

        document.add(Chunk.NEWLINE);

        
        Paragraph appreciationTitle = new Paragraph("en".equals(language) ? "SPECIFY YOUR ASSESSMENT:" : "PRÉCISEZ VOTRE APPRÉCIATION:", sectionFont);
        appreciationTitle.setSpacingAfter(6f);
        document.add(appreciationTitle);
        String globalApp = getGlobalAppreciation(formData);
        if (globalApp != null && !globalApp.isBlank()) {
            Paragraph appreciation = new Paragraph(globalApp, normalFont);
            appreciation.setSpacingAfter(6f);
            document.add(appreciation);
        } else {
            Paragraph emptySpace = new Paragraph("__________________________________________", normalFont);
            emptySpace.setSpacingAfter(6f);
            document.add(emptySpace);
        }
        document.add(Chunk.NEWLINE);

        
        String discussionText = "en".equals(language)
                ? "This evaluation was discussed with the trainee:"
                : "Cette évaluation a été discutée avec le stagiaire :";

        Paragraph discussionTitle = new Paragraph(discussionText, normalFont);
        discussionTitle.setSpacingAfter(5f);
        document.add(discussionTitle);
        Boolean discussed = getDiscussedWithTrainee(formData);
        boolean discussedBool = Boolean.TRUE.equals(discussed);
        Image yesImg = createCheckboxImage(discussedBool);
        Image noImg = createCheckboxImage(!discussedBool);
        Phrase pYes = new Phrase();
        if (yesImg != null) pYes.add(new Chunk(yesImg, 0, -2, true));
        else pYes.add(new Chunk(discussedBool ? "[x]" : "[ ]"));
        pYes.add(new Chunk(" " + ("en".equals(language) ? "Yes" : "Oui"), normalFont));
        Phrase pNo = new Phrase();
        if (noImg != null) pNo.add(new Chunk(noImg, 0, -2, true));
        else pNo.add(new Chunk(!discussedBool ? "[x]" : "[ ]"));
        pNo.add(new Chunk(" " + ("en".equals(language) ? "No" : "Non"), normalFont));
        PdfPTable discussionTable = new PdfPTable(2);
        discussionTable.setWidthPercentage(35);
        discussionTable.setSpacingAfter(6f);
        PdfPCell yesCell = new PdfPCell(pYes); yesCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell noCell = new PdfPCell(pNo); noCell.setBorder(Rectangle.NO_BORDER);
        discussionTable.addCell(yesCell); discussionTable.addCell(noCell);
        document.add(discussionTable);
        document.add(Chunk.NEWLINE);

        
        String hoursText = "en".equals(language)
                ? "Please indicate the actual number of supervision hours per week granted to the trainee:"
                : "Veuillez indiquer le nombre d'heures réel par semaine d'encadrement accordé au stagiaire :";

        Paragraph hoursDesc = new Paragraph(hoursText, normalFont);
        hoursDesc.setSpacingAfter(10f);
        document.add(hoursDesc);
        String hours = getSupervisionHours(formData);
        if (hours != null && !hours.isBlank()) {
            Phrase ph = new Phrase();
            ph.add(new Chunk(" " + hours + " hours", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
            document.add(new Paragraph(ph));
        } else {
            Image emptyHoursImg = createCheckboxImage(false);
            Phrase ph = new Phrase();
            if (emptyHoursImg != null) ph.add(new Chunk(emptyHoursImg, 0, -2, true));
            else ph.add(new Chunk("[ ]"));
            ph.add(new Chunk(" __________________ hours", normalFont));
            document.add(new Paragraph(ph));
        }
        document.add(Chunk.NEWLINE);

        
        String nextInternshipText = "en".equals(language)
                ? "WOULD THE COMPANY LIKE TO WELCOME THIS STUDENT FOR THEIR NEXT INTERNSHIP:"
                : "L'ENTREPRISE AIMERAIT ACCUEILLIR CET ÉLÈVE POUR SON PROCHAIN STAGE :";

        Paragraph nextInternship = new Paragraph(nextInternshipText, sectionFont);
        nextInternship.setSpacingAfter(10f);
        document.add(nextInternship);
        String welcome = getWelcomeNextInternship(formData);
        Image yesW = createCheckboxImage("YES".equalsIgnoreCase(welcome));
        Image noW = createCheckboxImage("NO".equalsIgnoreCase(welcome));
        Image maybeW = createCheckboxImage("MAYBE".equalsIgnoreCase(welcome));

        Phrase py = new Phrase(); if (yesW != null) py.add(new Chunk(yesW,0,-2,true)); else py.add(new Chunk(" "));
        py.add(new Chunk(" " + ("en".equals(language) ? "Yes" : "Oui"), normalFont));
        Phrase pn = new Phrase(); if (noW != null) pn.add(new Chunk(noW,0,-2,true)); else pn.add(new Chunk(" "));
        pn.add(new Chunk(" " + ("en".equals(language) ? "No" : "Non"), normalFont));
        Phrase pm = new Phrase(); if (maybeW != null) pm.add(new Chunk(maybeW,0,-2,true)); else pm.add(new Chunk(" "));
        pm.add(new Chunk(" " + ("en".equals(language) ? "Maybe" : "Peut-être"), normalFont));

        PdfPTable welcomeTable = new PdfPTable(3);
        welcomeTable.setWidthPercentage(45);
        welcomeTable.setSpacingAfter(6f);
        PdfPCell cw1 = new PdfPCell(py); cw1.setBorder(Rectangle.NO_BORDER);
        PdfPCell cw2 = new PdfPCell(pn); cw2.setBorder(Rectangle.NO_BORDER);
        PdfPCell cw3 = new PdfPCell(pm); cw3.setBorder(Rectangle.NO_BORDER);
        welcomeTable.addCell(cw1); welcomeTable.addCell(cw2); welcomeTable.addCell(cw3);
        document.add(welcomeTable);
        document.add(Chunk.NEWLINE);

        
        String trainingText = "en".equals(language)
                ? "Was the trainee's technical training sufficient to accomplish the internship mandate?"
                : "La formation technique du stagiaire était-elle suffisante pour accomplir le mandat de stage?";

        Paragraph trainingQuestion = new Paragraph(trainingText, normalFont);
        trainingQuestion.setSpacingAfter(10f);
        document.add(trainingQuestion);
        Boolean trainingSufficient = getTechnicalTrainingSufficient(formData);
        boolean ts = Boolean.TRUE.equals(trainingSufficient);
        Image yesT = createCheckboxImage(ts);
        Image noT = createCheckboxImage(!ts);
        Phrase pYesT = new Phrase(); if (yesT != null) pYesT.add(new Chunk(yesT,0,-2,true)); else pYesT.add(new Chunk("[ ]"));
        pYesT.add(new Chunk(" " + ("en".equals(language) ? "Yes" : "Oui"), normalFont));
        Phrase pNoT = new Phrase(); if (noT != null) pNoT.add(new Chunk(noT,0,-2,true)); else pNoT.add(new Chunk("[ ]"));
        pNoT.add(new Chunk(" " + ("en".equals(language) ? "No" : "Non"), normalFont));
        PdfPTable trainingTable = new PdfPTable(2);
        trainingTable.setWidthPercentage(30);
        trainingTable.setSpacingAfter(6f);
        PdfPCell ct1 = new PdfPCell(pYesT); ct1.setBorder(Rectangle.NO_BORDER);
        PdfPCell ct2 = new PdfPCell(pNoT); ct2.setBorder(Rectangle.NO_BORDER);
        trainingTable.addCell(ct1); trainingTable.addCell(ct2);
        document.add(trainingTable);
        document.add(Chunk.NEWLINE);

        Paragraph name = new Paragraph(("en".equals(language) ? "Name: " : "Nom : ") + "____________________    " + ( "en".equals(language) ? "Function: " : "Fonction : ") + "____________________", normalFont);
        name.setSpacingAfter(6f);
        document.add(name);
        Paragraph signature = new Paragraph(("en".equals(language) ? "Signature: " : "Signature : ") + "____________________    " + ( "en".equals(language) ? "Date: " : "Date : ") + LocalDate.now().format(DATE_FORMATTER), normalFont);
        signature.setSpacingAfter(6f);
        document.add(signature);

        Paragraph returnTitle = new Paragraph("en".equals(language) ? "Please return this form to:" : "Veuillez retourner ce formulaire à :", sectionFont);
        returnTitle.setSpacingAfter(4f);
        document.add(returnTitle);

        String professorName = profFirstName + " " + profLastName;
        String addressText = professorName + "\n" + nameCollege + "\n" + address + "\n" + fax_machine;
        Paragraph addr = new Paragraph(addressText, normalFont);
        addr.setSpacingAfter(4f);
        document.add(addr);
    }

    private String getRatingDisplayText(String rating, String language) {
        if ("en".equals(language)) {
            return switch (rating) {
                case "EXCELLENT" -> "Totally agree / Skills far exceed expectations";
                case "TRES_BIEN" -> "Mostly agree / Skills exceed expectations";
                case "SATISFAISANT" -> "Mostly disagree / Skills meet expectations";
                case "A_AMELIORER" -> "Totally disagree / Skills do not meet expectations";
                default -> "N/A";
            };
        } else {
            return switch (rating) {
                case "EXCELLENT" -> "Totalement d'accord / Les habiletés dépassent beaucoup les attentes";
                case "TRES_BIEN" -> "Plutôt d'accord / Les habiletés dépassent les attentes";
                case "SATISFAISANT" -> "Plutôt en désaccord / Les habiletés répondent aux attentes";
                case "A_AMELIORER" -> "Totalement en désaccord / Les habiletés ne répondent pas aux attentes";
                default -> "N/A";
            };
        }
    }

    private Map<String, CategoryData> getEvaluationCategoriesEmployer(String language) {
        if ("en".equals(language)) {
            return Map.of(
                    "productivity",
                    new CategoryData("PRODUCTIVITY",
                            "Ability to optimize work performance",
                            List.of("Plan and organize work effectively",
                                    "Quickly understand work instructions",
                                    "Maintain a steady work pace", "Establish priorities",
                                    "Meet deadlines")),
                    "quality", new CategoryData("WORK QUALITY",
                            "Ability to produce careful and precise work",
                            List.of("Demonstrate rigor in work",
                                    "Respect company quality standards",
                                    "Work autonomously in verifying own work",
                                    "Seek opportunities for improvement",
                                    "To conduct a thorough analysis of the problems encountered")),
                    "relationships", new CategoryData("QUALITY OF INTERPERSONAL RELATIONSHIP",
                            "Ability to establish harmonious interactions in the workplace",
                            List.of("Easily establish connections with people",
                                    "Actively contribute to teamwork",
                                    "Adapt easily to the company culture",
                                    "Accept constructive criticism", "Be respectful toward people",
                                    "Demonstrate active listening by trying to understand the " +
                                            "other person's point of view")),
                    "skills", new CategoryData("PERSONAL SKILLS",
                            "Ability to demonstrate mature and responsible attitudes or behaviors",
                            List.of("Demonstrate interest and motivation at work",
                                    "Express ideas clearly", "Show initiative",
                                    "Work safely",
                                    "Demonstrate a good sense of responsibility requiring minimal supervision",
                                    "To be punctual and diligent at work"))
            );
        } else {
            return Map.of(
                    "productivity", new CategoryData("PRODUCTIVITÉ",
                            "Capacité d'optimiser son rendement au travail",
                            List.of("Planifier et organiser son travail de façon efficace",
                                    "Comprendre rapidement les directives relatives à son travail",
                                    "Maintenir un rythme de travail soutenu",
                                    "Établir ses priorités",
                                    "Respecter ses échéanciers")),
                    "quality", new CategoryData("QUALITÉ DU TRAVAIL",
                            "Capacité de produire un travail soigné et précis",
                            List.of("Démontrer de la rigueur dans son travail",
                                    "Respecter les normes de qualité de l'entreprise",
                                    "Faire preuve d'autonomie dans la vérification de son travail",
                                    "Rechercher des occasions d'amélioration",
                                    "Mener une analyse approfondie des problèmes rencontrés")),
                    "relationships", new CategoryData("QUALITÉS DES RELATIONS INTERPERSONNELLES",
                            "Capacité d’établir des interrelations harmonieuses dans son milieu de travail",
                            List.of("établir facilement des contacts avec les gens",
                                    "contribuer activement au travail d’équipe",
                                    "s’adapter facilement à la culture de l’entreprise",
                                    "accepter les critiques constructives",
                                    "être respectueux envers les gens",
                                    "faire preuve d’écoute active en essayant de comprendre le point de vue de l’autre")),
                    "skills", new CategoryData("HABILETÉS PERSONNELLES",
                            "Capacité de faire preuve d’attitudes ou de comportements matures et responsables",
                            List.of("démontrer de l’intérêt et de la motivation au travail",
                                    "exprimer clairement ses idées",
                                    "faire preuve d’initiative",
                                    "travailler de façon sécuritaire",
                                    "démontrer un bon sens des responsabilités ne requérant qu’un minimum de supervision",
                                    "être ponctuel et assidu à son travail"))
            );
        }
    }
    private Map<String, CategoryData> getEvaluationCategoriesTeacher(String language){
        if ("en".equals(language)){
            return Map.of(
                    "conformity", new CategoryData(
                            "TASKS & INTEGRATION",
                            "",
                            List.of(
                                    "The tasks assigned to the intern are in accordance with " +
                                            "the tasks announced in the internship agreement.",
                                    "Reception measures facilitate the integration of the new intern.",
                                    "The actual time spent supervising the intern is sufficient."
                            )
                    ),
                    "environment", new CategoryData(
                            "WORK ENVIRONEMENT",
                            "",
                            List.of(
                                    "The work environment complies with hygiene and safety standards.",
                                    "The work climate is pleasant."
                            )
                    ),
                    "general", new CategoryData(
                            "WORKING CONDITIONS",
                            "",
                            List.of(
                                    "The internship location is accessible by public transport.",
                                    "The salary offered is acceptable.",
                                    "Communication with the supervisor supports the internship.",
                                    "The equipment provided is adequate.",
                                    "The workload is acceptable."
                            )
                    )
            );
        }
        else {
            return Map.of(
                    "conformity", new CategoryData(
                            "ÉVALUATION",
                            "",
                            List.of(
                                    "Les tâches confiées au stagiaire sont conformes à l’entente de stage.",
                                    "Des mesures d’accueil facilitent l’intégration du nouveau stagiaire.",
                                    "Le temps réel consacré à l’encadrement du stagiaire est suffisant."
                            )
                    ),
                    "environment", new CategoryData(
                            "ENVIRONNEMENT DE TRAVAIL",
                            "",
                            List.of(
                                    "L’environnement de travail respecte les normes d’hygiène et de sécurité.",
                                    "Le climat de travail est agréable."
                            )
                    ),
                    "general", new CategoryData(
                            "CONDITIONS DE TRAVAIL",
                            "",
                            List.of(
                                    "Le milieu de stage est accessible en transport en commun.",
                                    "Le salaire offert est intéressant.",
                                    "La communication avec le superviseur facilite le stage.",
                                    "L’équipement fourni est adéquat.",
                                    "Le volume de travail est acceptable."
                            )
                    )
            );
        }
    }

    private void addHeaderEvaluation(Document document, String language, String name_college) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.GRAY);
        String headerText = "en".equals(language) ? name_college + " - Work-Study Program" : name_college + " - Alternance travail-études";
        Paragraph header = new Paragraph(headerText, headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(6f);
        document.add(header);
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }
    private void  addHeaderEvaluationParProf(Document document, String language) throws DocumentException{
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
        String headerText = "en".equals(language) ? "EVALUATION OF THE INTERNSHIP ENVIRONMENT": "ÉVALUATION DU MILIEU DE STAGE";
        Paragraph header = new Paragraph(headerText, headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(6);
        document.add(header);

        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addTitleEvaluation(Document document, String language) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        String titleText = "en".equals(language) ? "Intern Evaluation Form" : "Fiche d'évaluation du stagiaire";
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6f);
        document.add(title);
    }

    private void addStudentAndCompanyTitle(Document document, EvaluationStagiaire evaluation, String language) throws DocumentException {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(6f);
        infoTable.setSpacingAfter(6f);

        addInfoRow(infoTable, getTranslation("studentName", language), evaluation.getStudent().getFirstName() + " " + evaluation.getStudent().getLastName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("program", language), translateProgram(evaluation.getStudent().getProgram(), language), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("company", language), evaluation.getInternshipOffer().getCompanyName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("supervisor", language), evaluation.getEmployeur().getFirstName() + " " + evaluation.getEmployeur().getLastName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("position", language), evaluation.getEmployeur().getCompanyName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("evaluationDate", language), evaluation.getDateEvaluation().format(DATE_FORMATTER), boldFont, normalFont);
        document.add(infoTable);
    }
    private void addEmployerSection(Document document, EvaluationStagiaire evaluationStagiaire,EntrepriseTeacherDto dto, String language) throws DocumentException{
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        String titleText = "en".equals(language)
                ? "IDENTIFICATION OF THE COMPANY"
                : "IDENTIFICATION DE L'ENTREPRISE";
        Paragraph sectionTitle = new Paragraph(titleText, sectionFont);
        sectionTitle.setSpacingBefore(10f);
        sectionTitle.setSpacingAfter(6f);
        document.add(sectionTitle);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{3f, 7f});
        infoTable.setSpacingAfter(8f);



        addInfoRow(infoTable, getTranslation("company", language), dto.companyName(), labelFont, valueFont);
        addInfoRow(infoTable, getTranslation("contactPerson", language), dto.contactName(), labelFont, valueFont);
        addInfoRow(infoTable, getTranslation("address", language), dto.address(), labelFont, valueFont);
        addInfoRow(infoTable, getTranslation("email", language), dto.email(), labelFont, valueFont);
        document.add(infoTable);

    }
    private void addStudentSection(Document document, EvaluationProfFormDto formData,  StudentTeacherDto dto, String language) throws DocumentException{
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        String titleText = "en".equals(language)
                ? "IDENTIFICATION OF THE INTERN"
                : "IDENTIFICATION DU STAGIAIRE";
        Paragraph sectionTitle = new Paragraph(titleText, sectionFont);
        sectionTitle.setSpacingBefore(10f);
        sectionTitle.setSpacingAfter(6f);
        document.add(sectionTitle);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{3f, 7f});
        infoTable.setSpacingAfter(8f);

        addInfoRow(infoTable, getTranslation("studentName", language), dto.fullname(), labelFont, valueFont);
        addInfoRow(infoTable, getTranslation("internshipDate", language), dto.internshipStartDate().toString(), labelFont, valueFont);

        Integer preferredStage = null;

        try{
            preferredStage = formData.preferredStage()!= null
                    ? Integer.parseInt(String.valueOf(formData.preferredStage()))
                    : null;
        } catch(Exception ignored){}


        System.out.println("Stage " + preferredStage);


        String stageText = "en".equals(language)
                ? "Internship: "
                : "Stage: ";

        addInfoRow(infoTable, stageText, String.valueOf(preferredStage), labelFont, valueFont);
        document.add(infoTable);
    }

    private void addObservationsGeneralesSection(Document document, EvaluationProfFormDto formData, String language)
            throws DocumentException {

        Font sectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph p = new Paragraph(
                t("OBSERVATIONS GÉNÉRALES", "GENERAL OBSERVATIONS", language),
                sectionTitle);
        p.setSpacingBefore(10f);
        p.setSpacingAfter(6f);
        document.add(p);

        
        
        
        PdfPTable table = new PdfPTable(1);   
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);

        
        PdfPTable block1 = new PdfPTable(2);
        block1.setWidthPercentage(100);
        block1.addCell(makeLabel(
                "Ce milieu est à privilégier pour le :",
                "This environment should be prioritized for :",
                language, labelFont));
        block1.addCell(makeChoiceRow(
                formData.preferredStage() != null && formData.preferredStage() == 1,
                formData.preferredStage() != null && formData.preferredStage() == 2,
                language, labelFont));

        table.addCell(makeContainerCell(block1));

        
        PdfPTable block2 = new PdfPTable(2);
        block2.setWidthPercentage(100);

        block2.addCell(makeLabel(
                "Ce milieu est ouvert à accueillir",
                "This environment is able to welcome",
                language, labelFont));

        PdfPTable capacityChoices = new PdfPTable(1);
        capacityChoices.setWidthPercentage(100);

        capacityChoices.addCell(makeCheckboxLine("un stagiaire", "one intern", formData.capacity() == 1, language, labelFont));
        capacityChoices.addCell(makeCheckboxLine("deux stagiaires", "two interns", formData.capacity() == 2, language, labelFont));
        capacityChoices.addCell(makeCheckboxLine("trois stagiaires", "three interns", formData.capacity() == 3, language, labelFont));
        capacityChoices.addCell(makeCheckboxLine("plus de trois", "more than three", formData.capacity() == 4, language, labelFont));

        PdfPCell capacityCell = new PdfPCell(capacityChoices);
        capacityCell.setBorder(Rectangle.NO_BORDER);
        capacityCell.setPadding(4f);

        block2.addCell(capacityCell);

        
        PdfPCell block2Container = new PdfPCell(block2);
        block2Container.setPadding(4f);
        block2Container.setBorder(Rectangle.BOX);
        table.addCell(block2Container);

        document.add(table);

        Paragraph sameLabel = new Paragraph(
                t("Ce milieu désire accueillir le même stagiaire pour un prochain stage",
                        "This environment wishes to welcome the same intern for a future internship",
                        language),
                labelFont);
        sameLabel.setSpacingBefore(12f);
        document.add(sameLabel);

        boolean same = Boolean.TRUE.equals(formData.sameTraineeNextStage());
        Paragraph sameValue = new Paragraph(same ? "Oui" : "Non", valueFont);
        sameValue.setIndentationLeft(15f);
        sameValue.setSpacingAfter(8f);
        document.add(sameValue);

        
        Paragraph shiftLabel = new Paragraph(
                t("Ce milieu offre des quarts de travail variables :",
                        "This environment offers variable work shifts:",
                        language),
                labelFont);
        shiftLabel.setSpacingBefore(8f);
        document.add(shiftLabel);

        boolean hasShift = Boolean.TRUE.equals(formData.workShiftYesNo());
        Paragraph shiftValue = new Paragraph(hasShift ? "Oui" : "Non", valueFont);
        shiftValue.setIndentationLeft(15f);
        document.add(shiftValue);

        
        if (hasShift) {
            for (int i = 0; i < 3; i++) {
                WorkShiftRange range =
                        (formData.workShifts() != null && formData.workShifts().size() > i)
                                ? formData.workShifts().get(i)
                                : new WorkShiftRange("____", "____");

                Paragraph rangeP = new Paragraph(
                        t("De " + range.from() + " à " + range.to(),
                                "From " + range.from() + " to " + range.to(),
                                language),
                        valueFont);
                rangeP.setIndentationLeft(25f);
                document.add(rangeP);
            }
        }
    }



    private PdfPCell makeLabel(String fr, String en, String language, Font font){
        PdfPCell cell = new PdfPCell(new Phrase(t(fr, en, language), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        return cell;
    }
    private PdfPCell makeContainerCell(PdfPTable table){
        PdfPCell cell = new PdfPCell(table);
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(4f);
        return cell;
    }
    private PdfPCell makeCheckboxLine(String fr, String en, boolean checked, String language, Font font){
        String box = checked ? "[X] " : "[ ] ";
        PdfPCell cell = new PdfPCell(new Phrase(box + t(fr, en, language), font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell makeChoiceRow(
            boolean option1, boolean option2,
            String language,
            Font font
    ){
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(makeCheckboxLine("premier stage", "first internship", option1, language, font));
        table.addCell(makeCheckboxLine("deuxième stage", "second internship", option2, language, font));

        PdfPCell wrap = new PdfPCell(table);
        wrap.setBorder(Rectangle.NO_BORDER);
        return wrap;
    }

    private String t(String fr, String en, String language){
        return "en".equals(language) ? en: fr;
    }

    private void addTeacherSignature(Document document, String language, String teacherName, String studentName) throws DocumentException {
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DATE_FORMATTER);

        PdfPTable table = new PdfPTable(new float[]{2f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(25f);

        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);

        Paragraph signatureLabel = new Paragraph(
                t("Signature de l'enseignant responsable du stagiaire", "Signature of the teacher responsible for the intern", language),
                boldFont
        );
        signatureLabel.setSpacingAfter(2f);

        Paragraph teacherSignature = new Paragraph(teacherName, italicFont);
        teacherSignature.setSpacingAfter(6f);

        signatureCell.addElement(signatureLabel);
        signatureCell.addElement(teacherSignature);

        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);

        Paragraph dateLabel = new Paragraph(
                t("Date", "Date", language),
                boldFont
        );
        dateLabel.setSpacingAfter(2f);

        Paragraph dateText = new Paragraph(formattedDate, italicFont);

        dateCell.addElement(dateLabel);
        dateCell.addElement(dateText);

        table.addCell(signatureCell);
        table.addCell(dateCell);

        PdfPTable tableStudent = new PdfPTable(new float[]{2f, 2f});
        tableStudent.setWidthPercentage(100);
        tableStudent.setSpacingBefore(25f);

        PdfPCell studentSignatureCell = new PdfPCell();
        studentSignatureCell.setBorder(Rectangle.NO_BORDER);

        Paragraph studentSignatureLabel = new Paragraph(
                t("Signature de l'étudiant", "Student Signature", language),
                boldFont
        );
        studentSignatureLabel.setSpacingAfter(2f);

        Paragraph studentSignature = new Paragraph(studentName, italicFont);
        studentSignature.setSpacingAfter(6f);

        studentSignatureCell.addElement(studentSignatureLabel);
        studentSignatureCell.addElement(studentSignature);

        PdfPCell studentDateCell = new PdfPCell();
        studentDateCell.setBorder(Rectangle.NO_BORDER);

        Paragraph studentDateLabel = new Paragraph(
                t("Date", "Date", language),
                boldFont
        );
        studentDateLabel.setSpacingAfter(2f);

        Paragraph studentDateText = new Paragraph(formattedDate, italicFont);

        studentDateCell.addElement(studentDateLabel);
        studentDateCell.addElement(studentDateText);

        tableStudent.addCell(studentSignatureCell);
        tableStudent.addCell(studentDateCell);


        document.add(table);
        document.add(tableStudent);
    }


    private void addGeneralComments(Document document, EvaluationEmployerFormData formData, String language) throws DocumentException {
        Object general = invokeGetter(formData, "generalComment", "getGeneralComment", "globalAppreciation", "getGlobalAppreciation");
        if (general != null) {
            String commentText = String.valueOf(general);
            if (!commentText.isBlank()) {
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
                Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
                String commentaireText = "en".equals(language) ? "General Comments" : "Commentaire généreaux";
                Paragraph commentsTitle = new Paragraph(commentaireText, sectionFont);
                commentsTitle.setSpacingBefore(6f);
                commentsTitle.setSpacingAfter(4f);
                document.add(commentsTitle);
                Paragraph comments = new Paragraph(commentText, commentFont);
                comments.setAlignment(Element.ALIGN_JUSTIFIED);
                comments.setSpacingAfter(6f);
                document.add(comments);
            }
        }
    }
    private List<String> getRatingLabels(String language) {
        return "en".equals(language)
                ? List.of("Strongly Agree", "Agree", "Disagree", "Strongly Disagree", "Impossible to say")
                : List.of("Totalement en accord", "Plutôt en accord", "Plutôt en désaccord", "Totalement en désaccord", "Impossible de se prononcer");
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell left = new PdfPCell(new Phrase(label == null ? "" : label, labelFont));
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(6f);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell right = new PdfPCell(new Phrase(value == null || value.isBlank() ? "-" : value, valueFont));
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(6f);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(left);
        table.addCell(right);
    }



    private void addFooterEvaluation(Document document, String language) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        String naText = "en".equals(language) ? "* N/A = not applicable" : "* N/A = non applicable";
        String generatedText = "en".equals(language) ? "Document generated on " : "Document généré le ";
        Paragraph footer = new Paragraph("\n" + naText + "\n\n" + generatedText + LocalDate.now().format(DATE_FORMATTER), footerFont);
        footer.setAlignment(Element.ALIGN_LEFT);
        footer.setSpacingBefore(8f);
        document.add(footer);
    }

    private String getTranslation(String key, String language) {
        Map<String, String> translations = Map.ofEntries(
                Map.entry("studentName", "en".equals(language) ? "Student Name:" : "Nom de l'élève:"),
                Map.entry("program", "en".equals(language) ? "Student Name:" : "Nom de l'élève:"),
                Map.entry("company", "en".equals(language) ? "Company Name:" : "Nom de l'entreprise:"),
                Map.entry("supervisor", "en".equals(language) ? "Supervisor:" : "Nom du superviseur:"),
                Map.entry("position", "en".equals(language) ? "Position:" : "Fonction:"),
                Map.entry("evaluationDate", "en".equals(language) ? "Evaluation Date:" : "Date d'évaluation:"),
                Map.entry("contactPerson", "en".equals(language) ? "Contact Person:": "Personne contact:"),
                Map.entry("address", "en".equals(language) ? "Address:": "Adresse:"),
                Map.entry("email", "en".equals(language) ? "Email:": "Courriel:"),
                Map.entry("internName", "en".equals(language) ? "Name of the Intern": "Nom du stagiaire"),
                Map.entry("internshipDate", "en".equals(language) ? "Internship Date": "Date du stage")

        );
        return translations.getOrDefault(key, key);
    }
    private String translateProgram(String programKey, String language) {
        Map<String, String> programTranslations = Map.ofEntries(
                Map.entry(Program.COMPUTER_SCIENCE.getTranslationKey(), "en".equals(language) ? "Computer Science" : "Informatique"),
                Map.entry(Program.SOFTWARE_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Software Engineering" : "Génie logiciel"),
                Map.entry(Program.INFORMATION_TECHNOLOGY.getTranslationKey(), "en".equals(language) ? "Information Technology" : "Technologie de l'information"),
                Map.entry(Program.DATA_SCIENCE.getTranslationKey(), "en".equals(language) ? "Data Science" : "Science des données"),
                Map.entry(Program.CYBER_SECURITY.getTranslationKey(), "en".equals(language) ? "Cyber Security" : "Cybersécurité"),
                Map.entry(Program.ARTIFICIAL_INTELLIGENCE.getTranslationKey(), "en".equals(language) ? "Artificial Intelligence" : "Intelligence artificielle"),
                Map.entry(Program.ELECTRICAL_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Electrical Engineering" : "Génie électrique"),
                Map.entry(Program.MECHANICAL_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Mechanical Engineering" : "Génie mécanique"),
                Map.entry(Program.CIVIL_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Civil Engineering" : "Génie civil"),
                Map.entry(Program.CHEMICAL_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Chemical Engineering" : "Génie chimique"),
                Map.entry(Program.BIOMEDICAL_ENGINEERING.getTranslationKey(), "en".equals(language) ? "Biomedical Engineering" : "Génie biomédical"),
                Map.entry(Program.BUSINESS_ADMINISTRATION.getTranslationKey(), "en".equals(language) ? "Business Administration" : "Administration des affaires"),
                Map.entry(Program.ACCOUNTING.getTranslationKey(), "en".equals(language) ? "Accounting" : "Comptabilité"),
                Map.entry(Program.FINANCE.getTranslationKey(), "Finance"),
                Map.entry(Program.ECONOMICS.getTranslationKey(), "en".equals(language) ? "Economics" : "Économie"),
                Map.entry(Program.MARKETING.getTranslationKey(), "Marketing"),
                Map.entry(Program.MANAGEMENT.getTranslationKey(), "en".equals(language) ? "Management" : "Gestion"),
                Map.entry(Program.PSYCHOLOGY.getTranslationKey(), "en".equals(language) ? "Psychology" : "Psychologie"),
                Map.entry(Program.SOCIOLOGY.getTranslationKey(), "en".equals(language) ? "Sociology" : "Sociologie"),
                Map.entry(Program.POLITICAL_SCIENCE.getTranslationKey(), "en".equals(language) ? "Political Science" : "Science politique"),
                Map.entry(Program.INTERNATIONAL_RELATIONS.getTranslationKey(), "en".equals(language) ? "International Relations" : "Relations internationales"),
                Map.entry(Program.LAW.getTranslationKey(), "en".equals(language) ? "Law" : "Droit"),
                Map.entry(Program.EDUCATION.getTranslationKey(), "en".equals(language) ? "Education" : "Éducation"),
                Map.entry(Program.LITERATURE.getTranslationKey(), "en".equals(language) ? "Literature" : "Littérature"),
                Map.entry(Program.HISTORY.getTranslationKey(), "en".equals(language) ? "History" : "Histoire"),
                Map.entry(Program.PHILOSOPHY.getTranslationKey(), "en".equals(language) ? "Philosophy" : "Philosophie"),
                Map.entry(Program.LINGUISTICS.getTranslationKey(), "en".equals(language) ? "Linguistics" : "Linguistique"),
                Map.entry(Program.BIOLOGY.getTranslationKey(), "en".equals(language) ? "Biology" : "Biologie"),
                Map.entry(Program.CHEMISTRY.getTranslationKey(), "en".equals(language) ? "Chemistry" : "Chimie"),
                Map.entry(Program.PHYSICS.getTranslationKey(), "en".equals(language) ? "Physics" : "Physique"),
                Map.entry(Program.MATHEMATICS.getTranslationKey(), "en".equals(language) ? "Mathematics" : "Mathématiques"),
                Map.entry(Program.STATISTICS.getTranslationKey(), "en".equals(language) ? "Statistics" : "Statistiques"),
                Map.entry(Program.ENVIRONMENTAL_SCIENCE.getTranslationKey(), "en".equals(language) ? "Environmental Science" : "Science environnementale"),
                Map.entry(Program.MEDICINE.getTranslationKey(), "en".equals(language) ? "Medicine" : "Médecine"),
                Map.entry(Program.NURSING.getTranslationKey(), "en".equals(language) ? "Nursing" : "Soins infirmiers"),
                Map.entry(Program.PHARMACY.getTranslationKey(), "en".equals(language) ? "Pharmacy" : "Pharmacie"),
                Map.entry(Program.DENTISTRY.getTranslationKey(), "en".equals(language) ? "Dentistry" : "Dentisterie"),
                Map.entry(Program.ARCHITECTURE.getTranslationKey(), "Architecture"),
                Map.entry(Program.FINE_ARTS.getTranslationKey(), "en".equals(language) ? "Fine Arts" : "Beaux-arts"),
                Map.entry(Program.MUSIC.getTranslationKey(), "en".equals(language) ? "Music" : "Musique"),
                Map.entry(Program.THEATER.getTranslationKey(), "en".equals(language) ? "Theater" : "Théâtre"),
                Map.entry(Program.FILM_STUDIES.getTranslationKey(), "en".equals(language) ? "Film Studies" : "Études cinématographiques"),
                Map.entry(Program.COMMUNICATION.getTranslationKey(), "Communication"),
                Map.entry(Program.JOURNALISM.getTranslationKey(), "en".equals(language) ? "Journalism" : "Journalisme"),
                Map.entry(Program.DESIGN.getTranslationKey(), "Design"),
                Map.entry(Program.ANTHROPOLOGY.getTranslationKey(), "en".equals(language) ? "Anthropology" : "Anthropologie"),
                Map.entry(Program.GEOGRAPHY.getTranslationKey(), "en".equals(language) ? "Geography" : "Géographie"),
                Map.entry(Program.SPORTS_SCIENCE.getTranslationKey(), "en".equals(language) ? "Sports Science" : "Science du sport")
        );
        return programTranslations.getOrDefault(programKey, programKey);
    }

    private String getGlobalAssessmentRatingKey(Integer globalAssessment) {
        if (globalAssessment == null) return null;
        return switch (globalAssessment) {
            case 0 -> "EXCELLENT";
            case 1 -> "TRES_BIEN";
            case 3 -> "SATISFAISANT";
            case 4 -> "A_AMELIORER";
            default -> null;
        };
    }
    private String getSafeLabel(List<String> labels, int idx) {
        if (labels == null || idx < 0 || idx >= labels.size()) return "";
        return labels.get(idx);
    }

   private void addGenericEvaluationContent(
           Document document,
           EvaluationForm formData,
           String language,
           Map<String, CategoryData> categories,
           boolean includesComments

   ) throws DocumentException{
       Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
       Font questionFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
       Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, BaseColor.DARK_GRAY);

       List<String> ratingKeys = List.of("EXCELLENT", "TRES_BIEN", "SATISFAISANT", "A_AMELIORER");
       List<String> ratingLabels = getRatingLabels(language);

       for (Map.Entry<String, CategoryData> entry : categories.entrySet()) {
           String categoryKey = entry.getKey();
           CategoryData categoryData = entry.getValue();

           List<? extends IQuestionResponse> responses = formData.getCategories() != null
                   ? formData.getCategories().get(categoryKey)
                   : null;

           Paragraph categoryTitle = new Paragraph(categoryData.getTitle(), categoryFont);
           categoryTitle.setSpacingBefore(6f);
           categoryTitle.setSpacingAfter(4f);
           document.add(categoryTitle);

           if (categoryData.getDescription() != null && !categoryData.getDescription().isBlank()) {
               Paragraph categoryDesc = new Paragraph(categoryData.getDescription(), questionFont);
               categoryDesc.setSpacingAfter(6f);
               document.add(categoryDesc);
           }

           
           List<String> questions = categoryData.getQuestions();
           for (int i = 0; i < questions.size(); i++) {
               String question = questions.get(i);
               document.add(new Paragraph((i + 1) + ". " + question, questionFont));
               document.add(Chunk.NEWLINE);

               String selectedRating = getQuestionRating(formData, categoryKey, i);

               PdfPTable ratingOptionsTable = new PdfPTable(ratingKeys.size());
               ratingOptionsTable.setWidthPercentage(100);
               ratingOptionsTable.setSpacingAfter(4f);

               for (int k = 0; k < ratingKeys.size(); k++) {
                   String key = ratingKeys.get(k);
                   boolean isSelected = key.equals(selectedRating);

                   Image checkboxImg = createCheckboxImage(isSelected);
                   PdfPCell cell;
                   if (checkboxImg != null) {
                       Paragraph p = new Paragraph();
                       p.add(new Chunk(checkboxImg, 0, -2, true));
                       cell = new PdfPCell(p);
                   } else {
                       String checkbox = isSelected ? "☑" : "☐";
                       Paragraph p = new Paragraph(checkbox + " " + getSafeLabel(ratingLabels, k), questionFont);
                       cell = new PdfPCell(p);
                   }

                   cell.setBorder(Rectangle.NO_BORDER);
                   cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                   cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                   cell.setPadding(6f);
                   ratingOptionsTable.addCell(cell);
               }

               document.add(ratingOptionsTable);

               if (includesComments) {
                   String comment = getQuestionComment(formData, categoryKey, i);
                   if (comment != null && !comment.isBlank()) {
                       String commentLabel = "en".equals(language) ? "Comment: " : "Commentaire: ";
                       Paragraph commentPara = new Paragraph(commentLabel + comment, commentFont);
                       commentPara.setIndentationLeft(12f);
                       commentPara.setSpacingAfter(4f);
                       document.add(commentPara);
                   }
               }

               document.add(Chunk.NEWLINE);
           }

           document.newPage();
       }
   }

    private String getQuestionRating(EvaluationForm formData, String categoryKey, int index) {
        if (formData.getCategories() == null) return null;

        List<? extends IQuestionResponse> responses = formData.getCategories().get(categoryKey);
        if (responses != null && index < responses.size()) {
            IQuestionResponse response = responses.get(index);
            return response.getRating();
        }
        return null;
    }

    private String getQuestionComment(EvaluationForm formData, String categoryKey, int index) {
        if (formData.getCategories() == null) return null;

        List<? extends IQuestionResponse> responses = formData.getCategories().get(categoryKey);
        if (responses != null && index < responses.size()) {
            IQuestionResponse response = responses.get(index);
            return response.getComment();
        }
        return null;
    }

}