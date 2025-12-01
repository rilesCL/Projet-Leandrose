package ca.cal.leandrose.service.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternshipOfferDto {
  String rejectionComment;
  private Long id;
  private String description;
  private LocalDate startDate;
  private int durationInWeeks;
  private String address;
  private Float remuneration;
  private String status;
  private Long employeurId;
  private String companyName;
  private String pdfPath;
  private String schoolTerm;
  private String errorMessage;
  private LocalDate validationDate;
  private EmployeurDto employeurDto;

  public InternshipOfferDto(
      Long id,
      String description,
      LocalDate startDate,
      int durationInWeeks,
      String address,
      Float remuneration,
      String status,
      EmployeurDto employeurDto,
      String pdfPath,
      String schoolTerm) {
    this.id = id;
    this.description = description;
    this.startDate = startDate;
    this.durationInWeeks = durationInWeeks;
    this.address = address;
    this.remuneration = remuneration;
    this.status = status;
    this.employeurDto = employeurDto;
    this.pdfPath = pdfPath;
    this.schoolTerm = schoolTerm;
    this.rejectionComment = null;
  }

  public InternshipOfferDto(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public static InternshipOfferDto withErrorMessage(String message) {
    return new InternshipOfferDto(message);
  }
}
