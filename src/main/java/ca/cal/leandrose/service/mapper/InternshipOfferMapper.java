package ca.cal.leandrose.service.mapper;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;

public class InternshipOfferMapper {
  public static InternshipOfferDto toDto(InternshipOffer offer) {
    if (offer == null) return null;

    Employeur employeur = offer.getEmployeur();

    return InternshipOfferDto.builder()
        .id(offer.getId())
        .description(offer.getDescription())
        .startDate(offer.getStartDate())
        .durationInWeeks(offer.getDurationInWeeks())
        .address(offer.getAddress())
        .remuneration(offer.getRemuneration())
        .status(offer.getStatus() != null ? offer.getStatus().name() : "PENDING_VALIDATION")
        .validationDate(offer.getValidationDate())
        .employeurId(offer.getEmployeurId())
        .companyName(offer.getCompanyName())
        .pdfPath(offer.getPdfPath())
        .rejectionComment(offer.getRejectionComment())
        .employeurDto(employeur != null ? EmployeurDto.create(employeur) : null)
        .build();
  }
}
