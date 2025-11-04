package ca.cal.leandrose.security.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends APIException {
  public UserNotFoundException() {
    super(HttpStatus.NOT_FOUND, "userNotFound");
  }
}
