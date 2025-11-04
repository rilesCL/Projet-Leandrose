package ca.cal.leandrose.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Program {
  COMPUTER_SCIENCE("program.computer_science"),
  SOFTWARE_ENGINEERING("program.software_engineering"),
  INFORMATION_TECHNOLOGY("program.information_technology"),
  DATA_SCIENCE("program.data_science"),
  CYBER_SECURITY("program.cyber_security"),
  ARTIFICIAL_INTELLIGENCE("program.artificial_intelligence"),
  ELECTRICAL_ENGINEERING("program.electrical_engineering"),
  MECHANICAL_ENGINEERING("program.mechanical_engineering"),
  CIVIL_ENGINEERING("program.civil_engineering"),
  CHEMICAL_ENGINEERING("program.chemical_engineering"),
  BIOMEDICAL_ENGINEERING("program.biomedical_engineering"),
  BUSINESS_ADMINISTRATION("program.business_administration"),
  ACCOUNTING("program.accounting"),
  FINANCE("program.finance"),
  ECONOMICS("program.economics"),
  MARKETING("program.marketing"),
  MANAGEMENT("program.management"),
  PSYCHOLOGY("program.psychology"),
  SOCIOLOGY("program.sociology"),
  POLITICAL_SCIENCE("program.political_science"),
  INTERNATIONAL_RELATIONS("program.international_relations"),
  LAW("program.law"),
  EDUCATION("program.education"),
  LITERATURE("program.literature"),
  HISTORY("program.history"),
  PHILOSOPHY("program.philosophy"),
  LINGUISTICS("program.linguistics"),
  BIOLOGY("program.biology"),
  CHEMISTRY("program.chemistry"),
  PHYSICS("program.physics"),
  MATHEMATICS("program.mathematics"),
  STATISTICS("program.statistics"),
  ENVIRONMENTAL_SCIENCE("program.environmental_science"),
  MEDICINE("program.medicine"),
  NURSING("program.nursing"),
  PHARMACY("program.pharmacy"),
  DENTISTRY("program.dentistry"),
  ARCHITECTURE("program.architecture"),
  FINE_ARTS("program.fine_arts"),
  MUSIC("program.music"),
  THEATER("program.theater"),
  FILM_STUDIES("program.film_studies"),
  COMMUNICATION("program.communication"),
  JOURNALISM("program.journalism"),
  DESIGN("program.design"),
  ANTHROPOLOGY("program.anthropology"),
  GEOGRAPHY("program.geography"),
  SPORTS_SCIENCE("program.sports_science");

  private final String translationKey;

  Program(String translationKey) {
    this.translationKey = translationKey;
  }

  @JsonValue
  public String getTranslationKey() {
    return translationKey;
  }
}
