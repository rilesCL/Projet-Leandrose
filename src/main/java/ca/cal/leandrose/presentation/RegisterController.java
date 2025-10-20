package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.presentation.request.RegisterStudent;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.StudentService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.ProgramDto;
import ca.cal.leandrose.service.dto.StudentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "http://localhost:5173")
public class RegisterController {

    private final EmployeurService employeurService;
    private final StudentService studentService;
    private final GestionnaireService gestionnaireService;

    public RegisterController(EmployeurService employeurService, StudentService studentService, GestionnaireService gestionnaireService) {
        this.employeurService = employeurService;
        this.studentService = studentService;
        this.gestionnaireService = gestionnaireService;
    }
    @GetMapping("/programs")
    public ResponseEntity<List<ProgramDto>> getPrograms() {
        return ResponseEntity.ok(gestionnaireService.getAllPrograms());
    }


    @PostMapping("/student")
    public ResponseEntity<StudentDto> registerStudent(@Valid @RequestBody RegisterStudent request) {
        try {
            StudentDto studentDto = studentService.createStudent(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getStudentNumber(),
                    request.getProgram()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(studentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new StudentDto(e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new StudentDto("Cet email est déjà utilisé"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StudentDto("Erreur serveur"));
        }
    }

    @PostMapping("/employeur")
    public ResponseEntity<?> registerEmployeur(@Valid @RequestBody RegisterEmployeur request) {
        try {
            EmployeurDto employeurDto = employeurService.createEmployeur(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getCompanyName(),
                    request.getField()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(employeurDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Cet email est déjà utilisé"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur serveur"));
        }
    }
}
