package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Prof;
import ca.cal.leandrose.model.auth.Role;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class ProfDto extends UserDTO {
  private String employeeNumber;
  private String nameCollege;
  private String address;
  private String fax_machine;
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
      String nameCollege,
      String address,
      String fax_machine,
      String department) {
    super(id, firstName, lastName, email, role);
    this.employeeNumber = employeeNumber;
    this.nameCollege = nameCollege;
    this.address = address;
    this.fax_machine = fax_machine;
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
        .nameCollege(prof.getNameCollege())
        .address(prof.getAddress())
        .fax_machine(prof.getFax_machine())
        .department(prof.getDepartment())
        .build();
  }

  public static ProfDto empty() {
    return new ProfDto();
  }
}
