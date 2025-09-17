package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.presentation.request.RegisterStudent;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.StudentService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.StudentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "http://localhost:5173")
public class RegisterController {

    private final EmployeurService employeurService;
    private final StudentService studentService;

    public RegisterController(EmployeurService employeurService, StudentService studentService) {
        this.employeurService = employeurService;
        this.studentService = studentService;
    }

    @PostMapping("/employeur")
    public ResponseEntity<EmployeurDto> registerEmployeur(@Valid @RequestBody RegisterEmployeur request) {
        EmployeurDto employeurDto = employeurService.createEmployeur(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getCompanyName(),
                request.getField()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(employeurDto);
    }

    @PostMapping("/student")
    public ResponseEntity<StudentDto> registerStudent(@Valid @RequestBody RegisterStudent request){
        StudentDto studentDto = studentService.createStudent(
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getEmail(),
                request.getProgramme(),
                request.getNumero_matricule()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(studentDto);
    }
}
