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
public class CvDownloadResponseDto {
    private String pdfPath;
    private String filename;
    private Map<String, String> error;

    public static CvDownloadResponseDto withErrorMessage(String message) {
        CvDownloadResponseDto dto = new CvDownloadResponseDto();
        dto.setError(Map.of("message", message));
        return dto;
    }
}
