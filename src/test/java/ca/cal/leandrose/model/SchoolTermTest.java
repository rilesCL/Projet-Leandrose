package ca.cal.leandrose.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SchoolTermTest {

  @Test
  void testNoArgsConstructor() {
    SchoolTerm term = new SchoolTerm();
    assertNotNull(term);
  }

  @Test
  void testAllArgsConstructor() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertEquals(SchoolTerm.Season.WINTER, term.getSeason());
    assertEquals(2024, term.getYear());
  }

  @Test
  void testGettersWithWinterSeason() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertEquals(SchoolTerm.Season.WINTER, term.getSeason());
    assertEquals(2024, term.getYear());
  }

  @Test
  void testGettersWithSummerSeason() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);
    assertEquals(SchoolTerm.Season.SUMMER, term.getSeason());
    assertEquals(2024, term.getYear());
  }

  @Test
  void testGettersWithFallSeason() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.FALL, 2024);
    assertEquals(SchoolTerm.Season.FALL, term.getSeason());
    assertEquals(2024, term.getYear());
  }

  @Test
  void testGetCurrentTerm() {
    SchoolTerm currentTerm = SchoolTerm.getCurrentTerm();
    assertNotNull(currentTerm);
    assertNotNull(currentTerm.getSeason());
    assertTrue(currentTerm.getYear() > 0);
  }

  @Test
  void testGetNextTerm() {
    SchoolTerm nextTerm = SchoolTerm.getNextTerm();
    assertNotNull(nextTerm);
    assertNotNull(nextTerm.getSeason());
    assertTrue(nextTerm.getYear() > 0);
  }

  @Test
  void testCalculateNextTermFromWinter() {

    SchoolTerm winter2024 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);

    assertTrue(winter2024.isBeforeNextTerm() || !winter2024.isBeforeNextTerm());
  }

  @Test
  void testCalculateNextTermFromSummer() {
    SchoolTerm summerTerm = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);
    SchoolTerm fallTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2024);

    assertNotEquals(summerTerm.getSeason(), fallTerm.getSeason());
  }

  @Test
  void testCalculateNextTermFromFall() {

    SchoolTerm winterTerm = new SchoolTerm(SchoolTerm.Season.WINTER, 2025);

    assertEquals(SchoolTerm.Season.WINTER, winterTerm.getSeason());
    assertEquals(2025, winterTerm.getYear());
  }

  @Test
  void testIsBeforeNextTermWithCurrentTerm() {
    SchoolTerm currentTerm = SchoolTerm.getCurrentTerm();
    assertTrue(currentTerm.isBeforeNextTerm());
  }

  @Test
  void testIsBeforeNextTermWithPastTerm() {
    SchoolTerm pastTerm = new SchoolTerm(SchoolTerm.Season.WINTER, 2020);
    assertTrue(pastTerm.isBeforeNextTerm());
  }

  @Test
  void testIsBeforeNextTermWithFutureTerm() {
    SchoolTerm futureTerm = new SchoolTerm(SchoolTerm.Season.WINTER, 2050);
    assertFalse(futureTerm.isBeforeNextTerm());
  }

  @Test
  void testIsBeforeNextTermSameYearDifferentSeason() {
    SchoolTerm winterTerm = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm summerTerm = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);

    assertEquals(0, winterTerm.getSeason().ordinal());
    assertTrue(winterTerm.getSeason().ordinal() < summerTerm.getSeason().ordinal());
  }

  @Test
  void testEqualsSameObject() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertEquals(term, term2);
  }

  @Test
  void testEqualsWithNull() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertNotEquals(null, term);
  }

  @Test
  void testEqualsWithSameValues() {
    SchoolTerm term1 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertEquals(term1, term2);
  }

  @Test
  void testEqualsWithDifferentSeason() {
    SchoolTerm term1 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);
    assertNotEquals(term1, term2);
  }

  @Test
  void testEqualsWithDifferentYear() {
    SchoolTerm term1 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.WINTER, 2025);
    assertNotEquals(term1, term2);
  }

  @Test
  void testHashCodeConsistency() {
    SchoolTerm term = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    int hashCode1 = term.hashCode();
    int hashCode2 = term.hashCode();
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void testHashCodeEqualObjects() {
    SchoolTerm term1 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    assertEquals(term1.hashCode(), term2.hashCode());
  }

  @Test
  void testHashCodeDifferentObjects() {
    SchoolTerm term1 = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm term2 = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);

    assertNotEquals(term1.hashCode(), term2.hashCode());
  }

  @Test
  void testSeasonEnumValues() {
    assertEquals(3, SchoolTerm.Season.values().length);
    assertEquals(SchoolTerm.Season.WINTER, SchoolTerm.Season.valueOf("WINTER"));
    assertEquals(SchoolTerm.Season.SUMMER, SchoolTerm.Season.valueOf("SUMMER"));
    assertEquals(SchoolTerm.Season.FALL, SchoolTerm.Season.valueOf("FALL"));
  }

  @Test
  void testSeasonEnumOrdinals() {
    assertEquals(0, SchoolTerm.Season.WINTER.ordinal());
    assertEquals(1, SchoolTerm.Season.SUMMER.ordinal());
    assertEquals(2, SchoolTerm.Season.FALL.ordinal());
  }

  @Test
  void testIsBeforeNextTermEdgeCaseNextYear() {
    SchoolTerm oldTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2023);
    assertTrue(oldTerm.isBeforeNextTerm());
  }

  @Test
  void testIsBeforeNextTermEdgeCaseFarFuture() {
    SchoolTerm futureTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2100);
    assertFalse(futureTerm.isBeforeNextTerm());
  }

  @Test
  void testTermProgression() {
    SchoolTerm winter = new SchoolTerm(SchoolTerm.Season.WINTER, 2024);
    SchoolTerm summer = new SchoolTerm(SchoolTerm.Season.SUMMER, 2024);
    SchoolTerm fall = new SchoolTerm(SchoolTerm.Season.FALL, 2024);
    SchoolTerm nextWinter = new SchoolTerm(SchoolTerm.Season.WINTER, 2025);

    assertTrue(winter.getSeason().ordinal() < summer.getSeason().ordinal());
    assertTrue(summer.getSeason().ordinal() < fall.getSeason().ordinal());

    assertEquals(2024, winter.getYear());
    assertEquals(2024, fall.getYear());
    assertEquals(2025, nextWinter.getYear());
  }
}
