package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.service.dto.evaluation.CategoryData;
import ca.cal.leandrose.service.dto.evaluation.EvaluationFormData;
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
        addTableRow(table, "Durée", entente.getDurationInWeeks() + " semaine(s)", cellBoldFont, cellFont);
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

    public String genererEvaluationPdf(EvaluationStagiaire evaluationStagiaire, EvaluationFormData formData, String language,
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

            addEvaluationContent(document, formData, language);

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

    private Image createCheckboxImage(boolean checked) {
        try {
            if (this.pdfWriter == null) return null;
            PdfContentByte cb = this.pdfWriter.getDirectContent();
            float size = 10f; // compact
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

    @SuppressWarnings("unchecked")
    private Map<String, List<Object>> getCategoriesMap(EvaluationFormData formData) {
        if (formData == null) return Collections.emptyMap();
        Object categoriesObj = invokeGetter(formData, "categories", "getCategories");
        if (categoriesObj instanceof Map) {
            try {
                return (Map<String, List<Object>>) categoriesObj;
            } catch (ClassCastException e) {
                Map<?, ?> raw = (Map<?, ?>) categoriesObj;
                Map<String, List<Object>> out = new LinkedHashMap<>();
                for (Map.Entry<?, ?> en : raw.entrySet()) {
                    out.put(String.valueOf(en.getKey()), (List<Object>) en.getValue());
                }
                return out;
            }
        }
        return Collections.emptyMap();
    }

    private String getQuestionRating(EvaluationFormData formData, String categoryKey, int index) {
        Map<String, List<Object>> categories = getCategoriesMap(formData);
        if (categories.containsKey(categoryKey)) {
            List<Object> qList = categories.get(categoryKey);
            if (qList != null && index < qList.size()) {
                Object qObj = qList.get(index);
                Object rating = invokeGetter(qObj, "rating", "getRating");
                return rating != null ? String.valueOf(rating) : null;
            }
        }
        return null;
    }

    private String getQuestionComment(EvaluationFormData formData, String categoryKey, int index) {
        Map<String, List<Object>> categories = getCategoriesMap(formData);
        if (categories.containsKey(categoryKey)) {
            List<Object> qList = categories.get(categoryKey);
            if (qList != null && index < qList.size()) {
                Object qObj = qList.get(index);
                Object comment = invokeGetter(qObj, "comment", "getComment");
                return comment != null ? String.valueOf(comment) : null;
            }
        }
        return null;
    }

    private Integer getGlobalAssessmentValue(EvaluationFormData formData) {
        Object ga = invokeGetter(formData, "globalAssessment", "getGlobalAssessment");
        if (ga == null) return null;
        if (ga instanceof Number) return ((Number) ga).intValue();
        try {
            return Integer.parseInt(String.valueOf(ga));
        } catch (Exception e) {
            return null;
        }
    }

    private String getGlobalAppreciation(EvaluationFormData formData) {
        Object val = invokeGetter(formData, "globalAppreciation", "getGlobalAppreciation", "generalComment", "getGeneralComment");
        return val != null ? String.valueOf(val) : null;
    }

    private Boolean getDiscussedWithTrainee(EvaluationFormData formData) {
        Object val = invokeGetter(formData, "discussedWithTrainee", "getDiscussedWithTrainee", "discussed", "isDiscussedWithTrainee");
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        String s = String.valueOf(val);
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s)) return true;
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "0".equals(s)) return false;
        return null;
    }

    private String getSupervisionHours(EvaluationFormData formData) {
        Object val = invokeGetter(formData, "supervisionHours", "getSupervisionHours");
        return val != null ? String.valueOf(val) : null;
    }

    private String getWelcomeNextInternship(EvaluationFormData formData) {
        Object val = invokeGetter(formData, "welcomeNextInternship", "getWelcomeNextInternship");
        return val != null ? String.valueOf(val) : null;
    }

    private Boolean getTechnicalTrainingSufficient(EvaluationFormData formData) {
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
                            m = null;
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

    private void addEvaluationContent(Document document, EvaluationFormData formData, String language) throws DocumentException {
        Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font questionFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, BaseColor.DARK_GRAY);

        Map<String, CategoryData> categories = getCategoriesByLanguage(language);
        List<String> ratingKeys = List.of("EXCELLENT", "TRES_BIEN", "SATISFAISANT", "A_AMELIORER");

        for (Map.Entry<String, CategoryData> category : categories.entrySet()) {
            String categoryKey = category.getKey();
            CategoryData categoryData = category.getValue();

            Paragraph categoryTitle = new Paragraph(categoryData.getTitle(), categoryFont);
            categoryTitle.setSpacingBefore(6f);
            categoryTitle.setSpacingAfter(4f);
            document.add(categoryTitle);

            Paragraph categoryDesc = new Paragraph(categoryData.getDescription(), questionFont);
            categoryDesc.setSpacingAfter(6f);
            document.add(categoryDesc);

            List<String> questions = categoryData.getQuestions();
            for (int i = 0; i < questions.size(); i++) {
                String question = questions.get(i);
                document.add(new Paragraph((i + 1) + ". " + question, questionFont));
                document.add(Chunk.NEWLINE);

                String selectedRating = getQuestionRating(formData, categoryKey, i);

                PdfPTable ratingOptionsTable = new PdfPTable(ratingKeys.size());
                ratingOptionsTable.setWidthPercentage(100);
                ratingOptionsTable.setSpacingAfter(4f);

                for (String key : ratingKeys) {
                    boolean isSelected = key.equals(selectedRating);
                    Image checkboxImg = createCheckboxImage(isSelected);
                    PdfPCell cell;
                    if (checkboxImg != null) {
                        Paragraph p = new Paragraph();
                        p.add(new Chunk(checkboxImg, 0, -2, true));
                        cell = new PdfPCell(p);
                    } else {
                        cell = new PdfPCell(new Phrase(isSelected ? "[x]" : "[ ]"));
                    }
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(4f);
                    ratingOptionsTable.addCell(cell);
                }

                document.add(ratingOptionsTable);

                String comment = getQuestionComment(formData, categoryKey, i);
                if (comment != null && !comment.isBlank()) {
                    String commentLabel = "en".equals(language) ? "Comment: " : "Commentaire: ";
                    Paragraph commentPara = new Paragraph(commentLabel + comment, commentFont);
                    commentPara.setIndentationLeft(12f);
                    commentPara.setSpacingAfter(4f);
                    document.add(commentPara);
                }
                document.add(Chunk.NEWLINE);
            }

            document.newPage();
        }
    }

    private void addTraineeEvaluationPage(Document document, EvaluationFormData formData,
                                          String language, String profFirstName, String profLastName,
                                          String nameCollege, String address, String fax_machine) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

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

    private Map<String, CategoryData> getCategoriesByLanguage(String language) {
        if ("en".equals(language)) {
            return Map.of(
                    "productivity", new CategoryData("PRODUCTIVITY", "Ability to optimize work performance",
                            List.of("Plan and organize work effectively", "Quickly understand work instructions", "Maintain a steady work pace", "Establish priorities", "Meet deadlines")),
                    "quality", new CategoryData("WORK QUALITY", "Ability to produce careful and precise work",
                            List.of("Demonstrate rigor in work", "Respect company quality standards", "Work autonomously in verifying own work", "Seek opportunities for improvement", "To conduct a thorough analysis of the problems encountered")),
                    "relationships", new CategoryData("QUALITY OF INTERPERSONAL RELATIONSHIP", "Ability to establish harmonious interactions in the workplace",
                            List.of("Easily establish connections with people", "Actively contribute to teamwork", "Adapt easily to the company culture", "Accept constructive criticism", "Be respectful toward people", "Demonstrate active listening by trying to understand the other person's point of view")),
                    "skills", new CategoryData("PERSONAL SKILLS", "Ability to demonstrate mature and responsible attitudes or behaviors",
                            List.of("Demonstrate interest and motivation at work", "Express ideas clearly", "Show initiative", "Work safely", "Demonstrate a good sense of responsibility requiring minimal supervision", "To be punctual and diligent at work"))
            );
        } else {
            return Map.of(
                    "productivity", new CategoryData("PRODUCTIVITÉ", "Capacité d'optimiser son rendement au travail",
                            List.of("Planifier et organiser son travail de façon efficace", "Comprendre rapidement les directives relatives à son travail", "Maintenir un rythme de travail soutenu", "Établir ses priorités", "Respecter ses échéanciers")),
                    "quality", new CategoryData("QUALITÉ DU TRAVAIL", "Capacité de produire un travail soigné et précis",
                            List.of("Démontrer de la rigueur dans son travail", "Respecter les normes de qualité de l'entreprise", "Faire preuve d'autonomie dans la vérification de son travail", "Rechercher des occasions d'amélioration", "Mener une analyse approfondie des problèmes rencontrés")),
                    "relationships", new CategoryData("QUALITÉS DES RELATIONS INTERPERSONNELLES", "Capacité d’établir des interrelations harmonieuses dans son milieu de travail",
                            List.of("établir facilement des contacts avec les gens", "contribuer activement au travail d’équipe", "s’adapter facilement à la culture de l’entreprise", "accepter les critiques constructives", "être respectueux envers les gens", "faire preuve d’écoute active en essayant de comprendre le point de vue de l’autre")),
                    "skills", new CategoryData("HABILETÉS PERSONNELLES", "Capacité de faire preuve d’attitudes ou de comportements matures et responsables",
                            List.of("démontrer de l’intérêt et de la motivation au travail", "exprimer clairement ses idées", "faire preuve d’initiative", "travailler de façon sécuritaire", "démontrer un bon sens des responsabilités ne requérant qu’un minimum de supervision", "être ponctuel et assidu à son travail"))
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

    private void addGeneralComments(Document document, EvaluationFormData formData, String language) throws DocumentException {
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

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2f);
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2f);
        table.addCell(labelCell);
        table.addCell(valueCell);
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
        Map<String, String> translations = Map.of(
                "studentName", "en".equals(language) ? "Student Name:" : "Nom de l'élève:",
                "program", "en".equals(language) ? "Program:" : "Programme d'études:",
                "company", "en".equals(language) ? "Company:" : "Nom de l'entreprise:",
                "supervisor", "en".equals(language) ? "Supervisor:" : "Nom du superviseur:",
                "position", "en".equals(language) ? "Position:" : "Fonction:",
                "evaluationDate", "en".equals(language) ? "Evaluation Date:" : "Date d'évaluation:"
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
                Map.entry(Program.FINANCE.getTranslationKey(), "en".equals(language) ? "Finance" : "Finance"),
                Map.entry(Program.ECONOMICS.getTranslationKey(), "en".equals(language) ? "Economics" : "Économie"),
                Map.entry(Program.MARKETING.getTranslationKey(), "en".equals(language) ? "Marketing" : "Marketing"),
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
                Map.entry(Program.ARCHITECTURE.getTranslationKey(), "en".equals(language) ? "Architecture" : "Architecture"),
                Map.entry(Program.FINE_ARTS.getTranslationKey(), "en".equals(language) ? "Fine Arts" : "Beaux-arts"),
                Map.entry(Program.MUSIC.getTranslationKey(), "en".equals(language) ? "Music" : "Musique"),
                Map.entry(Program.THEATER.getTranslationKey(), "en".equals(language) ? "Theater" : "Théâtre"),
                Map.entry(Program.FILM_STUDIES.getTranslationKey(), "en".equals(language) ? "Film Studies" : "Études cinématographiques"),
                Map.entry(Program.COMMUNICATION.getTranslationKey(), "en".equals(language) ? "Communication" : "Communication"),
                Map.entry(Program.JOURNALISM.getTranslationKey(), "en".equals(language) ? "Journalism" : "Journalisme"),
                Map.entry(Program.DESIGN.getTranslationKey(), "en".equals(language) ? "Design" : "Design"),
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
}