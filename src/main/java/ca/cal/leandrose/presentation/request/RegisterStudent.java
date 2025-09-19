package ca.cal.leandrose.presentation.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterStudent {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "last name is required")
    private String lastName;

    @NotBlank(message = "Email name is required")
    private String email;


    @NotBlank()
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;


    @NotBlank(message = "numero matricule is required")
    private String studentNumber;

    @NotBlank(message = "Name of the program is required")
    private String program;
}
