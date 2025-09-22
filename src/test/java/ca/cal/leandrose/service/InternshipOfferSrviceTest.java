package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternshipOfferServiceTest {

    @Mock
    private InternshipOfferRepository internshipOfferRepository;

    @InjectMocks
    private InternshipOfferService internshipOfferService;

    private Employeur employeur;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("secret")
                .companyName("TechCorp")
                .field("IT")
                .build();

        // Ensure base folder exists
        Path baseDir = Paths.get("uploads/offers/1");
        try {
            if (Files.exists(baseDir)) {
                Files.walk(baseDir)
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            }
        } catch (IOException ignored) {}
    }
    @AfterEach
    void tearDown() throws IOException {
        Path baseDir = Paths.get("uploads/offers");
        if (Files.exists(baseDir)) {
            Files.walk(baseDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }


    @Test
    void createOffer_validPdf_savesOffer() throws Exception {
        // Arrange: generate a valid PDF in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Hello Internship PDF"));
        document.close();
        byte[] pdfBytes = baos.toByteArray();

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "offer.pdf", "application/pdf", pdfBytes
        );

        when(internshipOfferRepository.save(any(InternshipOffer.class)))
                .thenAnswer(invocation -> {
                    InternshipOffer offer = invocation.getArgument(0);
                    offer.setId(10L); // simulate DB persistence
                    return offer;
                });

        // Act
        InternshipOffer result = internshipOfferService.createOffer(
                "Stage en Java",
                LocalDate.now(),
                12,
                "123 rue Tech",
                1000f,
                employeur,
                pdfFile
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(InternshipOffer.Status.PENDING_VALIDATION);

        // Path check: OS independent + timestamp tolerant
        String expectedDir = Paths.get("uploads", "offers", "1").toString();
        assertThat(result.getPdfPath()).contains(expectedDir);
        assertThat(result.getPdfPath()).contains("offer.pdf_");

        verify(internshipOfferRepository, times(1)).save(any(InternshipOffer.class));
    }



    @Test
    void createOffer_invalidPdf_throwsException() {
        // Arrange: not a valid PDF
        MockMultipartFile invalidPdf = new MockMultipartFile(
                "file", "bad.txt", "text/plain", "hello".getBytes()
        );

        // Act + Assert
        assertThatThrownBy(() -> internshipOfferService.createOffer(
                "Stage en DevOps",
                LocalDate.now(),
                10,
                "456 rue Cloud",
                500f,
                employeur,
                invalidPdf
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PDF invalide");
    }

    @Test
    void getOffer_existingId_returnsOffer() {
        InternshipOffer offer = InternshipOffer.builder()
                .id(5L)
                .description("Stage Data Science")
                .employeur(employeur)
                .build();

        when(internshipOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        InternshipOffer result = internshipOfferService.getOffer(5L);

        assertThat(result.getDescription()).isEqualTo("Stage Data Science");
    }

    @Test
    void getOffer_nonExistingId_throwsException() {
        when(internshipOfferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipOfferService.getOffer(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Offre de stage non trouvée");
    }

    @Test
    void getOfferPdf_readsBytesFromFile() throws Exception {
        // Arrange
        Path filePath = Paths.get("uploads/offers/1/test.pdf");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "PDF_CONTENT".getBytes());

        InternshipOffer offer = InternshipOffer.builder()
                .id(20L)
                .employeur(employeur)
                .pdfPath(filePath.toString())
                .build();

        when(internshipOfferRepository.findById(20L)).thenReturn(Optional.of(offer));

        // Act
        byte[] result = internshipOfferService.getOfferPdf(20L);

        // Assert
        assertThat(new String(result)).isEqualTo("PDF_CONTENT");
    }

    @Test
    void getOffersByEmployeurId_returnsList() {
        InternshipOffer offer1 = InternshipOffer.builder().id(1L).description("Stage A").employeur(employeur).build();
        InternshipOffer offer2 = InternshipOffer.builder().id(2L).description("Stage B").employeur(employeur).build();

        when(internshipOfferRepository.findOffersByEmployeurId(1L))
                .thenReturn(List.of(offer1, offer2));

        List<InternshipOffer> result = internshipOfferService.getOffersByEmployeurId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("description").containsExactly("Stage A", "Stage B");
    }
}
