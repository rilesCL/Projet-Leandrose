package ca.cal.leandrose.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ca.cal.leandrose.presentation.request.ChatRequest;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private GestionnaireService gestionnaireService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private CvService cvService;

    @MockitoBean
    private EntenteStageService ententeStageService;

    @MockitoBean
    private UserAppService userAppService;

    @MockitoBean
    private ProfService profService;

    private ChatRequest chatRequest;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        chatRequest = new ChatRequest("Quels sont les programmes disponibles?");
        testSessionId = UUID.randomUUID().toString();
    }

    @Test
    void exchange_ShouldReturnChatResponse_WhenRequestIsValid() throws Exception {
        // Arrange
        String expectedResponse = "Voici les programmes disponibles: Informatique, Génie logiciel, etc.";
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("response", expectedResponse);
        responseMap.put("sessionId", testSessionId);

        when(chatService.chat(eq(chatRequest.query()), anyString())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Session-Id", testSessionId)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(expectedResponse))
                .andExpect(jsonPath("$.sessionId").exists());

        verify(chatService, times(1)).chat(eq(chatRequest.query()), anyString());
    }

    @Test
    void exchange_ShouldGenerateSessionId_WhenSessionIdNotProvided() throws Exception {
        // Arrange
        String expectedResponse = "Réponse du chatbot";
        when(chatService.chat(eq(chatRequest.query()), anyString())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(expectedResponse))
                .andExpect(jsonPath("$.sessionId").exists());

        verify(chatService, times(1)).chat(eq(chatRequest.query()), anyString());
    }

    @Test
    void exchange_ShouldReturnError_WhenChatServiceThrowsException() throws Exception {
        // Arrange
        when(chatService.chat(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erreur API Gemini"));

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erreur du chat")));

        verify(chatService, times(1)).chat(anyString(), anyString());
    }

    @Test
    void exchange_ShouldHandleEmptyQuery() throws Exception {
        // Arrange
        ChatRequest emptyRequest = new ChatRequest("");
        String expectedResponse = "Veuillez poser une question.";

        when(chatService.chat(eq(""), anyString())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(expectedResponse));

        verify(chatService, times(1)).chat(eq(""), anyString());
    }

    @Test
    void exchange_ShouldUseProvidedSessionId() throws Exception {
        // Arrange
        String customSessionId = "custom-session-123";
        String expectedResponse = "Réponse avec session personnalisée.";

        when(chatService.chat(eq(chatRequest.query()), eq(customSessionId)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Session-Id", customSessionId)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(expectedResponse));

        verify(chatService, times(1)).chat(eq(chatRequest.query()), eq(customSessionId));
    }

    @Test
    void exchange_ShouldMaintainConversationContext() throws Exception {
        // Arrange
        String firstQuestion = "Qu'est-ce que LeandrOSE?";
        String secondQuestion = "Comment fonctionne l'approbation des CV?";
        String firstResponse = "LeandrOSE est une application de gestion de stages.";
        String secondResponse = "L'approbation des CV se fait par les gestionnaires.";

        when(chatService.chat(eq(firstQuestion), eq(testSessionId)))
                .thenReturn(firstResponse);
        when(chatService.chat(eq(secondQuestion), eq(testSessionId)))
                .thenReturn(secondResponse);

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Session-Id", testSessionId)
                        .content(objectMapper.writeValueAsString(new ChatRequest(firstQuestion))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(firstResponse));

        // Act & Assert
        mockMvc.perform(post("/gestionnaire/chatclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Session-Id", testSessionId)
                        .content(objectMapper.writeValueAsString(new ChatRequest(secondQuestion))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(secondResponse));

        verify(chatService, times(1)).chat(eq(firstQuestion), eq(testSessionId));
        verify(chatService, times(1)).chat(eq(secondQuestion), eq(testSessionId));
    }

    @Test
    void clearChatSession_ShouldReturnNoContent_WhenSessionExists() throws Exception {
        // Arrange
        String sessionId = "test-session-123";
        doNothing().when(chatService).clearHistory(sessionId);

        // Act & Assert
        mockMvc.perform(delete("/gestionnaire/chatclient/session/{sessionId}", sessionId))
                .andExpect(status().isNoContent());

        verify(chatService, times(1)).clearHistory(sessionId);
    }

    @Test
    void getActiveSessions_ShouldReturnListOfSessions() throws Exception {
        // Arrange
        Set<String> activeSessions = Set.of("session-1", "session-2", "session-3");
        when(chatService.getActiveSessions()).thenReturn(activeSessions);

        // Act & Assert
        mockMvc.perform(get("/gestionnaire/chatclient/sessions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(3));

        verify(chatService, times(1)).getActiveSessions();
    }
}

