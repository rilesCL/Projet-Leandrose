package ca.cal.leandrose.presentation;

import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.dto.ProfStudentItemDto;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prof")
@CrossOrigin(origins = {"http://localhost:5173"})
@RequiredArgsConstructor
public class ProfController {

    private final EntenteStageService ententeStageService;

    @GetMapping("/{profId}/etudiants")
    public List<ProfStudentItemDto> getEtudiantsAttribues(
            @PathVariable @Min(1) Long profId,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String entreprise,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String evaluationStatus,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "true") Boolean asc
    ) {
        return ententeStageService.getEtudiantsPourProf(
                profId,
                nom,
                entreprise,
                dateFrom,
                dateTo,
                evaluationStatus,
                sortBy,
                asc
        );
    }
}
