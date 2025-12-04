package ca.cal.leandrose.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatService {

  private static final int MAX_HISTORY_SIZE = 20;
  private static final String SYSTEM_CONTEXT =
      """
            Tu es un assistant IA spécialisé pour aider avec l'application LeandrOSE de gestion de stages.

            IMPORTANT : Tu dois TOUJOURS utiliser les fonctions disponibles pour récupérer des données en temps réel.
            Ne jamais inventer ou supposer des données. Si tu as besoin d'informations, utilise les fonctions.

            Aperçu du projet :
            - Application de gestion de stages pour un cégep/université
            - Entités : Étudiants, Employeurs, Gestionnaires, Professeurs, Offres de stage, CV, Candidatures, Ententes

            Fonctionnalités clés :
            1. Gestion des CV : Les étudiants téléversent des CV, les gestionnaires les approuvent/rejettent
            2. Gestion des offres : Les employeurs créent des offres, les gestionnaires les approuvent/rejettent
            3. Candidatures : Les étudiants postulent, processus d'acceptation mutuelle
            4. Ententes de stage : Création et signature par 4 parties (étudiant, employeur, gestionnaire, prof)

            Statuts des candidatures :
            - PENDING : En attente de l'employeur
            - ACCEPTED_BY_EMPLOYER : Acceptée par l'employeur
            - ACCEPTED : Acceptée par les deux parties
            - REJECTED : Rejetée

            Statuts des ententes :
            - BROUILLON : En cours de création
            - EN_ATTENTE_SIGNATURE : Créée, en attente de signatures
            - VALIDEE : Signée par toutes les parties (étudiant, employeur, gestionnaire)
            - Après validation, un professeur peut être assigné

            RÈGLES IMPORTANTES :
            - Réponds en français ou en anglais de manière professionnelle et structurée
            - Si tu ne connais pas la réponse, dis-le poliment et demande des précisions
            - Utilise TOUJOURS les fonctions disponibles pour obtenir des données à jour
            - Présente les données de manière claire avec des listes à puces ou numérotées
            - Pour les listes longues, résume les informations principales
            - Ne fournis que les informations demandées
            """;
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper;
  private final GestionnaireService gestionnaireService;
  private final InternshipOfferService internshipOfferService;
  private final EntenteStageService ententeService;
  private final Map<String, List<Map<String, Object>>> conversationHistory =
      new ConcurrentHashMap<>();

  @Value("${google.ai.api-key}")
  private String apiKey;

  public ChatService(
      GestionnaireService gestionnaireService,
      InternshipOfferService internshipOfferService,
      EntenteStageService ententeService) {
    this.gestionnaireService = gestionnaireService;
    this.internshipOfferService = internshipOfferService;
    this.ententeService = ententeService;

    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  private List<Map<String, Object>> getFunctionDeclarations() {
    return List.of(
        createFunctionDeclaration(
            "getPendingOffers",
            "Récupère la liste des offres de stage en attente d'approbation par les gestionnaires",
            Map.of()),
        createFunctionDeclaration(
            "getApprovedOffers",
            "Récupère la liste des offres de stage approuvées et publiées",
            Map.of()),
        createFunctionDeclaration(
            "getRejectedOffers",
            "Récupère la liste des offres de stage rejetées par les gestionnaires",
            Map.of()),
        createFunctionDeclaration(
            "getPendingCvs",
            "Récupère la liste des CV en attente d'approbation par les gestionnaires",
            Map.of()),
        createFunctionDeclaration(
            "getOfferDetails",
            "Récupère les détails complets d'une offre de stage spécifique par son ID",
            Map.of(
                "offerId",
                Map.of(
                    "type", "number",
                    "description", "L'ID numérique de l'offre de stage"))),
        createFunctionDeclaration(
            "getAllPrograms",
            "Récupère la liste complète de tous les programmes d'études disponibles dans le système",
            Map.of()),
        createFunctionDeclaration(
            "getCandidaturesAcceptees",
            "Récupère les candidatures acceptées par l'étudiant ET l'employeur (prêtes pour création d'entente)",
            Map.of()),
        createFunctionDeclaration(
            "getAllEntentes",
            "Récupère la liste complète de toutes les ententes de stage dans le système",
            Map.of()),
        createFunctionDeclaration(
            "getEntenteDetails",
            "Récupère les détails complets d'une entente de stage spécifique par son ID",
            Map.of(
                "ententeId",
                Map.of(
                    "type", "number",
                    "description", "L'ID numérique de l'entente de stage"))));
  }

  private Map<String, Object> createFunctionDeclaration(
      String name, String description, Map<String, Object> parameters) {
    Map<String, Object> function = new HashMap<>();
    function.put("name", name);
    function.put("description", description);

    Map<String, Object> parametersSchema = new HashMap<>();
    parametersSchema.put("type", "object");
    parametersSchema.put("properties", parameters);
    parametersSchema.put("required", new ArrayList<>(parameters.keySet()));

    function.put("parameters", parametersSchema);
    return function;
  }

  public String chat(String userMessage) {
    return chat(userMessage, "default");
  }

  public String chat(String userMessage, String sessionId) {
    String url =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + apiKey;

    List<Map<String, Object>> history =
        conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

    List<Map<String, Object>> fullConversation = new ArrayList<>();

    if (history.isEmpty()) {
      fullConversation.add(
          Map.of("role", "user", "parts", List.of(Map.of("text", SYSTEM_CONTEXT))));
      fullConversation.add(
          Map.of(
              "role",
              "model",
              "parts",
              List.of(
                  Map.of(
                      "text",
                      "Je comprends parfaitement. Je suis prêt à vous aider avec LeandrOSE. Je vais utiliser les fonctions disponibles pour obtenir des informations à jour du système."))));
    }

    fullConversation.addAll(history);

    Map<String, Object> userContent =
        Map.of("role", "user", "parts", List.of(Map.of("text", userMessage)));
    fullConversation.add(userContent);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contents", fullConversation);

    Map<String, Object> tools = Map.of("function_declarations", getFunctionDeclarations());
    requestBody.put("tools", List.of(tools));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
      Map<String, Object> responseBody = response.getBody();

      if (responseBody == null) {
        throw new RuntimeException("Réponse vide de l'API Gemini");
      }

      List<Map<String, Object>> candidates =
          (List<Map<String, Object>>) responseBody.get("candidates");
      if (candidates == null || candidates.isEmpty()) {
        throw new RuntimeException("Aucun candidat dans la réponse");
      }

      Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
      List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

      if (parts.get(0).containsKey("functionCall")) {
        return handleFunctionCalling(sessionId, fullConversation, userContent, parts, url, headers);
      }

      String aiResponse = (String) parts.get(0).get("text");

      history.add(userContent);
      history.add(Map.of("role", "model", "parts", List.of(Map.of("text", aiResponse))));

      trimHistory(history);

      return aiResponse;

    } catch (Exception e) {
      System.err.println("❌ Erreur lors de l'appel à l'API Gemini: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Erreur lors de l'appel à l'API Gemini: " + e.getMessage(), e);
    }
  }

  private String handleFunctionCalling(
      String sessionId,
      List<Map<String, Object>> fullConversation,
      Map<String, Object> userContent,
      List<Map<String, Object>> parts,
      String url,
      HttpHeaders headers) {

    List<Map<String, Object>> history = conversationHistory.get(sessionId);

    history.add(userContent);

    Map<String, Object> functionCall = (Map<String, Object>) parts.get(0).get("functionCall");
    String functionName = (String) functionCall.get("name");
    Map<String, Object> functionArgs =
        (Map<String, Object>) functionCall.getOrDefault("args", Map.of());

    Object functionResult = executeFunction(functionName, functionArgs);

    String resultJson;
    try {
      resultJson = objectMapper.writeValueAsString(functionResult);
    } catch (Exception e) {
      System.err.println("❌ Erreur de sérialisation JSON: " + e.getMessage());
      e.printStackTrace();

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Erreur de sérialisation");
      errorResponse.put("message", e.getMessage());
      errorResponse.put("functionName", functionName);
      errorResponse.put(
          "suggestion", "Les données contiennent probablement des types non supportés");

      try {
        resultJson = objectMapper.writeValueAsString(errorResponse);
      } catch (Exception ex) {
        resultJson = "{\"error\": \"Erreur critique de sérialisation\"}";
      }
    }

    history.add(Map.of("role", "model", "parts", List.of(Map.of("functionCall", functionCall))));

    Map<String, Object> functionResponse =
        Map.of(
            "role",
            "user",
            "parts",
            List.of(
                Map.of(
                    "functionResponse",
                    Map.of("name", functionName, "response", Map.of("result", resultJson)))));

    history.add(functionResponse);

    List<Map<String, Object>> newConversation = new ArrayList<>(fullConversation);
    newConversation.add(
        Map.of("role", "model", "parts", List.of(Map.of("functionCall", functionCall))));
    newConversation.add(functionResponse);

    Map<String, Object> newRequestBody = new HashMap<>();
    newRequestBody.put("contents", newConversation);
    newRequestBody.put(
        "tools", List.of(Map.of("function_declarations", getFunctionDeclarations())));

    HttpEntity<Map<String, Object>> newRequest = new HttpEntity<>(newRequestBody, headers);

    try {
      ResponseEntity<Map> newResponse = restTemplate.postForEntity(url, newRequest, Map.class);
      Map<String, Object> newResponseBody = newResponse.getBody();

      if (newResponseBody == null) {
        throw new RuntimeException("Réponse vide après function call");
      }

      List<Map<String, Object>> newCandidates =
          (List<Map<String, Object>>) newResponseBody.get("candidates");
      Map<String, Object> newContent = (Map<String, Object>) newCandidates.get(0).get("content");
      List<Map<String, Object>> newParts = (List<Map<String, Object>>) newContent.get("parts");

      String finalResponse = (String) newParts.get(0).get("text");

      history.add(Map.of("role", "model", "parts", List.of(Map.of("text", finalResponse))));

      trimHistory(history);

      return finalResponse;

    } catch (Exception e) {
      System.err.println("❌ Erreur lors du traitement de la function call: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(
          "Erreur lors du traitement de la function call: " + e.getMessage(), e);
    }
  }

  private Object executeFunction(String functionName, Map<String, Object> args) {
    try {
      return switch (functionName) {
        case "getPendingOffers" -> gestionnaireService.getPendingOffers();
        case "getApprovedOffers" -> gestionnaireService.getApprovedOffers();
        case "getRejectedOffers" -> gestionnaireService.getRejectedoffers();
        case "getPendingCvs" -> gestionnaireService.getPendingCvs();
        case "getOfferDetails" -> {
          Long offerId = ((Number) args.get("offerId")).longValue();
          yield internshipOfferService.getOffer(offerId);
        }
        case "getAllPrograms" -> gestionnaireService.getAllPrograms();
        case "getCandidaturesAcceptees" -> ententeService.getCandidaturesAcceptees();
        case "getAllEntentes" -> ententeService.getAllEntentes();
        case "getEntenteDetails" -> {
          Long ententeId = ((Number) args.get("ententeId")).longValue();
          yield ententeService.getEntenteById(ententeId);
        }
        default -> {
          System.err.println("❌ Fonction inconnue: " + functionName);
          yield Map.of("error", "Fonction inconnue: " + functionName);
        }
      };

    } catch (Exception e) {
      System.err.println("❌ Erreur lors de l'exécution de " + functionName + ": " + e.getMessage());
      e.printStackTrace();

      Map<String, String> errorResult = new HashMap<>();
      errorResult.put("error", e.getMessage());
      errorResult.put("function", functionName);
      errorResult.put("type", e.getClass().getSimpleName());

      return errorResult;
    }
  }

  private void trimHistory(List<Map<String, Object>> history) {
    if (history.size() > MAX_HISTORY_SIZE) {
      int toRemove = history.size() - MAX_HISTORY_SIZE;
      history.subList(0, toRemove).clear();
    }
  }

  public void clearHistory(String sessionId) {
    conversationHistory.remove(sessionId);
  }

  public Set<String> getActiveSessions() {
    return conversationHistory.keySet();
  }
}
