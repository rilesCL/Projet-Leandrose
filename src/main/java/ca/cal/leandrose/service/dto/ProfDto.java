package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Prof;
import ca.cal.leandrose.model.auth.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
public class ProfDto extends UserDTO {
  private String employeeNumber;
  private String department;
  private Map<String, String> error;

  @Builder
  public ProfDto(
      Long id,
      String firstName,
      String lastName,
      String email,
      Role role,
      String employeeNumber,
      String department) {
    super(id, firstName, lastName, email, role);
    this.employeeNumber = employeeNumber;
    this.department = department;
  }

  public ProfDto() {}

  public ProfDto(String error) {
    this.error = Map.of("error", error);
  }

  public static ProfDto create(Prof prof) {
    return ProfDto.builder()
        .id(prof.getId())
        .firstName(prof.getFirstName())
        .lastName(prof.getLastName())
        .email(prof.getEmail())
        .role(prof.getRole())
        .employeeNumber(prof.getEmployeeNumber())
        .department(prof.getDepartment())
        .build();
  }

  public static ProfDto empty() {
    return new ProfDto();
  }
}

