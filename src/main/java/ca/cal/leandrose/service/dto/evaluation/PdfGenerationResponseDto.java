package ca.cal.leandrose.service.dto.evaluation;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfGenerationResponseDto {
  private String pdfpath;
  private String message;
  private Map<String, String> error;

  public static PdfGenerationResponseDto fromResponse(PdfGenerationResponse response) {
    return PdfGenerationResponseDto.builder()
        .pdfpath(response.pdfpath())
        .message(response.message())
        .build();
  }

  public static PdfGenerationResponseDto withErrorMessage(String message) {
    PdfGenerationResponseDto dto = new PdfGenerationResponseDto();
    dto.setError(Map.of("message", message));
    return dto;
  }
}
