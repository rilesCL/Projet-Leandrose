package ca.cal.leandrose;

import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
public class LeandrOseApplication {

  public static void main(String[] args) {
    SpringApplication.run(LeandrOseApplication.class, args);
  }

  @Bean
  @Profile("!test")
  @Transactional
  public CommandLineRunner Lsc0SE(
      EmployeurService employeurService,
      StudentService studentService,
      GestionnaireService gestionnaireService,
      CvService cvService,
      InternshipOfferService internshipOfferService,
      CandidatureService candidatureService,
      ConvocationService convocationService,
      EntenteStageService ententeStageService,
      ProfService profService) {

    return args -> {
      try {
        employeurService.createEmployeur(
            "Leandro",
            "Schoonewolff",
            "wbbey@gmail.com",
            "mansang",
            "macolo",
            Program.COMPUTER_SCIENCE.getTranslationKey());

        StudentDto riles = studentService.createStudent(
            "Ghilas",
            "Amr",
            "ghil.amr@student.com",
            "Password123",
            "STU001",
            Program.COMPUTER_SCIENCE.getTranslationKey());

        studentService.createStudentwithTerm(
            "John",
            "Doe",
            "john.doe@student.com",
            "Password123",
            "STU002",
            Program.COMPUTER_SCIENCE.getTranslationKey(),
            new SchoolTerm(SchoolTerm.Season.SUMMER, 2023));

        GestionnaireDto gestionnaireCreated =
            gestionnaireService.createGestionnaire(
                "Jean", "Dupont", "gestionnaire@test.com", "Password123!", "514-123-4567");
        System.out.println("Gestionnaire créé avec ID: " + gestionnaireCreated.getId());

        EmployeurDto employeurConvocation =
            employeurService.createEmployeur(
                "Marie",
                "Tremblay",
                "marie.tremblay@entreprise.com",
                "Password123",
                "TechInnovation Inc.",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());

        StudentDto studentConvocation =
            studentService.createStudent(
                "Sophie",
                "Martin",
                "sophie.martin@student.com",
                "Password123",
                "STU003",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());

        MultipartFile cvFile = loadPdfFromResources("CV_Sophie_Martin.pdf");
        CvDto cvDto = cvService.uploadCv(studentConvocation.getId(), cvFile);
        CvDto cvApproved = gestionnaireService.approveCv(cvDto.getId());

        MultipartFile offerFile = loadPdfFromResources("Offre_Stage_TechInnovation.pdf");
        InternshipOfferDto offerDto =
            internshipOfferService.createOfferDto(
                "Développeur Full-Stack Junior",
                LocalDate.now().plusMonths(2),
                12,
                "123 Rue Principale, Montréal, QC",
                25.00f,
                employeurConvocation,
                offerFile);
        InternshipOfferDto offerApproved = gestionnaireService.approveOffer(offerDto.getId());

        CandidatureDto candidatureDto =
            candidatureService.postuler(
                studentConvocation.getId(), offerApproved.getId(), cvApproved.getId());

        LocalDateTime convocationDate = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0);
        String location =
            "TechInnovation Inc., Salle de conférence A, 123 Rue Principale, Montréal";
        String message =
            """
                        Bonjour Sophie,

                        Nous sommes ravis de vous inviter à un entretien pour le poste de Développeur Full-Stack Junior.
                        Veuillez apporter une pièce d'identité et une copie de votre CV.

                        Au plaisir de vous rencontrer!

                        Cordialement,
                        L'équipe RH de TechInnovation""";

        convocationService.addConvocation(
            candidatureDto.getId(), convocationDate, location, message);

        StudentDto studentConvocation2 =
            studentService.createStudent(
                "Alexandre",
                "Dubois",
                "alexandre.dubois@student.com",
                "Password123",
                "STU004",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());

        MultipartFile cvFile2 = loadPdfFromResources("CV_Alexandre_Dubois.pdf");
        CvDto cvDto2 = cvService.uploadCv(studentConvocation2.getId(), cvFile2);

        CvDto cvApproved2 = gestionnaireService.approveCv(cvDto2.getId());

        CandidatureDto candidatureDto2 =
            candidatureService.postuler(
                studentConvocation2.getId(), offerApproved.getId(), cvApproved2.getId());

        LocalDateTime convocationDate2 =
            LocalDateTime.now().plusDays(8).withHour(10).withMinute(30);
        String location2 = "TechInnovation Inc., Salle B, 123 Rue Principale, Montréal";
        String message2 =
            """
                        Bonjour Alexandre,

                        Nous vous invitons à un entretien pour le poste de Développeur Full-Stack Junior.
                        Apportez une pièce d'identité et votre CV.

                        Cordialement,
                        L'équipe RH""";

        convocationService.addConvocation(
            candidatureDto2.getId(), convocationDate2, location2, message2);

        candidatureService.acceptByEmployeur(candidatureDto2.getId());

        StudentDto studentConvocation3 =
            studentService.createStudent(
                "Alexandre",
                "Gagné",
                "alexandre.gagne@student.com",
                "Password123",
                "STU005",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());

        MultipartFile cvFile3 = loadPdfFromResources("CV_Alexandre_Gagne.pdf");
        CvDto cvDto3 = cvService.uploadCv(studentConvocation3.getId(), cvFile3);

        CvDto cvApproved3 = gestionnaireService.approveCv(cvDto3.getId());

        CandidatureDto candidatureDto3 =
            candidatureService.postuler(
                studentConvocation3.getId(), offerApproved.getId(), cvApproved3.getId());

        LocalDateTime convocationDate3 =
            LocalDateTime.now().plusDays(8).withHour(10).withMinute(30);
        String location3 = "TechInnovation Inc., Salle B, 123 Rue Principale, Montréal";
        String message3 =
            """
                        Bonjour Alexandre,

                        Nous vous invitons à un entretien pour le poste de Développeur Full-Stack Junior.
                        Apportez une pièce d'identité et votre CV.

                        Cordialement,
                        L'équipe RH""";

        convocationService.addConvocation(
            candidatureDto3.getId(), convocationDate3, location3, message3);

        candidatureService.acceptByEmployeur(candidatureDto3.getId());

        EmployeurDto employeurEntente =
            employeurService.createEmployeur(
                "Philippe",
                "Lavoie",
                "philippe.lavoie@entreprise.com",
                "Password123",
                "Solutions Logicielles Pro",
                Program.COMPUTER_SCIENCE.getTranslationKey());

        StudentDto studentEntente =
            studentService.createStudent(
                "Émilie",
                "Fortin",
                "emilie.fortin@student.com",
                "Password123",
                "STU006",
                Program.COMPUTER_SCIENCE.getTranslationKey());

        MultipartFile cvFileEntente = loadPdfFromResources("CV_Emilie_Fortin.pdf");
        CvDto cvDtoEntente = cvService.uploadCv(studentEntente.getId(), cvFileEntente);
        CvDto cvApprovedEntente = gestionnaireService.approveCv(cvDtoEntente.getId());

        MultipartFile offerFileEntente = loadPdfFromResources("Offre_Stage_Solutions_Pro.pdf");
        InternshipOfferDto offerDtoEntente =
            internshipOfferService.createOfferDto(
                "Stage en développement web",
                LocalDate.now().plusMonths(1),
                16,
                "456 Boulevard Tech, Montréal, QC",
                28.50f,
                employeurEntente,
                offerFileEntente);
        InternshipOfferDto offerApprovedEntente =
            gestionnaireService.approveOffer(offerDtoEntente.getId());

        CandidatureDto candidatureEntente =
            candidatureService.postuler(
                studentEntente.getId(), offerApprovedEntente.getId(), cvApprovedEntente.getId());

        CandidatureDto candidatureAcceptedEntente =
            candidatureService.acceptByEmployeur(candidatureEntente.getId());

        CandidatureDto candidatureFullyAccepted =
            candidatureService.acceptByStudent(
                candidatureAcceptedEntente.getId(), studentEntente.getId());

          EntenteStageDto ententeDto = getEntenteStageDto(candidatureFullyAccepted, offerApprovedEntente);

          EntenteStageDto ententeCreated = ententeStageService.creerEntente(ententeDto);

        StudentDto studentEntente2 =
            studentService.createStudent(
                "Lucas",
                "Bergeron",
                "lucas.bergeron@student.com",
                "Password123",
                "STU007",
                Program.COMPUTER_SCIENCE.getTranslationKey());


        MultipartFile cvFileEntente2 = loadPdfFromResources("CV_Lucas_Bergeron.pdf");
        CvDto cvDtoEntente2 = cvService.uploadCv(studentEntente2.getId(), cvFileEntente2);
        CvDto cvApprovedEntente2 = gestionnaireService.approveCv(cvDtoEntente2.getId());

        MultipartFile offerFileEntente2 = loadPdfFromResources("Offre_Stage_Solutions_Pro_2.pdf");
        InternshipOfferDto offerDtoEntente2 =
            internshipOfferService.createOfferDto(
                "Stage en développement mobile",
                LocalDate.now().plusMonths(2),
                12,
                "789 Avenue Innovation, Montréal, QC",
                26.00f,
                employeurEntente,
                offerFileEntente2);
        InternshipOfferDto offerApprovedEntente2 =
            gestionnaireService.approveOffer(offerDtoEntente2.getId());

        CandidatureDto candidatureEntente2 =
            candidatureService.postuler(
                studentEntente2.getId(), offerApprovedEntente2.getId(), cvApprovedEntente2.getId());

        CandidatureDto candidatureAcceptedEntente2 =
            candidatureService.acceptByEmployeur(candidatureEntente2.getId());
        CandidatureDto candidatureFullyAccepted2 =
            candidatureService.acceptByStudent(
                candidatureAcceptedEntente2.getId(), studentEntente2.getId());

          //Entente de stage Demo
        MultipartFile cvFileRiles = loadPdfFromResources("CV_Riles.pdf");
        CvDto cvDtoRiles = cvService.uploadCv(riles.getId(), cvFileRiles);
        CvDto cvApprovedRiles = gestionnaireService.approveCv(cvDtoRiles.getId());

        CandidatureDto candidatureDtoRiles = candidatureService.postuler(riles.getId(),
                offerApprovedEntente2.getId(), cvApprovedRiles.getId());
        CandidatureDto candidatureRilesDto =
                candidatureService.acceptByEmployeur(candidatureDtoRiles.getId());

        candidatureService.acceptByStudent(
                candidatureRilesDto.getId(), riles.getId());

        EntenteStageDto ententeDto2 = new EntenteStageDto();
        ententeDto2.setCandidatureId(candidatureFullyAccepted2.getId());
        ententeDto2.setDateDebut(offerApprovedEntente2.getStartDate());
        ententeDto2.setDuree(offerApprovedEntente2.getDurationInWeeks());
        ententeDto2.setLieu(offerApprovedEntente2.getAddress());
        ententeDto2.setRemuneration(offerApprovedEntente2.getRemuneration());
        ententeDto2.setMissionsObjectifs(
            """
                                L'étudiant(e) sera amené(e) à :
                                - Développer des applications mobiles avec React Native
                                - Apprendre Flutter et Dart
                                - Participer aux tests et au déploiement

                                Objectifs d'apprentissage :
                                - Maîtriser le développement mobile cross-platform
                                - Comprendre l'architecture des applications mobiles""");

        ententeStageService.creerEntente(ententeDto2);

        try {

          ententeStageService.signerParEtudiant(ententeCreated.getId(), studentEntente.getId());

        } catch (Exception e) {
          System.err.println("Erreur STORY 40 : " + e.getMessage());
        }

        // 1. Créer un professeur
        ProfDto prof1 =
            profService.createProf(
                "Marie-Claude",
                "Beauchamp",
                "marie-claude.beauchamp@college.ca",
                "Password123!",
                "PROF001",
                "Collège Mainsonneuve",
                "3800 R. Sherbrooke E, Montréal, QC H1X 2A2",
                "(514) 364-7130",
                "Département d'informatique");
        System.out.println(
            "✓ Prof créé: "
                + prof1.getFirstName()
                + " "
                + prof1.getLastName()
                + " (ID: "
                + prof1.getId()
                + ")");

        ProfDto prof2 =
            profService.createProf(
                "Jean-François",
                "Gagnon",
                "jf.gagnon@college.ca",
                "Password123!",
                "PROF002",
                "Collège Ahuntsic",
                "9155 Rue St-Hubert, Montréal, QC H2M 1Y8",
                "(514) 364-7130",
                "Département de génie logiciel");
        System.out.println(
            "✓ Prof créé: "
                + prof2.getFirstName()
                + " "
                + prof2.getLastName()
                + " (ID: "
                + prof2.getId()
                + ")");

        // 2. Créer un nouveau scénario complet pour l'attribution d'un prof
        EmployeurDto employeurProf =
            employeurService.createEmployeur(
                "Catherine",
                "Leduc",
                "catherine.leduc@techquebec.com",
                "Password123",
                "TechQuébec Solutions",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());
        System.out.println("✓ Employeur créé: " + employeurProf.getCompanyName());

        StudentDto studentProf =
            studentService.createStudent(
                "Antoine",
                "Tremblay",
                "antoine.tremblay@student.com",
                "Password123",
                "STU100",
                Program.SOFTWARE_ENGINEERING.getTranslationKey());
        System.out.println(
            "✓ Étudiant créé: " + studentProf.getFirstName() + " " + studentProf.getLastName());

        // 3. CV et offre
        MultipartFile cvFileProf = loadPdfFromResources("CV_Antoine_Tremblay.pdf");
        CvDto cvDtoProf = cvService.uploadCv(studentProf.getId(), cvFileProf);
        CvDto cvApprovedProf = gestionnaireService.approveCv(cvDtoProf.getId());
        System.out.println("✓ CV approuvé pour l'étudiant");

        MultipartFile offerFileProf = loadPdfFromResources("Offre_Stage_TechQuebec.pdf");
        InternshipOfferDto offerDtoProf =
            internshipOfferService.createOfferDto(
                "Développeur Java/Spring Boot",
                LocalDate.now().plusMonths(3),
                14,
                "100 Rue Innovation, Québec, QC",
                27.50f,
                employeurProf,
                offerFileProf);
        InternshipOfferDto offerApprovedProf =
            gestionnaireService.approveOffer(offerDtoProf.getId());
        System.out.println("✓ Offre de stage approuvée: " + offerApprovedProf.getDescription());

        // 4. Candidature acceptée par employeur ET étudiant
        CandidatureDto candidatureProf =
            candidatureService.postuler(
                studentProf.getId(), offerApprovedProf.getId(), cvApprovedProf.getId());
        System.out.println("✓ Candidature soumise");

        CandidatureDto candidatureAcceptedByEmployeur =
            candidatureService.acceptByEmployeur(candidatureProf.getId());
        System.out.println("✓ Candidature acceptée par l'employeur");

        CandidatureDto candidatureFullyAcceptedProf =
            candidatureService.acceptByStudent(
                candidatureAcceptedByEmployeur.getId(), studentProf.getId());
        System.out.println("✓ Candidature acceptée par l'étudiant (ACCEPTED)");

        // 5. Créer l'entente
        EntenteStageDto ententeDtoProf = new EntenteStageDto();
        ententeDtoProf.setCandidatureId(candidatureFullyAcceptedProf.getId());
        ententeDtoProf.setDateDebut(offerApprovedProf.getStartDate());
        ententeDtoProf.setDuree(offerApprovedProf.getDurationInWeeks());
        ententeDtoProf.setLieu(offerApprovedProf.getAddress());
        ententeDtoProf.setRemuneration(offerApprovedProf.getRemuneration());
        ententeDtoProf.setMissionsObjectifs(
            """
                            Mission du stage:
                            - Développer des microservices avec Spring Boot
                            - Implémenter des API RESTful
                            - Participer aux revues de code
                            - Utiliser Docker et Kubernetes

                            Objectifs d'apprentissage:
                            - Maîtriser l'écosystème Spring
                            - Comprendre l'architecture microservices
                            - Développer des compétences en DevOps""");

        EntenteStageDto ententeCreatedProf = ententeStageService.creerEntente(ententeDtoProf);
        System.out.println(
            "✓ Entente créée (Statut: "
                + ententeCreatedProf.getStatut()
                + ", ID: "
                + ententeCreatedProf.getId()
                + ")");

        // 6. Faire signer l'entente par TOUTES les parties
        // Signature étudiant
        ententeStageService.signerParEtudiant(ententeCreatedProf.getId(), studentProf.getId());
        System.out.println("✓ Entente signée par l'étudiant");

        // Signature employeur
        ententeStageService.signerParEmployeur(ententeCreatedProf.getId(), employeurProf.getId());
        System.out.println("✓ Entente signée par l'employeur");

        // Signature gestionnaire
        ententeStageService.signerParGestionnaire(
            ententeCreatedProf.getId(), gestionnaireCreated.getId());
        System.out.println(
            "✓ Entente signée par le gestionnaire: "
                + gestionnaireCreated.getFirstName()
                + " "
                + gestionnaireCreated.getLastName());

        // Vérifier le statut après toutes les signatures
        EntenteStageDto ententeValidee =
            ententeStageService.getEntenteById(ententeCreatedProf.getId());
        System.out.println("✓ Statut de l'entente: " + ententeValidee.getStatut());

        if (ententeValidee.getStatut().toString().equals("VALIDEE")) {
          System.out.println("✓ L'entente est VALIDEE - Prêt pour attribution d'un prof!");
          ententeStageService.attribuerProf(ententeValidee.getId(), prof1.getId());
          System.out.println(prof1.getFirstName() + " " + prof1.getLastName() + " a été attributé");

        } else {
          System.err.println(
              "✗ ERREUR: L'entente n'est pas VALIDEE. Statut actuel: "
                  + ententeValidee.getStatut());
        }

      } catch (Exception e) {
        System.err.println("Erreur générale non prévue: " + e.getMessage());
        e.printStackTrace();
      }
    };
  }

    private static EntenteStageDto getEntenteStageDto(CandidatureDto candidatureFullyAccepted, InternshipOfferDto offerApprovedEntente) {
        EntenteStageDto ententeDto = new EntenteStageDto();
        ententeDto.setCandidatureId(candidatureFullyAccepted.getId());
        ententeDto.setDateDebut(offerApprovedEntente.getStartDate());
        ententeDto.setDuree(offerApprovedEntente.getDurationInWeeks());
        ententeDto.setLieu(offerApprovedEntente.getAddress());
        ententeDto.setRemuneration(offerApprovedEntente.getRemuneration());
        ententeDto.setMissionsObjectifs(
            """
                                L'étudiant(e) sera amené(e) à :
                                - Développer des fonctionnalités web en utilisant React et Node.js
                                - Participer aux revues de code et aux cérémonies Agile
                                - Collaborer avec l'équipe de développement sur des projets clients
                                - Améliorer ses compétences en développement full-stack

                                Objectifs d'apprentissage :
                                - Maîtriser les frameworks modernes de développement web
                                - Comprendre les méthodologies Agile en entreprise
                                - Développer son autonomie et sa capacité à résoudre des problèmes""");
        return ententeDto;
    }

    private MultipartFile loadPdfFromResources(String filename) throws IOException {
    ClassPathResource resource = new ClassPathResource("test.pdf");
    try (InputStream inputStream = resource.getInputStream()) {
      byte[] pdfContent = inputStream.readAllBytes();
      return new CustomMultipartFile(pdfContent, filename);
    }
  }

  private record CustomMultipartFile(byte[] content, String filename) implements MultipartFile {
    @Override
    public String getName() {
      return "file";
    }

    @Override
    public String getOriginalFilename() {
      return filename;
    }

    @Override
    public String getContentType() {
      return "application/pdf";
    }

    @Override
    public boolean isEmpty() {
      return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() {
      return content;
    }

    @Override
    public InputStream getInputStream() {
      return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
      try (FileOutputStream fos = new FileOutputStream(dest)) {
        fos.write(content);
      }
    }
  }
}
