package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.auth.Role;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class EmployeurDto extends UserDTO {
  private String companyName;
  private String field;
  private String phoneNumber;
  private Map<String, String> error;

  @Builder
  public EmployeurDto(
      Long id,
      String firstName,
      String lastname,
      String email,
      Role role,
      String companyName,
      String field,
      String phoneNumber) {
    super(id, firstName, lastname, email, role);
    this.companyName = companyName;
    this.field = field;
    this.phoneNumber = phoneNumber;
  }

  public EmployeurDto() {}

  public static EmployeurDto create(Employeur employeur) {
    return EmployeurDto.builder()
        .id(employeur.getId())
        .firstName(employeur.getFirstName())
        .lastname(employeur.getLastName())
        .email(employeur.getEmail())
        .role(employeur.getRole())
        .companyName(employeur.getCompanyName())
        .field(employeur.getField())
        .phoneNumber(employeur.getPhoneNumber())
        .build();
  }

  public static EmployeurDto empty() {
    return new EmployeurDto();
  }
}
