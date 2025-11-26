package ca.cal.leandrose.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectOfferRequest {

  @NotBlank(message = "Un commentaire est obligatoire pour rejeter une offre")
  private String comment;
}
