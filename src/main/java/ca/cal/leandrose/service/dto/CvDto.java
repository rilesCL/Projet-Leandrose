package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Cv;
import lombok.Builder;
import lombok.Data;

@Data
public class CvDto {

    private Long id;
    private Long studentId;
    private String pdfPath;
    private Cv.Status status;

    @Builder
    public CvDto(Long id, Long studentId, String pdfPath, Cv.Status status) {
        this.id = id;
        this.studentId = studentId;
        this.pdfPath = pdfPath;
        this.status = status;
    }

    public CvDto() {}

    public static CvDto create(Cv cv) {
        Long sid = null;
        if (cv != null && cv.getStudent() != null) {
            sid = cv.getStudent().getId();
        }
        Cv.Status st = cv != null ? cv.getStatus() : null;
        String path = cv != null ? cv.getPdfPath() : null;
        Long id = cv != null ? cv.getId() : null;
        return CvDto.builder()
                .id(id)
                .studentId(sid)
                .pdfPath(path)
                .status(st)
                .build();
    }

    public static CvDto empty() {
        return new CvDto();
    }
}