package ca.cal.leandrose.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConvocationRequest {
    @NotNull(message = "La date est requise")
    @Future(message = "La date doit être future")
    private LocalDateTime convocationDate;

    @NotBlank(message = "Le lieu est requis")
    private String location;

    private String message;
}