// RejectOfferRequest.java
package ca.cal.leandrose.presentation.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RejectOfferRequest {

    @NotBlank(message = "Un commentaire est obligatoire pour rejeter une offre")
    private String comment;
}