package ca.cal.leandrose.presentation.request;

import ca.cal.leandrose.presentation.request.UpdateStudentInfoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateStudentInfoRequestTest {

  @Test
  @DisplayName("Valid UpdateStudentInfoRequest should be created")
  void testValidUpdateStudentInfoRequest() {
    UpdateStudentInfoRequest request = new UpdateStudentInfoRequest("Computer Science");

    assertNotNull(request);
    assertEquals("Computer Science", request.getProgram());
  }

  @Test
  @DisplayName("No-args constructor should work")
  void testNoArgsConstructor() {
    UpdateStudentInfoRequest request = new UpdateStudentInfoRequest();

    assertNotNull(request);
    assertNull(request.getProgram());
  }

  @Test
  @DisplayName("Setter should update program")
  void testSetter() {
    UpdateStudentInfoRequest request = new UpdateStudentInfoRequest();
    request.setProgram("Software Engineering");

    assertEquals("Software Engineering", request.getProgram());
  }

  @Test
  @DisplayName("Null program should be allowed")
  void testNullProgram() {
    UpdateStudentInfoRequest request = new UpdateStudentInfoRequest(null);

    assertNull(request.getProgram());
  }
}
