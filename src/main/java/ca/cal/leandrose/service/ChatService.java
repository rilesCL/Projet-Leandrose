package ca.cal.leandrose.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    @Value("${google.ai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Stocker l'historique des conversations par session/utilisateur
    private final Map<String, List<Map<String, Object>>> conversationHistory = new ConcurrentHashMap<>();

    // Contexte système sur votre projet
    private static final String SYSTEM_CONTEXT = """
            Tu es un assistant IA qui aide avec une application Spring Boot de gestion de stages appelée LeandrOSE.
            
            Aperçu du projet :
            - L'application gère les offres de stage pour un cégep/université
            - Entités principales : Étudiants, Employeurs, Gestionnaires, Professeurs, Offres de stage, CV, Candidatures, Convocations d'entrevue, et Ententes de stage
            
            Fonctionnalités clés :
            1. Les étudiants peuvent téléverser des CV et postuler aux offres de stage
            2. Les employeurs peuvent créer des offres de stage
            3. Les gestionnaires approuvent/rejettent les CV et les offres de stage
            4. Les employeurs peuvent inviter les étudiants à des entrevues (convocations)
            5. L'employeur et l'étudiant doivent tous deux accepter les candidatures
            6. Les ententes de stage sont créées et doivent être signées par l'étudiant, l'employeur, le gestionnaire et le professeur assigné
            7. Les professeurs sont assignés pour superviser les stages
            
            Stack technique :
            - Spring Boot 3.5.5
            - Java 21
            - Base de données PostgreSQL
            - Authentification JWT
            - JPA/Hibernate
            - Génération de PDF avec iText
            - APIs RESTful
            
            Programmes disponibles :
            - Informatique (Computer Science)
            - Génie logiciel (Software Engineering)
            - Technologies de l'information (Information Technology)
            - Science des données (Data Science)
            - Cybersécurité (Cyber Security)
            - Intelligence artificielle (Artificial Intelligence)
            - Génie électrique (Electrical Engineering)
            - Génie mécanique (Mechanical Engineering)
            - Génie civil (Civil Engineering)
            - Génie chimique (Chemical Engineering)
            - Génie biomédical (Biomedical Engineering)
            - Administration des affaires (Business Administration)
            - Comptabilité (Accounting)
            - Finance
            - Économie (Economics)
            - Marketing
            - Gestion (Management)
            - Psychologie (Psychology)
            - Sociologie (Sociology)
            - Science politique (Political Science)
            - Relations internationales (International Relations)
            - Droit (Law)
            - Éducation (Education)
            - Littérature (Literature)
            - Histoire (History)
            - Philosophie (Philosophy)
            - Linguistique (Linguistics)
            - Biologie (Biology)
            - Chimie (Chemistry)
            - Physique (Physics)
            - Mathématiques (Mathematics)
            - Statistiques (Statistics)
            - Sciences environnementales (Environmental Science)
            - Médecine (Medicine)
            - Sciences infirmières (Nursing)
            - Pharmacie (Pharmacy)
            - Médecine dentaire (Dentistry)
            - Architecture
            - Beaux-arts (Fine Arts)
            - Musique (Music)
            - Théâtre (Theater)
            - Études cinématographiques (Film Studies)
            - Communication
            - Journalisme (Journalism)
            - Design
            - Anthropologie (Anthropology)
            - Géographie (Geography)
            - Sciences du sport (Sports Science)
            
            Flux de travail principaux :
            - Approbation de CV : Étudiant téléverse → Gestionnaire approuve/rejette
            - Approbation d'offre : Employeur crée → Gestionnaire approuve/rejette
            - Processus de candidature : Étudiant postule → Employeur accepte → Étudiant accepte → Statut devient ACCEPTED
            - Entente de stage : Créée après acceptation des deux parties → Doit être signée par les 4 parties (étudiant, employeur, gestionnaire, prof)
            
            Statuts des candidatures :
            - PENDING : En attente de la décision de l'employeur
            - ACCEPTED_BY_EMPLOYER : Acceptée par l'employeur, en attente de la décision de l'étudiant
            - ACCEPTED : Acceptée par les deux parties (prête pour création d'entente)
            - REJECTED : Rejetée par l'employeur ou l'étudiant
            
            Statuts des ententes de stage :
            - EN_ATTENTE : Créée, en attente de signatures
            - SIGNEE_ETUDIANT : Signée par l'étudiant
            - SIGNEE_EMPLOYEUR : Signée par l'employeur
            - SIGNEE_GESTIONNAIRE : Signée par le gestionnaire
            - VALIDEE : Signée par toutes les parties (peut maintenant recevoir un prof)
            
            Endpoints principaux :
            - POST /gestionnaire/cv/{cvId}/approve : Approuver un CV
            - POST /gestionnaire/cv/{cvId}/reject : Rejeter un CV
            - POST /gestionnaire/offers/{id}/approve : Approuver une offre
            - POST /gestionnaire/offers/{id}/reject : Rejeter une offre
            - POST /gestionnaire/ententes : Créer une entente
            - POST /gestionnaire/ententes/{ententeId}/signer : Signer une entente (gestionnaire)
            - POST /gestionnaire/ententes/{ententeId}/attribuer-prof : Attribuer un professeur
            - POST /gestionnaire/chatclient : Discuter avec l'assistant IA
            
            Réponds aux questions sur ce système, son architecture, ses fonctionnalités et comment l'utiliser ou l'étendre.
            Réponds toujours en français et sois précis et professionnel.
            """;

    public String chat(String userMessage) {
        return chat(userMessage, "default");
    }

    public String chat(String userMessage, String sessionId) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // Obtenir ou créer l'historique de conversation pour cette session
        List<Map<String, Object>> history = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Construire la conversation complète avec le contexte système
        List<Map<String, Object>> fullConversation = new ArrayList<>();

        // Ajouter le contexte système comme premier message si l'historique est vide
        if (history.isEmpty()) {
            fullConversation.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", SYSTEM_CONTEXT))
            ));
            fullConversation.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text", "Je comprends. Je suis prêt à aider avec les questions sur le système de gestion de stages LeandrOSE."))
            ));
        }

        // Ajouter l'historique de conversation
        fullConversation.addAll(history);

        // Ajouter le message actuel de l'utilisateur
        Map<String, Object> userContent = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
        );
        fullConversation.add(userContent);

        // Construire le corps de la requête
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", fullConversation);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Réponse vide de l'API Gemini");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Aucun candidat dans la réponse");
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String aiResponse = (String) parts.get(0).get("text");

            // Sauvegarder dans l'historique
            history.add(userContent);
            history.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text", aiResponse))
            ));

            // Limiter l'historique aux 20 derniers messages (10 échanges) pour éviter les limites de tokens
            if (history.size() > 20) {
                history.subList(0, history.size() - 20).clear();
            }

            return aiResponse;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'appel à l'API Gemini: " + e.getMessage(), e);
        }
    }

    // Effacer l'historique de conversation pour une session
    public void clearHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }

    // Obtenir toutes les sessions actives
    public Set<String> getActiveSessions() {
        return conversationHistory.keySet();
    }
}