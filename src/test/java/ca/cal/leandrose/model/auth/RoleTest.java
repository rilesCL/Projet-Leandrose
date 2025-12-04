package ca.cal.leandrose.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void testRoleEnumValues() {
    assertNotNull(Role.GESTIONNAIRE);
    assertNotNull(Role.EMPLOYEUR);
    assertNotNull(Role.STUDENT);
    assertNotNull(Role.PROF);
  }

  @Test
  void testRoleToString() {
    assertEquals("ROLE_GESTIONNAIRE", Role.GESTIONNAIRE.toString());
    assertEquals("ROLE_EMPLOYEUR", Role.EMPLOYEUR.toString());
    assertEquals("ROLE_STUDENT", Role.STUDENT.toString());
    assertEquals("ROLE_PROF", Role.PROF.toString());
  }

  @Test
  void testRoleValueOf() {
    assertEquals(Role.GESTIONNAIRE, Role.valueOf("GESTIONNAIRE"));
    assertEquals(Role.EMPLOYEUR, Role.valueOf("EMPLOYEUR"));
    assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
    assertEquals(Role.PROF, Role.valueOf("PROF"));
  }

  @Test
  void testRoleValues() {
    Role[] values = Role.values();
    assertEquals(4, values.length);
    assertTrue(java.util.Arrays.asList(values).contains(Role.GESTIONNAIRE));
    assertTrue(java.util.Arrays.asList(values).contains(Role.EMPLOYEUR));
    assertTrue(java.util.Arrays.asList(values).contains(Role.STUDENT));
    assertTrue(java.util.Arrays.asList(values).contains(Role.PROF));
  }

  @Test
  void testGestionnaireManagedRoles() {
    assertNotNull(Role.GESTIONNAIRE);
  }
}
