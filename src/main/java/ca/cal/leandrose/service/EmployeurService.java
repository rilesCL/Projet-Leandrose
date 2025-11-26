package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.EmployeurDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeurService {

  private final EmployeurRepository employeurRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public EmployeurDto createEmployeur(
      String firstName,
      String lastName,
      String email,
      String rawPassword,
      String companyName,
      String field) {
    if (employeurRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Cet email est déjà utilisé");
    }

    Employeur employeur =
        Employeur.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .companyName(companyName)
            .field(field)
            .build();

    Employeur savedEmployeur = employeurRepository.save(employeur);
    return EmployeurDto.create(savedEmployeur);
  }

  @Transactional
  public EmployeurDto getEmployeurById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException();
    }
    Optional<Employeur> emp = employeurRepository.findById(id);
    if (emp.isEmpty()) {
      throw new UserNotFoundException();
    }
    return EmployeurDto.create(emp.get());
  }
}
