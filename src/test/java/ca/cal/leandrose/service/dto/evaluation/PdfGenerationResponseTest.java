package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PdfGenerationResponseTest {

  @Test
  void testPdfGenerationResponseRecord() {
    PdfGenerationResponse response =
        new PdfGenerationResponse("/uploads/evaluation.pdf", "PDF généré avec succès");

    assertNotNull(response);
    assertEquals("/uploads/evaluation.pdf", response.pdfpath());
    assertEquals("PDF généré avec succès", response.message());
  }

  @Test
  void testPdfGenerationResponseWithNullValues() {
    PdfGenerationResponse response = new PdfGenerationResponse(null, null);

    assertNull(response.pdfpath());
    assertNull(response.message());
  }

  @Test
  void testPdfGenerationResponseEquals() {
    PdfGenerationResponse response1 =
        new PdfGenerationResponse("/uploads/evaluation.pdf", "PDF généré avec succès");
    PdfGenerationResponse response2 =
        new PdfGenerationResponse("/uploads/evaluation.pdf", "PDF généré avec succès");
    PdfGenerationResponse response3 =
        new PdfGenerationResponse("/uploads/other.pdf", "PDF généré avec succès");

    assertEquals(response1, response2);
    assertEquals(response1.hashCode(), response2.hashCode());
    assertNotEquals(response1, response3);
  }

  @Test
  void testPdfGenerationResponseToString() {
    PdfGenerationResponse response =
        new PdfGenerationResponse("/uploads/evaluation.pdf", "PDF généré avec succès");

    assertNotNull(response.toString());
    assertTrue(response.toString().contains("PdfGenerationResponse"));
  }

  @Test
  void testPdfGenerationResponseWithEmptyStrings() {
    PdfGenerationResponse response = new PdfGenerationResponse("", "");

    assertEquals("", response.pdfpath());
    assertEquals("", response.message());
  }
}
