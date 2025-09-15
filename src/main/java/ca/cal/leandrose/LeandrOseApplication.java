package ca.cal.leandrose;

import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LeandrOseApplication {



    public static void main(String[] args) {
        SpringApplication.run(LeandrOseApplication.class, args);
    }
    @Bean
    public CommandLineRunner Lsc0SE(EmployeurService employeurService) {
    return args -> {
      try {
        EmployeurDto empDto =
            employeurService.createEmployeur(
                "Leandro", "Schoonewolff", "wbbey@gmail.com", "mansang", "macolo", "alimentation");
        System.out.println(employeurService.getEmployeurById(1L));
      } catch (Exception e) {
        System.err.println("Erreur générale non prévue: " + e.getMessage());
        e.printStackTrace();
      }
    };
    }
}


