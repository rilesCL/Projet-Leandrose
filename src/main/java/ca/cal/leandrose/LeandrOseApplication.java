package ca.cal.leandrose;

import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
            EntenteStageService ententeStageService) {

        return args -> {
            try {
                // === Code existant (Stories 34–38) ===
                employeurService.createEmployeur(
                        "Leandro",
                        "Schoonewolff",
                        "wbbey@gmail.com",
                        "mansang",
                        "macolo",
                        Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println(employeurService.getEmployeurById(1L));

                StudentDto studentDto =
                        studentService.createStudent(
                                "Ghilas",
                                "Amr",
                                "ghil.amr@student.com",
                                "Password123",
                                "STU001",
                                Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println("Student créé: " + studentService.getStudentById(studentDto.getId()));

                StudentDto studentDto2 =
                        studentService.createStudent(
                                "John",
                                "Doe",
                                "john.doe@student.com",
                                "Password123",
                                "STU002",
                                Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println("Student créé: " + studentService.getStudentById(studentDto2.getId()));

                GestionnaireDto gestionnaireDto =
                        gestionnaireService.createGestionnaire(
                                "Jean", "Dupont", "gestionnaire@test.com", "Password123!", "514-123-4567");
                System.out.println("Gestionnaire créé: " + gestionnaireDto);

                EmployeurDto employeurConvocation = employeurService.createEmployeur(
                        "Marie",
                        "Tremblay",
                        "marie.tremblay@entreprise.com",
                        "Password123",
                        "TechInnovation Inc.",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Employeur pour convocation créé: " + employeurConvocation);

                StudentDto studentConvocation = studentService.createStudent(
                        "Sophie",
                        "Martin",
                        "sophie.martin@student.com",
                        "Password123",
                        "STU003",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Étudiant pour convocation créé: " + studentConvocation);

                MultipartFile cvFile = loadPdfFromResources("test.pdf", "CV_Sophie_Martin.pdf");
                CvDto cvDto = cvService.uploadCv(studentConvocation.getId(), cvFile);
                CvDto cvApproved = gestionnaireService.approveCv(cvDto.getId());

                MultipartFile offerFile = loadPdfFromResources("test.pdf", "Offre_Stage_TechInnovation.pdf");
                InternshipOfferDto offerDto = internshipOfferService.createOfferDto(
                        "Développeur Full-Stack Junior",
                        LocalDate.now().plusMonths(2),
                        12,
                        "123 Rue Principale, Montréal, QC",
                        25.00f,
                        employeurConvocation,
                        offerFile);
                InternshipOfferDto offerApproved = gestionnaireService.approveOffer(offerDto.getId());

                CandidatureDto candidatureDto = candidatureService.postuler(
                        studentConvocation.getId(),
                        offerApproved.getId(),
                        cvApproved.getId());

                LocalDateTime convocationDate = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0);
                String location = "TechInnovation Inc., Salle A, 123 Rue Principale, Montréal";
                convocationService.addConvocation(
                        candidatureDto.getId(),
                        convocationDate,
                        location,
                        "Convocation STORY 38");

                StudentDto studentConvocation2 = studentService.createStudent(
                        "Alexandre",
                        "Dubois",
                        "alexandre.dubois@student.com",
                        "Password123",
                        "STU004",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                MultipartFile cvFile2 = loadPdfFromResources("test.pdf", "CV_Alexandre_Dubois.pdf");
                CvDto cvDto2 = cvService.uploadCv(studentConvocation2.getId(), cvFile2);
                CvDto cvApproved2 = gestionnaireService.approveCv(cvDto2.getId());
                CandidatureDto candidatureDto2 = candidatureService.postuler(
                        studentConvocation2.getId(),
                        offerApproved.getId(),
                        cvApproved2.getId());
                candidatureService.acceptByEmployeur(candidatureDto2.getId());

                // === STORY 39 : Création complète d'une entente de stage ===
                System.out.println("\n===== STORY 39 : Création complète d'une entente de stage =====");

                // 1️⃣ Création d'un employeur
                EmployeurDto employeurEntente = employeurService.createEmployeur(
                        "Patrick",
                        "Lavoie",
                        "patrick.lavoie@innovsolutions.com",
                        "Password123",
                        "InnovSolutions",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Employeur STORY 39 créé : " + employeurEntente.getCompanyName());

                // 2️⃣ Création d'un étudiant
                StudentDto studentEntente = studentService.createStudent(
                        "Camille",
                        "Roy",
                        "camille.roy@student.com",
                        "Password123",
                        "STU005",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Étudiant STORY 39 créé : " + studentEntente.getFirstName());

                // 3️⃣ Upload et approbation du CV
                MultipartFile cvFile3 = loadPdfFromResources("test.pdf", "CV_Camille_Roy.pdf");
                CvDto cvDto3 = cvService.uploadCv(studentEntente.getId(), cvFile3);
                CvDto cvApproved3 = gestionnaireService.approveCv(cvDto3.getId());
                System.out.println("CV approuvé pour " + studentEntente.getFirstName());

                // 4️⃣ Création et approbation de l'offre
                MultipartFile offerFile3 = loadPdfFromResources("test.pdf", "Offre_Stage_InnovSolutions.pdf");
                InternshipOfferDto offerDto3 = internshipOfferService.createOfferDto(
                        "Stage QA Automatisation",
                        LocalDate.now().plusWeeks(3),
                        10,
                        "850 Rue Saint-Denis, Montréal",
                        23.50f,
                        employeurEntente,
                        offerFile3);
                InternshipOfferDto offerApproved3 = gestionnaireService.approveOffer(offerDto3.getId());
                System.out.println("Offre STORY 39 approuvée : ");

                // 5️⃣ L'étudiant postule à l'offre
                CandidatureDto candidatureEntente = candidatureService.postuler(
                        studentEntente.getId(),
                        offerApproved3.getId(),
                        cvApproved3.getId());
                System.out.println("Candidature STORY 39 créée : " + candidatureEntente.getId());

                // 6️⃣ L’employeur accepte la candidature
                CandidatureDto candidatureAccepted39 = candidatureService.acceptByEmployeur(candidatureEntente.getId());
                System.out.println("Candidature STORY 39 acceptée par l'employeur : " + candidatureAccepted39.getStatus());

                // 7️⃣ L’étudiant confirme l’acceptation (statut devient ACCEPTED)
                CandidatureDto candidatureConfirmed39 = candidatureService.acceptByStudent(
                        candidatureAccepted39.getId(),
                        studentEntente.getId());
                System.out.println("Candidature STORY 39 confirmée par l’étudiant : " + candidatureConfirmed39.getStatus());

                // 8️⃣ Création de l’entente
                EntenteStageDto ententeDto39 = EntenteStageDto.builder()
                        .candidatureId(candidatureConfirmed39.getId())
                        .dateDebut(LocalDate.now().plusWeeks(4))
                        .dateFin(LocalDate.now().plusMonths(3))
                        .duree("10 semaines")
                        .horaires("37.5h/semaine")
                        .lieu("850 Rue Saint-Denis, Montréal")
                        .modalitesTeletravail("Présentiel complet")
                        .remuneration(new java.math.BigDecimal("23.50"))
                        .missionsObjectifs("Tests automatisés, scripts CI/CD et outils de QA avec Selenium et Jenkins.")
                        .build();

                EntenteStageDto ententeCreated39 = ententeStageService.creerEntente(ententeDto39);
                System.out.println("✅ Entente STORY 39 créée : " + ententeCreated39.getId());

                // 9️⃣ Validation et signature
                EntenteStageDto ententeValidee39 = ententeStageService.validerEtGenererEntente(ententeCreated39.getId());
                System.out.println("📄 Entente STORY 39 validée par le gestionnaire.");

//                EntenteStageDto ententeSignee39 = ententeStageService.signerParEmployeur(
//                        ententeValidee39.getId(),
//                        employeurEntente.getId());
//                System.out.println("✍️ Entente STORY 39 signée par l'employeur : "
//                        + ententeSignee39.getDateSignatureEmployeur());

                System.out.println("===== FIN STORY 39 =====\n");

            } catch (Exception e) {
                System.err.println("Erreur générale non prévue: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    /** Charge un fichier PDF depuis les resources */
    private MultipartFile loadPdfFromResources(String resourcePath, String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] pdfContent = inputStream.readAllBytes();
            return new CustomMultipartFile(pdfContent, filename);
        }
    }

    private record CustomMultipartFile(byte[] content, String filename) implements MultipartFile {
        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return "application/pdf"; }
        @Override public boolean isEmpty() { return content == null || content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(dest)) { fos.write(content); }
        }
    }
}
