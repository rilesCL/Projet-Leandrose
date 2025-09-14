package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.service.dto.EmployeurDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeurService {

    private final EmployeurRepository employeurRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeurDto createEmployeur(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            String companyName,
            String field,
            LocalDate since
    ) {
        Employeur employeur = Employeur.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .companyName(companyName)
                .field(field)
                .since(since)
                .build();

        Employeur savedEmployeur = employeurRepository.save(employeur);

        return EmployeurDto.create(savedEmployeur);
    }
}
