package ca.cal.leandrose.presentation.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String firstName;
    private String lastName;

    private String phoneNumber;

    private String companyName;
    private String field;

    private String program;
    private String studentNumber;
}
