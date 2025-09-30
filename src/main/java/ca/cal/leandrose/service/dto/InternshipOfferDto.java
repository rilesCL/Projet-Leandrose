package ca.cal.leandrose.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InternshipOfferDto {
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
}
