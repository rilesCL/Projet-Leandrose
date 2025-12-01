package ca.cal.leandrose.service.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfDownloadResponseDto {
    private byte[] pdfBytes;
    private String filename;
    private Map<String, String> error;

    public static PdfDownloadResponseDto withErrorMessage(String message) {
        PdfDownloadResponseDto dto = new PdfDownloadResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}

