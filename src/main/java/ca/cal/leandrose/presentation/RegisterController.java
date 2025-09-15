package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final EmployeurService employeurService;

    @PostMapping("/employeur")
    public ResponseEntity<EmployeurDto> registerEmployeur(@RequestBody RegisterEmployeur request) {
        EmployeurDto employeurDto = employeurService.createEmployeur(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getCompanyName(),
                request.getField(),
                request.getSince()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(employeurDto);
    }


}
