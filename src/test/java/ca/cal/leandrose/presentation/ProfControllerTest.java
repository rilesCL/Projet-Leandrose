package ca.cal.leandrose.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.dto.ProfStudentItemDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProfController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class ProfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntenteStageService ententeStageService;

    private ProfStudentItemDto dto(long ententeId, long studentId, String fn, String ln,
                                   String company, String title,
                                   LocalDate start, LocalDate end,
                                   String stageStatus, String evaluationStatus) {
        return ProfStudentItemDto.builder()
                .ententeId(ententeId)
                .studentId(studentId)
                .studentFirstName(fn)
                .studentLastName(ln)
                .companyName(company)
                .offerTitle(title)
                .startDate(start)
                .endDate(end)
                .stageStatus(stageStatus)
                .evaluationStatus(evaluationStatus)
                .build();
    }

    @Test
    void getEtudiantsAttribues_DefaultParams() throws Exception {
        var item = dto(10L, 5L, "John", "Doe", "TechCorp", "Stage dev",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 24), "EN_COURS", "A_FAIRE");

        when(ententeStageService.getEtudiantsPourProf(1L, null, null, null, null, null, "name", true))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/prof/1/etudiants").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ententeId", is(10)))
                .andExpect(jsonPath("$[0].studentId", is(5)))
                .andExpect(jsonPath("$[0].studentFirstName", is("John")))
                .andExpect(jsonPath("$[0].studentLastName", is("Doe")))
                .andExpect(jsonPath("$[0].companyName", is("TechCorp")))
                .andExpect(jsonPath("$[0].offerTitle", is("Stage dev")))
                .andExpect(jsonPath("$[0].stageStatus", is("EN_COURS")))
                .andExpect(jsonPath("$[0].evaluationStatus", is("A_FAIRE")));

        verify(ententeStageService, times(1))
                .getEtudiantsPourProf(1L, null, null, null, null, null, "name", true);
    }

    @Test
    void getEtudiantsAttribues_AllFiltersAndSortParams() throws Exception {
        var item = dto(20L, 8L, "Sophie", "Martin", "TechInnovation", "Full-Stack",
                LocalDate.of(2025, 5, 20), LocalDate.of(2025, 9, 7), "EN_COURS", "EN_COURS");

        when(ententeStageService.getEtudiantsPourProf(
                eq(2L),
                eq("sophie"),
                eq("tech"),
                eq(LocalDate.of(2025, 5, 1)),
                eq(LocalDate.of(2025, 9, 30)),
                eq("EN_COURS"),
                eq("company"),
                eq(false)
        )).thenReturn(List.of(item));

        mockMvc.perform(get("/prof/2/etudiants")
                        .param("nom", "sophie")
                        .param("entreprise", "tech")
                        .param("dateFrom", "2025-05-01")
                        .param("dateTo", "2025-09-30")
                        .param("evaluationStatus", "EN_COURS")
                        .param("sortBy", "company")
                        .param("asc", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ententeId", is(20)))
                .andExpect(jsonPath("$[0].studentId", is(8)))
                .andExpect(jsonPath("$[0].companyName", is("TechInnovation")))
                .andExpect(jsonPath("$[0].evaluationStatus", is("EN_COURS")));

        ArgumentCaptor<Long> profId = ArgumentCaptor.forClass(Long.class);
        verify(ententeStageService, times(1)).getEtudiantsPourProf(
                profId.capture(),
                eq("sophie"),
                eq("tech"),
                eq(LocalDate.of(2025, 5, 1)),
                eq(LocalDate.of(2025, 9, 30)),
                eq("EN_COURS"),
                eq("company"),
                eq(false)
        );
        org.junit.jupiter.api.Assertions.assertEquals(2L, profId.getValue());
    }
}
