package ca.cal.leandrose.service;


import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.StudentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CvRepository cvRepository;


    @Transactional
    public StudentDto createStudent(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            String studentNumber,
            String program
    ) {
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        Student student = Student.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .studentNumber(studentNumber)
                .program(program)
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

}
