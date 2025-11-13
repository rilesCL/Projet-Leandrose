package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Prof;
import ca.cal.leandrose.repository.ProfRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.ProfDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfService {
  private final ProfRepository profRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public ProfDto createProf(
      String firstName,
      String lastName,
      String email,
      String rawPassword,
      String employeeNumber,
      String nameCollege,
      String address,
      String fax_machine,
      String department) {
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new IllegalArgumentException("Le prénom est obligatoire");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new IllegalArgumentException("Le nom est obligatoire");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("L'email est obligatoire");
    }
    if (rawPassword == null || rawPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Le mot de passe est obligatoire");
    }
    if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("Le numéro d'employé est obligatoire");
    }

    if (profRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Cet email est déjà utilisé");
    }
    if (profRepository.existsByEmployeeNumber(employeeNumber)) {
      throw new IllegalArgumentException("Ce numéro d'employé est déjà utilisé");
    }

    Prof prof =
        Prof.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .employeeNumber(employeeNumber)
            .nameCollege(nameCollege)
            .address(address)
            .fax_machine(fax_machine)
            .department(department)
            .build();

    Prof savedProf = profRepository.save(prof);
    return ProfDto.create(savedProf);
  }

  @Transactional(readOnly = true)
  public ProfDto getProfById(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("L'id doit être valide");
    }

    Optional<Prof> prof = profRepository.findById(id);
    if (prof.isEmpty()) {
      throw new UserNotFoundException();
    }

    return ProfDto.create(prof.get());
  }

  @Transactional(readOnly = true)
  public List<ProfDto> getAllProfs() {
    return profRepository.findAll().stream()
        .map(ProfDto::create)
        .collect(Collectors.toList());
  }
}

