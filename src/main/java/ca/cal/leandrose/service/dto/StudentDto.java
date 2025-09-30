package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.model.auth.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
public class StudentDto extends UserDTO{
    private String studentNumber;
    private String program;
    private Map<String,String> error;

    @Builder
    public StudentDto(Long id, String firstName, String lastName, String email, Role role, String studentNumber, String program) {
        super(id, firstName, lastName, email, role);
        this.studentNumber = studentNumber;
        this.program = program;
    }

    public StudentDto(){}

    public StudentDto(String error){
        this.error = Map.of("error", error);
    }

    public static StudentDto create(Student student){
    return StudentDto.builder()
        .id(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .role(student.getRole())
        .studentNumber(student.getStudentNumber())
        .program(student.getProgram())
        .build();
    }

    public static StudentDto empty() {
        return new StudentDto();
    }
}
