package ca.cal.leandrose.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.Month;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolTerm {
  @Enumerated(EnumType.STRING)
  private Season season;
  private int year;

  public static SchoolTerm getNextTerm() {
    return getCurrentTerm().calculateNextTerm();
  }

  public static SchoolTerm getCurrentTerm() {
    LocalDate now = LocalDate.now();
    int currentYear = now.getYear();
    Month currentMonth = now.getMonth();
    Season currentSeason;

    if (currentMonth.compareTo(Month.MAY) <= 0) {
      currentSeason = Season.WINTER;
    } else if (currentMonth.compareTo(Month.AUGUST) <= 0) {
      currentSeason = Season.SUMMER;
    } else {
      currentSeason = Season.FALL;
    }

    return new SchoolTerm(currentSeason, currentYear);
  }

  private SchoolTerm calculateNextTerm() {
    Season nextSeason;
    int nextYear = this.year;

    switch (this.season) {
      case WINTER:
        nextSeason = Season.SUMMER;
        break;
      case SUMMER:
        nextSeason = Season.FALL;
        break;
      case FALL:
        nextSeason = Season.WINTER;
        nextYear = this.year + 1;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + this.season);
    }

    return new SchoolTerm(nextSeason, nextYear);
  }

  public boolean isBeforeNextTerm() {
    SchoolTerm nextTerm = getNextTerm();
    return this.year < nextTerm.year
        || (this.year == nextTerm.year && this.season.ordinal() < nextTerm.season.ordinal());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SchoolTerm that = (SchoolTerm) o;
    return year == that.year && season == that.season;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(season, year);
  }

  public String getTermAsString() {
    return (getSeason() != null)? getSeason() + " " + getYear():null;
  }

  public enum Season {
    WINTER,
    SUMMER,
    FALL
  }
}