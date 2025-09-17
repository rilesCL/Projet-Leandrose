package ca.cal.leandrose.service;


import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.service.dto.StudentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public StudentDto createStudent(
            String firstName,
            String lastName,
            String rawPassword,
            String email,
            String programme,
            String numero_matricule
    ){
        try{
            Student student = Student.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .program(programme)
                    .studentNumber(numero_matricule)
                    .build();
            Student savedStudent = studentRepository.save(student);
            return StudentDto.create(savedStudent);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
