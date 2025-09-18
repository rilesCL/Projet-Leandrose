package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.StudentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateStudent() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPass";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        Student savedStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@student.com")
                .password(encodedPassword)
                .studentNumber("STU001")
                .program("Computer Science")
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentDto dto = studentService.createStudent(
                "John", "Doe", "john.doe@student.com", rawPassword, "STU001", "Computer Science"
        );

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@student.com", dto.getEmail());
        assertEquals("STU001", dto.getStudentNumber());
        assertEquals("Computer Science", dto.getProgram());

        // Verify that passwordEncoder.encode was called
        verify(passwordEncoder).encode(rawPassword);

        // Verify that studentRepository.save was called with correct data
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertEquals(encodedPassword, captor.getValue().getPassword());
    }

    @Test
    void testGetStudentByIdFound() {
        Student student = Student.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@student.com")
                .password("pass")
                .studentNumber("STU002")
                .program("Mathematics")
                .build();

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        StudentDto dto = studentService.getStudentById(2L);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Alice", dto.getFirstName());
    }

    @Test
    void testGetStudentByIdNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> studentService.getStudentById(99L));
    }
}