package ca.cal.leandrose.presentation.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String firstName;
    private String lastName;

    private String email;
    private String newPassword;
    private String currentPassword;

    private String phoneNumber;
}
