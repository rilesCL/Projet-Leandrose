package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "candidature_id", nullable = false, unique = true)
  private Candidature candidature;

  @Column(nullable = false)
  private LocalDateTime convocationDate;

  @Column(nullable = false)
  private String location;

  @Column(nullable = false)
  private String personnalMessage;

  public Convocation(
      Candidature candidature,
      LocalDateTime convocationDate,
      String location,
      String personnalMessage) {
    this.personnalMessage = personnalMessage;
    this.candidature = candidature;
    this.convocationDate = convocationDate;
    this.location = location;
  }
}
