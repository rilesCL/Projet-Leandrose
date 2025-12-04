package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Program;
import org.junit.jupiter.api.Test;

class ProgramDtoTest {

  @Test
  void testProgramDtoFromEnum() {
    ProgramDto dto = ProgramDto.fromEnum(Program.COMPUTER_SCIENCE);

    assertNotNull(dto);
    assertEquals("COMPUTER_SCIENCE", dto.code());
    assertEquals("program.computer_science", dto.translationKey());
  }

  @Test
  void testProgramDtoFromEnum_WithDifferentProgram() {
    ProgramDto dto = ProgramDto.fromEnum(Program.SOFTWARE_ENGINEERING);

    assertNotNull(dto);
    assertEquals("SOFTWARE_ENGINEERING", dto.code());
    assertEquals("program.software_engineering", dto.translationKey());
  }

  @Test
  void testProgramDtoRecord() {
    ProgramDto dto = new ProgramDto("TEST_CODE", "test.translation.key");

    assertEquals("TEST_CODE", dto.code());
    assertEquals("test.translation.key", dto.translationKey());
  }

  @Test
  void testProgramDtoFromEnum_AllPrograms() {
    for (Program program : Program.values()) {
      ProgramDto dto = ProgramDto.fromEnum(program);
      assertNotNull(dto);
      assertEquals(program.name(), dto.code());
      assertEquals(program.getTranslationKey(), dto.translationKey());
    }
  }
}
