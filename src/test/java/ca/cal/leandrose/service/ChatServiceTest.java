package ca.cal.leandrose.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  private static final String API_KEY = "test-api-key";
  private static final String TEST_SESSION_ID = "test-session-123";
  @Mock private RestTemplate restTemplate;
  @Mock private GestionnaireService gestionnaireService;
  @Mock private InternshipOfferService internshipOfferService;
  @Mock private EntenteStageService ententeService;
  @InjectMocks private ChatService chatService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(chatService, "apiKey", API_KEY);
    ReflectionTestUtils.setField(chatService, "restTemplate", restTemplate);
  }

  @Test
  void chat_ShouldReturnResponse_WhenApiCallSucceeds() {
    // Arrange
    String userMessage = "Quels sont les programmes disponibles?";
    String expectedResponse =
        "Voici les programmes disponibles: Informatique, Génie logiciel, etc.";

    Map<String, Object> responseBody = createMockResponse(expectedResponse);
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

    // Act
    String result = chatService.chat(userMessage, TEST_SESSION_ID);

    // Assert
    assertThat(result).isEqualTo(expectedResponse);
    verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
  }

  @Test
  void chat_ShouldUseDefaultSession_WhenSessionIdNotProvided() {
    // Arrange
    String userMessage = "Bonjour";
    String expectedResponse = "Bonjour! Comment puis-je vous aider?";

    Map<String, Object> responseBody = createMockResponse(expectedResponse);
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

    // Act
    String result = chatService.chat(userMessage);

    // Assert
    assertThat(result).isEqualTo(expectedResponse);
    verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
  }

  @Test
  void chat_ShouldMaintainConversationHistory() {
    // Arrange
    String firstMessage = "Premier message";
    String secondMessage = "Deuxième message";
    String firstResponse = "Réponse au premier message";
    String secondResponse = "Réponse au deuxième message";

    Map<String, Object> firstResponseBody = createMockResponse(firstResponse);
    Map<String, Object> secondResponseBody = createMockResponse(secondResponse);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(firstResponseBody, HttpStatus.OK))
        .thenReturn(new ResponseEntity<>(secondResponseBody, HttpStatus.OK));

    // Act
    String result1 = chatService.chat(firstMessage, TEST_SESSION_ID);
    String result2 = chatService.chat(secondMessage, TEST_SESSION_ID);

    // Assert
    assertThat(result1).isEqualTo(firstResponse);
    assertThat(result2).isEqualTo(secondResponse);
    verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(Map.class));
  }

  @Test
  void chat_ShouldLimitHistoryTo20Messages() {
    // Arrange
    String message = "Message";
    String response = "Réponse";

    Map<String, Object> responseBody = createMockResponse(response);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    // Act
    for (int i = 0; i < 25; i++) {
      chatService.chat(message + i, TEST_SESSION_ID);
    }

    // Assert
    verify(restTemplate, times(25)).postForEntity(anyString(), any(), eq(Map.class));
  }

  @Test
  void chat_ShouldThrowException_WhenApiReturnsError() {
    // Arrange
    String userMessage = "Test message";
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenThrow(new RuntimeException("Erreur API"));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> {
          chatService.chat(userMessage, TEST_SESSION_ID);
        });
  }

  @Test
  void chat_ShouldThrowException_WhenResponseBodyIsNull() {
    // Arrange
    String userMessage = "Test message";
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> {
          chatService.chat(userMessage, TEST_SESSION_ID);
        });
  }

  @Test
  void chat_ShouldThrowException_WhenNoCandidatesInResponse() {
    // Arrange
    String userMessage = "Test message";
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("candidates", Collections.emptyList());
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> {
          chatService.chat(userMessage, TEST_SESSION_ID);
        });
  }

  @Test
  void clearHistory_ShouldRemoveSessionHistory() {
    // Arrange
    String userMessage = "Test message";
    String response = "Test response";

    Map<String, Object> responseBody = createMockResponse(response);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    // Act
    chatService.chat(userMessage, TEST_SESSION_ID);
    chatService.clearHistory(TEST_SESSION_ID);

    // Assert
    Set<String> activeSessions = chatService.getActiveSessions();
    assertThat(activeSessions).doesNotContain(TEST_SESSION_ID);
  }

  @Test
  void getActiveSessions_ShouldReturnAllActiveSessions() {
    // Arrange
    String session1 = "session-1";
    String session2 = "session-2";
    String message = "Test";
    String response = "Response";

    Map<String, Object> responseBody = createMockResponse(response);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    // Act
    chatService.chat(message, session1);
    chatService.chat(message, session2);

    // Assert
    Set<String> activeSessions = chatService.getActiveSessions();
    assertThat(activeSessions).contains(session1, session2);
  }

  @Test
  void chat_ShouldIncludeSystemContext_WhenHistoryIsEmpty() {
    // Arrange
    String userMessage = "Qu'est-ce que LeandrOSE?";
    String expectedResponse = "LeandrOSE est une application de gestion de stages.";

    Map<String, Object> responseBody = createMockResponse(expectedResponse);
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

    // Act
    String result = chatService.chat(userMessage, TEST_SESSION_ID);

    // Assert
    assertThat(result).isEqualTo(expectedResponse);
    verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
  }

  @Test
  void chat_ShouldHandleDifferentSessionsIndependently() {
    // Arrange
    String session1 = "session-1";
    String session2 = "session-2";
    String message = "Test";
    String response = "Response";

    Map<String, Object> responseBody = createMockResponse(response);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    // Act
    chatService.chat(message, session1);
    chatService.chat(message, session2);

    // Assert
    Set<String> activeSessions = chatService.getActiveSessions();
    assertThat(activeSessions).hasSize(2);
    assertThat(activeSessions).contains(session1, session2);
  }

  private Map<String, Object> createMockResponse(String text) {
    Map<String, Object> responseBody = new HashMap<>();
    Map<String, Object> content = new HashMap<>();
    List<Map<String, Object>> parts = new ArrayList<>();
    parts.add(Map.of("text", text));
    content.put("parts", parts);

    Map<String, Object> candidate = new HashMap<>();
    candidate.put("content", content);

    List<Map<String, Object>> candidates = new ArrayList<>();
    candidates.add(candidate);
    responseBody.put("candidates", candidates);

    return responseBody;
  }
}
