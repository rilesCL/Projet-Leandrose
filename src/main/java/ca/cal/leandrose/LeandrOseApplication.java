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
                System.out.println("Candidature créée: " + candidatureDto);

                LocalDateTime convocationDate = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0);
                String location = "TechInnovation Inc., Salle de conférence A, 123 Rue Principale, Montréal";
                String message = "Bonjour Sophie,\n\n" +
                        "Nous sommes ravis de vous inviter à un entretien pour le poste de Développeur Full-Stack Junior.\n" +
                        "Veuillez apporter une pièce d'identité et une copie de votre CV.\n\n" +
                        "Au plaisir de vous rencontrer!\n\n" +
                        "Cordialement,\nL'équipe RH de TechInnovation";

                convocationService.addConvocation(
                        candidatureDto.getId(),
                        convocationDate,
                        location,
                        message);
                System.out.println("Convocation créée avec succès pour la candidature ID: " + candidatureDto.getId());

                StudentDto studentConvocation2 = studentService.createStudent(
                        "Alexandre",
                        "Dubois",
                        "alexandre.dubois@student.com",
                        "Password123",
                        "STU004",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Étudiant pour convocation et acceptation créé: " + studentConvocation2);

                MultipartFile cvFile2 = loadPdfFromResources("test.pdf", "CV_Alexandre_Dubois.pdf");
                CvDto cvDto2 = cvService.uploadCv(studentConvocation2.getId(), cvFile2);
                System.out.println("CV créé pour Alexandre: " + cvDto2);

                CvDto cvApproved2 = gestionnaireService.approveCv(cvDto2.getId());
                System.out.println("CV approuvé pour Alexandre: " + cvApproved2);

                CandidatureDto candidatureDto2 = candidatureService.postuler(
                        studentConvocation2.getId(),
                        offerApproved.getId(),
                        cvApproved2.getId());
                System.out.println("Candidature créée pour Alexandre: " + candidatureDto2);

                LocalDateTime convocationDate2 = LocalDateTime.now().plusDays(8).withHour(10).withMinute(30);
                String location2 = "TechInnovation Inc., Salle B, 123 Rue Principale, Montréal";
                String message2 = "Bonjour Alexandre,\n\n" +
                        "Nous vous invitons à un entretien pour le poste de Développeur Full-Stack Junior.\n" +
                        "Apportez une pièce d'identité et votre CV.\n\n" +
                        "Cordialement,\nL'équipe RH";

                convocationService.addConvocation(
                        candidatureDto2.getId(),
                        convocationDate2,
                        location2,
                        message2);
                System.out.println("Convocation créée pour Alexandre, candidature ID: " + candidatureDto2.getId());

                CandidatureDto candidatureAccepted = candidatureService.acceptByEmployeur(candidatureDto2.getId());
                System.out.println("Candidature acceptée par l'employeur pour Alexandre: " + candidatureAccepted);

                StudentDto studentConvocation3 = studentService.createStudent(
                        "Alexandre",
                        "Gagné",
                        "alexandre.gagne@student.com",
                        "Password123",
                        "STU005",
                        Program.SOFTWARE_ENGINEERING.getTranslationKey());
                System.out.println("Étudiant pour convocation et acceptation créé: " + studentConvocation3);

                MultipartFile cvFile3 = loadPdfFromResources("test.pdf", "CV_Alexandre_Gagne.pdf");
                CvDto cvDto3 = cvService.uploadCv(studentConvocation3.getId(), cvFile3);
                System.out.println("CV créé pour Alexandre: " + cvDto3);

                CvDto cvApproved3 = gestionnaireService.approveCv(cvDto3.getId());
                System.out.println("CV approuvé pour Alexandre: " + cvApproved3);

                CandidatureDto candidatureDto3 = candidatureService.postuler(
                        studentConvocation3.getId(),
                        offerApproved.getId(),
                        cvApproved3.getId());
                System.out.println("Candidature créée pour Alexandre: " + candidatureDto3);

                LocalDateTime convocationDate3 = LocalDateTime.now().plusDays(8).withHour(10).withMinute(30);
                String location3 = "TechInnovation Inc., Salle B, 123 Rue Principale, Montréal";
                String message3 = "Bonjour Alexandre,\n\n" +
                        "Nous vous invitons à un entretien pour le poste de Développeur Full-Stack Junior.\n" +
                        "Apportez une pièce d'identité et votre CV.\n\n" +
                        "Cordialement,\nL'équipe RH";

                convocationService.addConvocation(
                        candidatureDto3.getId(),
                        convocationDate3,
                        location3,
                        message3);
                System.out.println("Convocation créée pour Alexandre, candidature ID: " + candidatureDto3.getId());

                CandidatureDto candidatureAccepted2 = candidatureService.acceptByEmployeur(candidatureDto3.getId());
                System.out.println("Candidature acceptée par l'employeur pour Alexandre: " + candidatureAccepted2);

                EmployeurDto employeurEntente = employeurService.createEmployeur(
                        "Philippe",
                        "Lavoie",
                        "philippe.lavoie@entreprise.com",
                        "Password123",
                        "Solutions Logicielles Pro",
                        Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println("Employeur pour entente créé: " + employeurEntente);

                StudentDto studentEntente = studentService.createStudent(
                        "Émilie",
                        "Fortin",
                        "emilie.fortin@student.com",
                        "Password123",
                        "STU006",
                        Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println("Étudiant pour entente créé: " + studentEntente);

                MultipartFile cvFileEntente = loadPdfFromResources("test.pdf", "CV_Emilie_Fortin.pdf");
                CvDto cvDtoEntente = cvService.uploadCv(studentEntente.getId(), cvFileEntente);
                CvDto cvApprovedEntente = gestionnaireService.approveCv(cvDtoEntente.getId());
                System.out.println("CV approuvé pour Émilie: " + cvApprovedEntente);

                MultipartFile offerFileEntente = loadPdfFromResources("test.pdf", "Offre_Stage_Solutions_Pro.pdf");
                InternshipOfferDto offerDtoEntente = internshipOfferService.createOfferDto(
                        "Stage en développement web",
                        LocalDate.now().plusMonths(1),
                        16,
                        "456 Boulevard Tech, Montréal, QC",
                        28.50f,
                        employeurEntente,
                        offerFileEntente);
                InternshipOfferDto offerApprovedEntente = gestionnaireService.approveOffer(offerDtoEntente.getId());
                System.out.println("Offre approuvée pour entente: " + offerApprovedEntente);

                CandidatureDto candidatureEntente = candidatureService.postuler(
                        studentEntente.getId(),
                        offerApprovedEntente.getId(),
                        cvApprovedEntente.getId());
                System.out.println("Candidature créée pour entente: " + candidatureEntente);

                CandidatureDto candidatureAcceptedEntente = candidatureService.acceptByEmployeur(candidatureEntente.getId());
                System.out.println("Candidature acceptée par l'employeur: " + candidatureAcceptedEntente);
                System.out.println("Statut après acceptation employeur: " + candidatureAcceptedEntente.getStatus());

                CandidatureDto candidatureFullyAccepted = candidatureService.acceptByStudent(
                        candidatureAcceptedEntente.getId(),
                        studentEntente.getId());
                System.out.println("Candidature acceptée par l'étudiant aussi: " + candidatureFullyAccepted);
                System.out.println("Statut final: " + candidatureFullyAccepted.getStatus());

                EntenteStageDto ententeDto = new EntenteStageDto();
                ententeDto.setCandidatureId(candidatureFullyAccepted.getId());
                ententeDto.setDateDebut(offerApprovedEntente.getStartDate());
                ententeDto.setDuree(offerApprovedEntente.getDurationInWeeks());
                ententeDto.setLieu(offerApprovedEntente.getAddress());
                ententeDto.setRemuneration(offerApprovedEntente.getRemuneration());
                ententeDto.setMissionsObjectifs(
                        "L'étudiant(e) sera amené(e) à :\n" +
                                "- Développer des fonctionnalités web en utilisant React et Node.js\n" +
                                "- Participer aux revues de code et aux cérémonies Agile\n" +
                                "- Collaborer avec l'équipe de développement sur des projets clients\n" +
                                "- Améliorer ses compétences en développement full-stack\n\n" +
                                "Objectifs d'apprentissage :\n" +
                                "- Maîtriser les frameworks modernes de développement web\n" +
                                "- Comprendre les méthodologies Agile en entreprise\n" +
                                "- Développer son autonomie et sa capacité à résoudre des problèmes"
                );

                EntenteStageDto ententeCreated = ententeStageService.creerEntente(ententeDto);
                System.out.println("Entente créée avec succès - ID: " + ententeCreated.getId());
                System.out.println("Statut de l'entente: " + ententeCreated.getStatut());
                System.out.println("PDF généré: " + ententeCreated.getCheminDocumentPDF());
                System.out.println("✅ L'entente est maintenant prête à être signée par l'employeur Philippe Lavoie!");


                System.out.println("\n===== CRÉATION DEUXIÈME ENTENTE POUR LUCAS =====");

                StudentDto studentEntente2 = studentService.createStudent(
                        "Lucas",
                        "Bergeron",
                        "lucas.bergeron@student.com",
                        "Password123",
                        "STU007",
                        Program.COMPUTER_SCIENCE.getTranslationKey());
                System.out.println("Deuxième étudiant pour entente créé: " + studentEntente2);

                MultipartFile cvFileEntente2 = loadPdfFromResources("test.pdf", "CV_Lucas_Bergeron.pdf");
                CvDto cvDtoEntente2 = cvService.uploadCv(studentEntente2.getId(), cvFileEntente2);
                CvDto cvApprovedEntente2 = gestionnaireService.approveCv(cvDtoEntente2.getId());

                MultipartFile offerFileEntente2 = loadPdfFromResources("test.pdf", "Offre_Stage_Solutions_Pro_2.pdf");
                InternshipOfferDto offerDtoEntente2 = internshipOfferService.createOfferDto(
                        "Stage en développement mobile",
                        LocalDate.now().plusMonths(2),
                        12,
                        "789 Avenue Innovation, Montréal, QC",
                        26.00f,
                        employeurEntente,
                        offerFileEntente2);
                InternshipOfferDto offerApprovedEntente2 = gestionnaireService.approveOffer(offerDtoEntente2.getId());

                CandidatureDto candidatureEntente2 = candidatureService.postuler(
                        studentEntente2.getId(),
                        offerApprovedEntente2.getId(),
                        cvApprovedEntente2.getId());

                CandidatureDto candidatureAcceptedEntente2 = candidatureService.acceptByEmployeur(candidatureEntente2.getId());
                CandidatureDto candidatureFullyAccepted2 = candidatureService.acceptByStudent(
                        candidatureAcceptedEntente2.getId(),
                        studentEntente2.getId());

                EntenteStageDto ententeDto2 = new EntenteStageDto();
                ententeDto2.setCandidatureId(candidatureFullyAccepted2.getId());
                ententeDto2.setDateDebut(offerApprovedEntente2.getStartDate());
                ententeDto2.setDuree(offerApprovedEntente2.getDurationInWeeks());
                ententeDto2.setLieu(offerApprovedEntente2.getAddress());
                ententeDto2.setRemuneration(offerApprovedEntente2.getRemuneration());
                ententeDto2.setMissionsObjectifs(
                        "L'étudiant(e) sera amené(e) à :\n" +
                                "- Développer des applications mobiles avec React Native\n" +
                                "- Apprendre Flutter et Dart\n" +
                                "- Participer aux tests et au déploiement\n\n" +
                                "Objectifs d'apprentissage :\n" +
                                "- Maîtriser le développement mobile cross-platform\n" +
                                "- Comprendre l'architecture des applications mobiles");

                EntenteStageDto ententeCreated2 = ententeStageService.creerEntente(ententeDto2);
                System.out.println("Deuxième entente créée - ID: " + ententeCreated2.getId());
                System.out.println("✅ Entente de Lucas prête à être signée!");
                System.out.println("===== FIN CRÉATION DEUXIÈME ENTENTE =====\n");


                System.out.println("\n===== STORY 40 : Signature de l'entente par l'étudiant =====");

                try {
                    EntenteStageDto ententeEtudiant40 = ententeCreated;

                    System.out.println("Statut actuel avant signature étudiant : " + ententeEtudiant40.getStatut());

                    EntenteStageDto ententeSigneeEtudiant40 = ententeStageService.signerParEtudiant(
                            ententeEtudiant40.getId(),
                            studentEntente.getId());

                    System.out.println("✍️ Entente STORY 40 signée par l'étudiant : "
                            + ententeSigneeEtudiant40.getDateSignatureEtudiant());

                    System.out.println("Statut après signature étudiant : " + ententeSigneeEtudiant40.getStatut());
                    System.out.println("===== FIN STORY 40 =====\n");

                } catch (Exception e) {
                    System.err.println("Erreur STORY 40 : " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.err.println("Erreur générale non prévue: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

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