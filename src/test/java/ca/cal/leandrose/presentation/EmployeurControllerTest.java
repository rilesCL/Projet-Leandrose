package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.InternshipOfferRequest;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import ca.cal.leandrose.service.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeurController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class EmployeurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAppService userAppService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    EmployeurRepository employeurRepository;

    @Test
    void getMyOffers_asEmployeur_returnsOffers() throws Exception {
        // Arrange
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .email("emp@test.com")
                .role(ca.cal.leandrose.model.auth.Role.EMPLOYEUR)
                .build();

        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .companyName("TechCorp")
                .field("IT")
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(100L)
                .description("Stage Java")
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .employeur(employeur)
                .build();

        Mockito.when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        Mockito.when(internshipOfferService.getOffersByEmployeurId(1L))
                .thenReturn(List.of(offer));

        // Act & Assert
        mockMvc.perform(get("/employeur/offers")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].description").value("Stage Java"));
    }

    @Test
    void getMyOffers_notEmployeur_returnsForbidden() throws Exception {
        // Arrange
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .email("student@test.com")
                .role(ca.cal.leandrose.model.auth.Role.STUDENT)
                .build();

        Mockito.when(userAppService.getMe(anyString())).thenReturn(studentDto);

        // Act & Assert
        mockMvc.perform(get("/employeur/offers")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadOffer_asEmployeur_savesOffer() throws Exception {
        // Arrange
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .email("emp@test.com")
                .role(ca.cal.leandrose.model.auth.Role.EMPLOYEUR)
                .build();

        InternshipOfferRequest offerRequest = new InternshipOfferRequest();
        offerRequest.setDescription("Stage React");
        offerRequest.setStartDate(LocalDate.now().toString());
        offerRequest.setDurationInWeeks(12);
        offerRequest.setAddress("123 Rue Test");
        offerRequest.setRemuneration(800f);

        MockMultipartFile offerPart = new MockMultipartFile(
                "offer",
                "offer.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(offerRequest)
        );

        MockMultipartFile pdfPart = new MockMultipartFile(
                "pdfFile",
                "offer.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF_CONTENT".getBytes()
        );

        InternshipOfferDto savedOffer = InternshipOfferDto.builder()
                .id(200L)
                .description("Stage React")
                .build();

        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .companyName("TechCorp")
                .field("IT")
                .build();

        Mockito.when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        Mockito.when(employeurRepository.findById(1L)).thenReturn(Optional.of(employeur)); // <-- important
        Mockito.when(internshipOfferService.createOfferDto(
                anyString(), any(), anyInt(), anyString(), anyFloat(), any(Employeur.class), any()
        )).thenReturn(savedOffer);

        // Act & Assert
        mockMvc.perform(multipart("/employeur/offers")
                        .file(offerPart)
                        .file(pdfPart)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("Stage React"));
    }

    @Test
    void uploadOffer_notEmployeur_returnsForbidden() throws Exception {
        // Arrange
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .email("student@test.com")
                .role(ca.cal.leandrose.model.auth.Role.STUDENT)
                .build();

        Mockito.when(userAppService.getMe(anyString())).thenReturn(studentDto);

        MockMultipartFile offerPart = new MockMultipartFile(
                "offer", "offer.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"description\":\"Stage X\"}".getBytes()
        );
        MockMultipartFile pdfPart = new MockMultipartFile(
                "pdfFile", "offer.pdf", MediaType.APPLICATION_PDF_VALUE,
                "PDF_CONTENT".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/employeur/offers")
                        .file(offerPart)
                        .file(pdfPart)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden());
    }
}
