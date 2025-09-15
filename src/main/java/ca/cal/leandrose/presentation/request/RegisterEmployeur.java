package ca.cal.leandrose.presentation.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterEmployeur {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String companyName;
    private String field;
    private LocalDate since;
}
