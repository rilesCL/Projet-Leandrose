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
            ConvocationService convocationService) {

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

                // === Nouveau code pour la convocation ===

                // 1. Créer un nouvel employeur pour la convocation
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
                System.out.println("CV créé pour l'étudiant: " + cvDto);

                CvDto cvApproved = gestionnaireService.approveCv(cvDto.getId());
                System.out.println("CV approuvé: " + cvApproved);

                MultipartFile offerFile = loadPdfFromResources("test.pdf", "Offre_Stage_TechInnovation.pdf");
                InternshipOfferDto offerDto = internshipOfferService.createOfferDto(
                        "Développeur Full-Stack Junior",
                        LocalDate.now().plusMonths(2),
                        12,
                        "123 Rue Principale, Montréal, QC",
                        25.00f,
                        employeurConvocation,
                        offerFile);
                System.out.println("Offre de stage créée: " + offerDto);

                InternshipOfferDto offerApproved = gestionnaireService.approveOffer(offerDto.getId());
                System.out.println("Offre de stage approuvée: " + offerApproved);

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

            } catch (Exception e) {
                System.err.println("Erreur générale non prévue: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    /**
     * Charge un fichier PDF depuis les resources
     */
    private MultipartFile loadPdfFromResources(String resourcePath, String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);

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
            public byte[] getBytes() throws IOException {
                return content;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(content);
                }
            }
        }
}