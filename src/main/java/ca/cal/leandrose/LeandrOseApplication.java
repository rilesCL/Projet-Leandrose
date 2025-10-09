package ca.cal.leandrose;
import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.StudentService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.dto.StudentDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;


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
            GestionnaireService gestionnaireService) {

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
          System.out.println("Student créé: " + studentService.getStudentById(studentDto.getId()));

        GestionnaireDto gestionnaireDto =
            gestionnaireService.createGestionnaire(
                "Jean", "Dupont", "gestionnaire@test.com", "Password123!", "514-123-4567");
        System.out.println("Gestionnaire créé: " + gestionnaireDto);

      } catch (Exception e) {
        System.err.println("Erreur générale non prévue: " + e.getMessage());
        e.printStackTrace();
      }
    };
    }
}