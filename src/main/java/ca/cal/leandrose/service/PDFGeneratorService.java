package ca.cal.leandrose.service;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.EvaluationStagiaire;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.service.dto.evaluation.CategoryData;
import ca.cal.leandrose.service.dto.evaluation.EvaluationFormData;
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
import java.util.Map;
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

    document.add(
        new Paragraph("L'ÉTUDIANT :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
    document.add(
        new Paragraph("Nom : " + student.getLastName() + " " + student.getFirstName(), normalFont));
    document.add(new Paragraph("Email : " + student.getCredentials().getEmail(), normalFont));
    document.add(Chunk.NEWLINE);

    document.add(
        new Paragraph("L'ENTREPRISE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
    document.add(new Paragraph("Nom de l'entreprise : " + offer.getCompanyName(), normalFont));
    document.add(new Paragraph("Personne contact : " + offer.getEmployeurEmail(), normalFont));
    if (entente.getAddress() != null) {
      document.add(new Paragraph("Adresse : " + entente.getAddress(), normalFont));
    }
    document.add(Chunk.NEWLINE);

    document.add(
        new Paragraph("LE COLLÈGE :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
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
    addTableRow(
        table,
        "Date de début",
        entente.getStartDate().format(DATE_FORMATTER),
        cellBoldFont,
        cellFont);
    addTableRow(
        table, "Durée", String.valueOf(entente.getDurationInWeeks()), cellBoldFont, cellFont);

    if (entente.getAddress() != null && !entente.getAddress().isBlank()) {
      addTableRow(table, "Lieu", entente.getAddress(), cellBoldFont, cellFont);
    }

    addTableRow(table, "Rémunération", entente.getRemuneration() + " $", cellBoldFont, cellFont);

    document.add(table);
  }

  private void addTableRow(
      PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
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

  private void addMissionsObjectifs(Document document, EntenteStage entente)
      throws DocumentException {
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

    Paragraph footer =
        new Paragraph(
            "\nCe document constitue une entente officielle entre les parties mentionnées ci-dessus. "
                + "Toute modification doit faire l'objet d'un avenant signé par toutes les parties.\n\n"
                + "Document généré le "
                + LocalDate.now().format(DATE_FORMATTER),
            footerFont);
    footer.setAlignment(Element.ALIGN_JUSTIFIED);
    footer.setSpacingBefore(30f);
    document.add(footer);
  }
    public String genererEvaluationPdf(EvaluationStagiaire evaluationStagiaire, EvaluationFormData formData, String language,
                                       String profFirstName, String profLastName){
        try{
            Path evaluationDir = Paths.get(baseEvaluationsDir).toAbsolutePath().normalize();

            if(!Files.exists(evaluationDir)){
                Files.createDirectories(evaluationDir);
            }
            String filename = "evaluation_" + evaluationStagiaire.getId() + "_" + UUID.randomUUID() + PDF_EXTENSION;
            Path targetPath = evaluationDir.resolve(filename);

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile()));

            document.open();
            addHeaderEvaluation(document, language);
            addTitleEvaluation(document, language);
            addStudentAndCompanyTitle(document, evaluationStagiaire, language);
            addEvaluationContent(document, formData, language);
            addGeneralComments(document, formData, language);
            addTraineeEvaluationPage(document, formData, language, profFirstName, profLastName);
            addFooterEvaluation(document, language);

            document.close();
            writer.close();

            log.info("PDF d'évaluation généré avec succès : {}", targetPath);
            return targetPath.toString();


        }
        catch(Exception e){
            log.error("Erreur lors de la génération du PDF d'évaluation {}", evaluationStagiaire.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF d'évaluation", e);
        }
    }

    private void addHeaderEvaluation(Document document, String language) throws DocumentException{
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
        String headerText = "en".equals(language)
                ? "ANDRE-LAURENDEAU COLLEGE - Work-Study Program"
                : "CÉGEP ANDRÉ-LAURENDEAU - Alternance travail-études";

        Paragraph header = new Paragraph(headerText, headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(10f);
        document.add(header);

        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addTitleEvaluation(Document document, String language) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        String titleText = "en".equals(language)
                ? "Intern Evaluation Form"
                : "Fiche d'évaluation du stagiaire";
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
    }

    private void addStudentAndCompanyTitle(Document document, EvaluationStagiaire evaluation, String language)
            throws DocumentException{
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);
        infoTable.setSpacingAfter(15f);

        addInfoRow(infoTable, getTranslation("studentName", language),
                evaluation.getStudent().getFirstName() + " " + evaluation.getStudent().getLastName(),
                boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("program", language),
                evaluation.getStudent().getProgram(), boldFont, normalFont);

        // Company info
        addInfoRow(infoTable, getTranslation("company", language),
                evaluation.getInternshipOffer().getCompanyName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("supervisor", language),
                evaluation.getEmployeur().getFirstName() + " " + evaluation.getEmployeur().getLastName(),
                boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("position", language),
                evaluation.getEmployeur().getCompanyName(), boldFont, normalFont);
        addInfoRow(infoTable, getTranslation("evaluationDate", language),
                evaluation.getDateEvaluation().format(DATE_FORMATTER), boldFont, normalFont);

        document.add(infoTable);

        String instructionsText = "en".equals(language)
                ? "Please check the behaviors observed in the intern and provide comments if applicable."
                : "Veuillez cocher les comportements observés chez le stagiaire et formuler des commentaires s'il y a lieu.";

        Paragraph instructions = new Paragraph(instructionsText, normalFont);
        instructions.setSpacingAfter(15f);
        document.add(instructions);
    }

    private void addEvaluationContent(Document document, EvaluationFormData formData, String language) throws DocumentException {
        Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font questionFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font ratingFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);

        Map<String, CategoryData> categories = getCategoriesByLanguage(language);

        for (Map.Entry<String, CategoryData> category : categories.entrySet()) {
            String categoryKey = category.getKey();
            CategoryData categoryData = category.getValue();

            // Category title and description
            Paragraph categoryTitle = new Paragraph(categoryData.getTitle(), categoryFont);
            categoryTitle.setSpacingBefore(15f);
            categoryTitle.setSpacingAfter(5f);
            document.add(categoryTitle);

            Paragraph categoryDesc = new Paragraph(categoryData.getDescription(), questionFont);
            categoryDesc.setSpacingAfter(10f);
            document.add(categoryDesc);

            List<String> questions = categoryData.getQuestions();

            for (int i = 0; i < questions.size(); i++) {
                String question = questions.get(i);
                String questionKey = categoryKey + "_Q" + (i + 1);

                // Create a table with 3 columns: checkbox, question, rating
                PdfPTable questionTable = new PdfPTable(3);
                questionTable.setWidthPercentage(100);
                questionTable.setWidths(new float[]{1, 15, 4}); // Adjust widths as needed

                // Checkbox column
                PdfPCell checkboxCell = new PdfPCell();
                checkboxCell.setBorder(Rectangle.NO_BORDER);
                checkboxCell.setPadding(2f);
                if (formData.categories().containsKey(categoryKey) &&
                        i < formData.categories().get(categoryKey).size() &&
                        Boolean.TRUE.equals(formData.categories().get(categoryKey).get(i).checked())) {
                    checkboxCell.addElement(new Phrase("☑", questionFont));
                } else {
                    checkboxCell.addElement(new Phrase("☐", questionFont));
                }
                questionTable.addCell(checkboxCell);

                // Question text column
                PdfPCell questionCell = new PdfPCell(new Phrase(question, questionFont));
                questionCell.setBorder(Rectangle.NO_BORDER);
                questionCell.setPadding(2f);
                questionTable.addCell(questionCell);

                // Rating column - Show the actual rating value
                PdfPCell ratingCell = new PdfPCell();
                ratingCell.setBorder(Rectangle.NO_BORDER);
                ratingCell.setPadding(2f);

                String ratingText = "";
                if (formData.categories().containsKey(categoryKey) &&
                        i < formData.categories().get(categoryKey).size()) {
                    String rating = formData.categories().get(categoryKey).get(i).rating();
                    ratingText = getRatingDisplayText(rating, language);
                } else {
                    ratingText = getRatingDisplayText("NON_APPLICABLE", language);
                }

                ratingCell.addElement(new Phrase(ratingText, ratingFont));
                questionTable.addCell(ratingCell);

                document.add(questionTable);

                // Comments
                if (formData.categories().containsKey(categoryKey) &&
                        i < formData.categories().get(categoryKey).size()) {
                    String comment = formData.categories().get(categoryKey).get(i).comment();
                    if (comment != null && !comment.trim().isEmpty()) {
                        String commentLabel = "en".equals(language) ? "Comment: " : "Commentaire: ";
                        Paragraph commentPara = new Paragraph(commentLabel + comment, commentFont);
                        commentPara.setIndentationLeft(15f);
                        commentPara.setSpacingAfter(5f);
                        document.add(commentPara);
                    }
                }
            }

            document.add(Chunk.NEWLINE);
        }
    }

    // Helper method to convert rating codes to display text
    private String getRatingDisplayText(String rating, String language) {
        if ("en".equals(language)) {
            return switch (rating) {
                case "EXCELLENT" -> "Totally Agree";
                case "TRES_BIEN" -> "Mostly Agree";
                case "SATISFAISANT" -> "Mostly Disagree";
                case "A_AMELIORER" -> "Totally Disagree";
                case "NON_APPLICABLE" -> "N/A";
                default -> "N/A";
            };
        } else {
            return switch (rating) {
                case "EXCELLENT" -> "Totalement d'accord";
                case "TRES_BIEN" -> "Plutôt d'accord";
                case "SATISFAISANT" -> "Plutôt en désaccord";
                case "A_AMELIORER" -> "Totalement en désaccord";
                case "NON_APPLICABLE" -> "N/A";
                default -> "N/A";
            };
        }
    }

    private String getCategoryDescription(String category) {
        return switch (category) {
            case "PRODUCTIVITÉ" -> "Capacité d'optimiser son rendement au travail";
            case "QUALITÉ DU TRAVAIL" -> "Capacité de produire un travail soigné et précis";
            case "AUTONOMIE ET INITIATIVE" -> "Capacité de travailler avec un minimum de supervision";
            case "RELATIONS INTERPERSONNELLES" -> "Capacité d'établir et de maintenir de bonnes relations de travail";
            case "ATTITUDE PROFESSIONNELLE" -> "Comportement et éthique professionnelle";
            default -> "";
        };
    }

    private Map<String, CategoryData> getCategoriesByLanguage(String language) {
        if ("en".equals(language)) {
            return Map.of(
                    "productivity", new CategoryData(
                            "PRODUCTIVITY",
                            "Ability to optimize work performance",
                            List.of(
                                    "Plan and organize work effectively",
                                    "Quickly understand work instructions",
                                    "Maintain a steady work pace",
                                    "Establish priorities",
                                    "Meet deadlines"
                            )
                    ),
                    "quality", new CategoryData(
                            "WORK QUALITY",
                            "Ability to produce careful and precise work",
                            List.of(
                                    "Demonstrate rigor in work",
                                    "Respect company quality standards",
                                    "Work autonomously in verifying own work",
                                    "Seek opportunities for improvement",
                                    "To conduct a thorough analysis of the problems encountered"
                            )
                    ),
                    "relationships", new CategoryData(
                            "QUALITY OF INTERPERSONAL RELATIONSHIP",
                            "ability to establish harmonious interactions in the workplace",
                            List.of(
                                    "easily establish connections with people",
                                    "actively contribute to teamwork",
                                    "adapt easily to the company culture",
                                    "accept constructive criticism",
                                    "be respectful toward people",
                                    "demonstrate active listening by trying to understand the other person's point of view"
                            )
                    ),
                    "skills", new CategoryData(
                            "PERSONNAL SKILLS",
                            "ability to demonstrate mature and responsable attitudes or behaviors",
                            List.of(
                                    "demonstrate interest and motivation at work",
                                    "express ideas clearly",
                                    "show initiative",
                                    "work safely",
                                    "demonstrate a good sense of responsability requiring minimal supervision",
                                    "to be punctual and dilligent at work"
                            )
                    )
            );
        } else {
            return Map.of(
                    "productivity", new CategoryData(
                            "PRODUCTIVITÉ",
                            "Capacité d'optimiser son rendement au travail",
                            List.of(
                                    "Planifier et organiser son travail de façon efficace",
                                    "Comprendre rapidement les directives relatives à son travail",
                                    "Maintenir un rythme de travail soutenu",
                                    "Établir ses priorités",
                                    "Respecter ses échéanciers"
                            )
                    ),
                    "quality", new CategoryData(
                            "QUALITÉ DU TRAVAIL",
                            "Capacité de produire un travail soigné et précis",
                            List.of(
                                    "Démontrer de la rigueur dans son travail",
                                    "Respecter les normes de qualité de l'entreprise",
                                    "Faire preuve d'autonomie dans la vérification de son travail"
                            )
                    ),
                    "relationship", new CategoryData(
                            "QUALITÉS DES RELATIONS INTERPERSONNELLES",
                            "Capacité d’établir des interrelations harmonieuses dans son milieu de travail",
                            List.of(
                                    "établir facilement des contacts avec les gens",
                                    "contribuer activement au travail d’équipe",
                                    "s’adapter facilement à la culture de l’entreprise",
                                    "accepter les critiques constructives",
                                    "être respectueux envers les gens",
                                    "faire preuve d’écoute active en essayant de comprendre le point de vue de l’autre"
                            )
                    ),
                    "skills", new CategoryData(
                            "HABILETÉS PERSONNELLES",
                            "Capacité de faire preuve d’attitudes ou de comportements matures et responsables",
                            List.of(
                                    "démontrer de l’intérêt et de la motivation au travail",
                                    "exprimer clairement ses idées",
                                    "faire preuve d’initiative",
                                    "travailler de façon sécuritaire",
                                    "démontrer un bon sens des responsabilités ne requérant qu’un minimum de supervision",
                                    "être ponctuel et assidu à son travail"
                            )
                    )
            );
        }
    }

    private void addGeneralComments(Document document, EvaluationFormData formData, String language) throws DocumentException{
        if (formData.generalComment() != null && !formData.generalComment().trim().isEmpty()) {
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            String commentaireText = "en".equals(language)
                    ? "General Comments"
                    : "Commentaire généreaux";
            Paragraph commentsTitle = new Paragraph(commentaireText, sectionFont);
            commentsTitle.setSpacingBefore(15f);
            commentsTitle.setSpacingAfter(10f);
            document.add(commentsTitle);

            Paragraph comments = new Paragraph(formData.generalComment(), commentFont);
            comments.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(comments);
        }
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
    private void addTraineeEvaluationPage(Document document, EvaluationFormData formData, String language, String profFirstName, String profLastName) throws DocumentException {
        document.newPage();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

        String titleText = "en".equals(language)
                ? "OVERALL ASSESSMENT OF THE TRAINEE"
                : "APPRÉCIATION GLOBALE DU STAGIAIRE";
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        if (formData.globalAssessment() != null) {
            List<String> evaluationOptions = "en".equals(language)
                    ? List.of(
                    "The skills demonstrated far exceed expectations",
                    "The skills demonstrated exceed expectations",
                    "The skills demonstrated fully meet expectations",
                    "The skills demonstrated partially meet expectations",
                    "The skills demonstrated do not meet expectations"
            )
                    : List.of(
                    "Les habiletés démontrées dépassent de beaucoup les attentes",
                    "Les habiletés démontrées dépassent les attentes",
                    "Les habiletés démontrées répondent pleinement aux attentes",
                    "Les habiletés démontrées répondent partiellement aux attentes",
                    "Les habiletés démontrées ne répondent pas aux attentes"
            );

            int selectedIndex = formData.globalAssessment();
            if (selectedIndex >= 0 && selectedIndex < evaluationOptions.size()) {
                String selectedOption = evaluationOptions.get(selectedIndex);
                Paragraph assessment = new Paragraph("☑ " + selectedOption, boldFont);
                assessment.setSpacingAfter(15f);
                document.add(assessment);
            }
        }

        document.add(Chunk.NEWLINE);
        String appreciationText = "en".equals(language)
                ? "SPECIFY YOUR ASSESSMENT:"
                : "PRÉCISEZ VOTRE APPRÉCIATION:";
        Paragraph appreciationTitle = new Paragraph(appreciationText, sectionFont);
        appreciationTitle.setSpacingAfter(10f);
        document.add(appreciationTitle);

        if (formData.globalAppreciation() != null && !formData.globalAppreciation().trim().isEmpty()) {
            Paragraph appreciation = new Paragraph(formData.globalAppreciation(), normalFont);
            appreciation.setSpacingAfter(15f);
            document.add(appreciation);
        } else {
            Paragraph emptySpace = new Paragraph("_________________________", normalFont);
            emptySpace.setSpacingAfter(15f);
            document.add(emptySpace);
        }

        String discussionText = "en".equals(language)
                ? "This evaluation was discussed with the trainee:"
                : "Cette évaluation a été discutée avec le stagiaire :";

        Paragraph discussionTitle = new Paragraph(discussionText, normalFont);
        discussionTitle.setSpacingAfter(5f);
        document.add(discussionTitle);

        if (formData.discussedWithTrainee() != null) {
            String answer = formData.discussedWithTrainee()
                    ? ("en".equals(language) ? "☑ Yes" : "☑ Oui")
                    : ("en".equals(language) ? "☑ No" : "☑ Non");
            Paragraph discussionAnswer = new Paragraph(answer, boldFont);
            discussionAnswer.setSpacingAfter(15f);
            document.add(discussionAnswer);
        } else {
            String emptyAnswer = "en".equals(language) ? "☐ Yes    ☐ No" : "☐ Oui    ☐ Non";
            Paragraph emptyDiscussion = new Paragraph(emptyAnswer, normalFont);
            emptyDiscussion.setSpacingAfter(15f);
            document.add(emptyDiscussion);
        }

        String hoursText = "en".equals(language)
                ? "Please indicate the actual number of supervision hours per week granted to the trainee:"
                : "Veuillez indiquer le nombre d'heures réel par semaine d'encadrement accordé au stagiaire :";

        Paragraph hours = new Paragraph(hoursText, normalFont);
        hours.setSpacingAfter(10f);
        document.add(hours);

        if (formData.supervisionHours() != null) {
            Paragraph hoursValue = new Paragraph("☑ " + formData.supervisionHours().toString() + " hours", boldFont);
            document.add(hoursValue);
        } else {
            Paragraph emptyHours = new Paragraph("_________________________ hours", normalFont);
            document.add(emptyHours);
        }

        document.add(Chunk.NEWLINE);

        String nextInternshipText = "en".equals(language)
                ? "WOULD THE COMPANY LIKE TO WELCOME THIS STUDENT FOR THEIR NEXT INTERNSHIP:"
                : "L'ENTREPRISE AIMERAIT ACCUEILLIR CET ÉLÈVE POUR SON PROCHAIN STAGE :";

        Paragraph nextInternship = new Paragraph(nextInternshipText, sectionFont);
        nextInternship.setSpacingAfter(10f);
        document.add(nextInternship);

        if (formData.welcomeNextInternship() != null) {
            String answer = switch (formData.welcomeNextInternship()) {
                case "YES" -> "en".equals(language) ? "☑ Yes" : "☑ Oui";
                case "NO" -> "en".equals(language) ? "☑ No" : "☑ Non";
                case "MAYBE" -> "en".equals(language) ? "☑ Maybe" : "☑ Peut-être";
                default -> "";
            };
            Paragraph internshipAnswer = new Paragraph(answer, boldFont);
            internshipAnswer.setSpacingAfter(15f);
            document.add(internshipAnswer);
        } else {
            String emptyOptions = "en".equals(language) ? "☐ Yes    ☐ No    ☐ Maybe" : "☐ Oui    ☐ Non    ☐ Peut-être";
            Paragraph emptyInternship = new Paragraph(emptyOptions, normalFont);
            emptyInternship.setSpacingAfter(15f);
            document.add(emptyInternship);
        }

        String trainingText = "en".equals(language)
                ? "Was the trainee's technical training sufficient to accomplish the internship mandate?"
                : "La formation technique du stagiaire était-elle suffisante pour accomplir le mandat de stage?";

        Paragraph trainingQuestion = new Paragraph(trainingText, normalFont);
        trainingQuestion.setSpacingAfter(10f);
        document.add(trainingQuestion);

        if (formData.technicalTrainingSufficient() != null) {
            String answer = formData.technicalTrainingSufficient()
                    ? ("en".equals(language) ? "☑ Yes" : "☑ Oui")
                    : ("en".equals(language) ? "☑ No" : "☑ Non");
            Paragraph trainingAnswer = new Paragraph(answer, boldFont);
            trainingAnswer.setSpacingAfter(20f);
            document.add(trainingAnswer);
        } else {
            String emptyAnswer = "en".equals(language) ? "☐ Yes    ☐ No" : "☐ Oui    ☐ Non";
            Paragraph emptyTraining = new Paragraph(emptyAnswer, normalFont);
            emptyTraining.setSpacingAfter(20f);
            document.add(emptyTraining);
        }

        String nameText = "en".equals(language) ? "Name: _________________________" : "Nom : _________________________";
        String functionText = "en".equals(language) ? "Function: _________________________" : "Fonction : _________________________";

        Paragraph name = new Paragraph(nameText + "   " + functionText, normalFont);
        name.setSpacingAfter(10f);
        document.add(name);

        String signatureText = "en".equals(language) ? "Signature: _________________________" : "Signature : _________________________";
        String dateText = "en".equals(language) ? "Date:" + LocalDate.now() : "Date : " + LocalDate.now();

        Paragraph signature = new Paragraph(signatureText + "   " + dateText, normalFont);
        signature.setSpacingAfter(20f);
        document.add(signature);

        String returnText = "en".equals(language)
                ? "Please return this form to:"
                : "Veuillez retourner ce formulaire à :";

        Paragraph returnTitle = new Paragraph(returnText, sectionFont);
        returnTitle.setSpacingAfter(5f);
        document.add(returnTitle);

//        Paragraph address = new Paragraph("Patrice Brodeur\nCégep André-Laurendeau\n1111, rue Lapierre\nLASALLE (Québec)\nH8N 2J4", normalFont);
//        address.setSpacingAfter(5f);
//        document.add(address);

        // Use the provided professor name or fallback to default
        String professorName = (profFirstName != null && profLastName != null)
                ? profFirstName + " " + profLastName
                : "Patrice Brodeur"; // Fallback default

        Paragraph address = new Paragraph(
                professorName + "\nCégep André-Laurendeau\n1111, rue Lapierre\nLASALLE (Québec)\nH8N 2J4",
                normalFont
        );
        address.setSpacingAfter(5f);
        document.add(address);

        String faxText = "en".equals(language)
                ? "Fax number: (514) 364-7130"
                : "Numéro de télécopieur : (514) 364-7130";

        Paragraph fax = new Paragraph(faxText, normalFont);
        fax.setSpacingAfter(15f);
        document.add(fax);

        String thankYouText = "en".equals(language)
                ? "We thank you for your support!"
                : "Nous vous remercions de votre appui !";

        Paragraph thankYou = new Paragraph(thankYouText, boldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingAfter(10f);
        document.add(thankYou);

        Paragraph footer = new Paragraph("Collège André-Laurendeau\nALTERNANCE TRAVAIL-ÉTUDES\n2010-09-21", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
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

    private void addFooterEvaluation(Document document, String language) throws DocumentException{
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        String naText = "en".equals(language) ? "* N/A = not applicable" : "* N/A = non applicable";
        String generatedText = "en".equals(language) ? "Document generated on " : "Document généré le ";


        Paragraph footer = new Paragraph(
                "\n" + naText + "\n\n" +
                        generatedText + LocalDate.now().format(DATE_FORMATTER),
                footerFont
        );
        footer.setAlignment(Element.ALIGN_LEFT);
        footer.setSpacingBefore(20f);
        document.add(footer);
    }
}