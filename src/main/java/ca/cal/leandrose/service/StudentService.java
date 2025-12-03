package ca.cal.leandrose.service;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.EntenteStageRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import ca.cal.leandrose.service.dto.ProfDto;
import ca.cal.leandrose.service.dto.StudentDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final EntenteStageRepository ententeStageRepository;

  @Transactional
  public StudentDto createStudent(
      String firstName,
      String lastName,
      String email,
      String rawPassword,
      String studentNumber,
      String program) {
    if (studentRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Cet email est déjà utilisé");
    }
    Student student =
        Student.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .studentNumber(studentNumber)
            .program(program)
            .internshipTerm(null)
            .build();

    Student savedStudent = studentRepository.save(student);
    System.out.println(savedStudent.getProgram());
    System.out.println(savedStudent.getTermAsString());
    return StudentDto.create(savedStudent);
  }

  @Transactional
  public StudentDto createStudentwithTerm(
      String firstName,
      String lastName,
      String email,
      String rawPassword,
      String studentNumber,
      String program,
      SchoolTerm internshipTerm) {
    if (studentRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Cet email est déjà utilisé");
    }
    Student student =
        Student.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .studentNumber(studentNumber)
            .program(program)
            .internshipTerm(internshipTerm)
            .build();

    Student savedStudent = studentRepository.save(student);
    return StudentDto.create(savedStudent);
  }

  @Transactional
  public StudentDto getStudentById(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException();
    }
    Optional<Student> student = studentRepository.findById(id);
    if (student.isEmpty()) {
      throw new UserNotFoundException();
    }
    return StudentDto.create(student.get());
  }

  @Transactional
  public StudentDto updateStudentInfo(Long studentId, String newProgram) {
    if (newProgram == null || newProgram.isBlank()) {
      throw new IllegalArgumentException("Le programme ne peut pas être vide");
    }

    Student student = studentRepository.findById(studentId).orElseThrow(UserNotFoundException::new);

    student.setProgram(newProgram);

    student.setInternshipTerm(SchoolTerm.getNextTerm());

    Student updated = studentRepository.save(student);

    return StudentDto.create(updated);
  }

  @Transactional(readOnly = true)
  public Optional<ProfDto> getProfByStudentId(Long studentId) {
    if (studentId == null || studentId <= 0) {
      return Optional.empty();
    }

    List<EntenteStage> ententes =
        ententeStageRepository.findAll().stream()
            .filter(
                entente -> {
                  Long studentEntenteId = entente.getCandidature().getStudent().getId();
                  return studentEntenteId != null && studentEntenteId.equals(studentId);
                })
            .filter(entente -> entente.getProf() != null)
            .sorted(
                (e1, e2) -> {
                  if (e1.getDateModification() == null && e2.getDateModification() == null) {
                    return 0;
                  }
                  if (e1.getDateModification() == null) {
                    return 1;
                  }
                  if (e2.getDateModification() == null) {
                    return -1;
                  }
                  return e2.getDateModification().compareTo(e1.getDateModification());
                })
            .collect(Collectors.toList());

    return ententes.stream()
        .findFirst()
        .map(EntenteStage::getProf)
        .map(prof -> ProfDto.create(prof));
  }

  @Transactional(readOnly = true)
  public Optional<GestionnaireDto> getGestionnaireByStudentId(Long studentId) {
    if (studentId == null || studentId <= 0) {
      return Optional.empty();
    }

    List<EntenteStage> ententes =
        ententeStageRepository.findAll().stream()
            .filter(
                entente -> {
                  Long studentEntenteId = entente.getCandidature().getStudent().getId();
                  return studentEntenteId != null && studentEntenteId.equals(studentId);
                })
            .filter(entente -> entente.getGestionnaire() != null)
            .sorted(
                (e1, e2) -> {
                  if (e1.getDateModification() == null && e2.getDateModification() == null) {
                    return 0;
                  }
                  if (e1.getDateModification() == null) {
                    return 1;
                  }
                  if (e2.getDateModification() == null) {
                    return -1;
                  }
                  return e2.getDateModification().compareTo(e1.getDateModification());
                })
            .collect(Collectors.toList());

    return ententes.stream()
        .findFirst()
        .map(EntenteStage::getGestionnaire)
        .map(gestionnaire -> GestionnaireDto.create(gestionnaire));
  }

  @Transactional(readOnly = true)
  public List<EmployeurDto> getEmployeursByStudentId(Long studentId) {
    if (studentId == null || studentId <= 0) {
      return List.of();
    }

    return ententeStageRepository.findAll().stream()
        .filter(
            entente -> {
              Long studentEntenteId = entente.getCandidature().getStudent().getId();
              return studentEntenteId != null && studentEntenteId.equals(studentId);
            })
        .map(EntenteStage::getEmployeur)
        .filter(employeur -> employeur != null)
        .distinct()
        .map(employeur -> EmployeurDto.create(employeur))
        .collect(Collectors.toList());
  }
}
