package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipOffer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String description;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private int durationInWeeks;

  @ManyToOne private Gestionnaire validatedBy;

  @Column private LocalDate validationDate;

  @Column(nullable = false)
  private String address;

  @Column private Float remuneration;

  @ManyToOne private Employeur employeur;

  private String pdfPath;

  @Embedded private SchoolTerm schoolTerm;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column private String rejectionComment;

  public enum Status {
    PENDING_VALIDATION,
    PUBLISHED,
    ASSIGNED,
    ARCHIVED,
    REJECTED
  }

  public Long getEmployeurId() {
    return employeur != null ? employeur.getId() : null;
  }

  public String getCompanyName() {
    return employeur != null ? employeur.getCompanyName() : null;
  }

  public String getEmployeurFirstName() {
    return employeur != null ? employeur.getFirstName() : null;
  }

  public String getEmployeurLastName() {
    return employeur != null ? employeur.getLastName() : null;
  }

  public String getEmployeurEmail() {
    return employeur != null && employeur.getCredentials() != null
        ? employeur.getCredentials().getEmail()
        : null;
  }
  public String getTerm(){
      return (schoolTerm != null) ? schoolTerm.getTermAsString() : null;
  }
}
