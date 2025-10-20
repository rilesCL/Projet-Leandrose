package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class InternshipOfferServiceTest {

    @Mock
    private InternshipOfferRepository internshipOfferRepository;

    @Mock
    private EmployeurRepository employeurRepository;

    @InjectMocks
    private InternshipOfferService internshipOfferService;

    private Employeur employeur;
    private EmployeurDto employeurDto;

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
        employeurDto = EmployeurDto.create(employeur);
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
        // Arrange
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

        when(employeurRepository.findById(employeurDto.getId()))
                .thenReturn(Optional.of(employeur));

        when(internshipOfferRepository.save(any(InternshipOffer.class)))
                .thenAnswer(invocation -> {
                    InternshipOffer offer = invocation.getArgument(0);
                    offer.setId(10L);
                    return offer;
                });

        // Act
        InternshipOfferDto result = internshipOfferService.createOfferDto(
                "Stage en Java",
                LocalDate.now(),
                12,
                "123 rue Tech",
                1000f,
                employeurDto,
                pdfFile
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("PENDING_VALIDATION");

        String expectedDir = Paths.get("uploads", "offers", "1").toString();
        assertThat(result.getPdfPath()).contains(expectedDir);
        assertThat(result.getPdfPath()).contains("offer.pdf_");

        verify(internshipOfferRepository, times(1)).save(any(InternshipOffer.class));
    }


    @Test
    void createOffer_invalidPdf_throwsException() {
        // Arrange
        MockMultipartFile invalidPdf = new MockMultipartFile(
                "file", "bad.txt", "text/plain", "hello".getBytes()
        );
        when(employeurRepository.findById(employeurDto.getId()))
                .thenReturn(Optional.of(employeur));

        // Act + Assert
        assertThatThrownBy(() -> internshipOfferService.createOfferDto(
                "Stage en DevOps",
                LocalDate.now(),
                10,
                "456 rue Cloud",
                500f,
                employeurDto,
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

        InternshipOfferDto result = internshipOfferService.getOffer(5L);

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

    @Test
    void getOneOfferDetails(){
        Long offerId = 1L;
        InternshipOffer offer = new InternshipOffer();
        offer.setId(offerId);
        offer.setDescription("Test Internship offer");
        offer.setStatus(InternshipOffer.Status.PUBLISHED);
        offer.setAddress("123 Main St.");
        offer.setDurationInWeeks(12);

        Employeur employeur = new Employeur();
        employeur.setId(100L);
        employeur.setFirstName("Alice");
        employeur.setLastName("Doe");

        Credentials creds = new Credentials("alice@example", "securepass", Role.EMPLOYEUR);
        employeur.setCredentials(creds);
        offer.setEmployeur(employeur);

        when(internshipOfferRepository.findById(offerId)).thenReturn(Optional.of(offer));

        InternshipOfferDto result = internshipOfferService.getOfferDetails(offerId);

        assertNotNull(result);
        assertEquals(offerId, result.getId());
        assertEquals("Test Internship offer", result.getDescription());
        assertEquals("PUBLISHED", result.getStatus());
        assertEquals("123 Main St.", result.getAddress());
        assertEquals(12, result.getDurationInWeeks());
    }
}
