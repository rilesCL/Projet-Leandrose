package ca.cal.leandrose.service;


import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.repository.EvaluationStagiaireRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.service.dto.evaluation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EvaluationStagiaireService {

    private final EvaluationStagiaireRepository evaluationStagiaireRepository;
    private final EmployeurRepository employeurRepository;
    private final StudentRepository studentRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final EvaluationTemplateService templateService;
    private final ObjectMapper objectMapper;

    public EvaluationStagiaireDto createEvaluation(EvaluationStagiaireDto evaluationDto){
        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(
                evaluationDto.internshipOfferId(), evaluationDto.studentId())){
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }

        Employeur employeur = employeurRepository.findById(evaluationDto.employeurId())
                .orElseThrow(() -> new RuntimeException("Employeur non trouvé"));
        Student student = studentRepository.findById(evaluationDto.studentId())
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        InternshipOffer internshipOffer = internshipOfferRepository.findById(evaluationDto.internshipOfferId())
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvé"));
        String templateJson = evaluationDto.template() != null
                ? convertTemplateToJson(evaluationDto.template())
                : templateService.getDefaultTemplate();

        EvaluationTemplateDto template = templateService.parseTemplate(templateJson);
        EvaluationResponsesDto emptyResponses = createEmptyResponses(template);
        String responseJson = convertResposnesToJSon(emptyResponses);

        EvaluationStagiaire evaluation = EvaluationStagiaire.builder()
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .internshipOffer(internshipOffer)
                .evaluationTemplate(templateJson)
                .evaluationResponses(responseJson)
                .generalComment("")
                .submitted(false)
                .build();

        EvaluationStagiaire savedEvaluation = evaluationStagiaireRepository.save(evaluation);
        return mapToDto(savedEvaluation);
    }

    public EvaluationStagiaireDto submitEvaluation(Long evaluationId, EvaluationResponsesDto responses) {
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        try {
            String responsesJson = objectMapper.writeValueAsString(responses);
            evaluation.setEvaluationResponses(responsesJson);
            evaluation.setGeneralComment(responses.generalComment());
            evaluation.setSubmitted(true);

            EvaluationStagiaire savedEvaluation = evaluationStagiaireRepository.save(evaluation);
            return mapToDto(savedEvaluation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde des réponses", e);
        }
    }

    public EvaluationStagiaireDto getEvaluationById(Long id) {
        return evaluationStagiaireRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));
    }

    public List<EvaluationStagiaireDto> getEvaluationsByEmployeur(Long employeurId) {
        return evaluationStagiaireRepository.findByEmployeurId(employeurId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EvaluationStagiaireDto> getEvaluationsByStudent(Long studentId) {
        return evaluationStagiaireRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private EvaluationResponsesDto createEmptyResponses(EvaluationTemplateDto template){
        List<EvaluationCategoryResponse> categoryResponses = template.categories().stream()
                .map(category -> {
                    List<QuestionResponse> questionResponses = category.questionsKeys().stream()
                            .map(questionKey -> new QuestionResponse(questionKey, "", false,
                                    EvaluationChoice.NOT_APPLICATION))
                            .toList();
                    return new EvaluationCategoryResponse(category.key(), questionResponses, "");
                })
                .toList();
        return new EvaluationResponsesDto(categoryResponses, "");
    }

    private String convertTemplateToJson(EvaluationTemplateDto template){
        try{
            return objectMapper.writeValueAsString(template);
        }
        catch(JsonProcessingException e){
            throw new RuntimeException("Erreur de conversion du template", e);
        }
    }

    private String convertResposnesToJSon(EvaluationResponsesDto responses){
        try{
            return objectMapper.writeValueAsString(responses);
        }
        catch(JsonProcessingException e){
            throw new RuntimeException("Erreur de conversion du template", e);
        }
    }


    private EvaluationStagiaireDto mapToDto(EvaluationStagiaire evaluation){
        try{
            EvaluationTemplateDto template = objectMapper.readValue(
                    evaluation.getEvaluationTemplate(), EvaluationTemplateDto.class);
            EvaluationResponsesDto responses = objectMapper.readValue(

                evaluation.getEvaluationResponses(), EvaluationResponsesDto.class);

            return new EvaluationStagiaireDto(
                    evaluation.getId(),
                    evaluation.getDateEvaluation(),
                    evaluation.getEmployeur().getId(),
                    evaluation.getStudent().getId(),
                    evaluation.getInternshipOffer().getId(),
                    template,
                    responses,
                    evaluation.isSubmitted()
            );
        }
        catch(JsonProcessingException e){
            throw new RuntimeException("Erreur lors de la conversion en DTO", e);
        }
    }
}
