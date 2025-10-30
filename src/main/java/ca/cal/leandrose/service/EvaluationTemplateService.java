package ca.cal.leandrose.service;


import ca.cal.leandrose.service.dto.evaluation.EvaluationCategory;
import ca.cal.leandrose.service.dto.evaluation.EvaluationTemplateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationTemplateService {
    private final ObjectMapper objectMapper;

    public EvaluationTemplateService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }


    public String getDefaultTemplate(){
        EvaluationTemplateDto template = new EvaluationTemplateDto(List.of(
                new EvaluationCategory(
                        "productivity", "productivity.title", "productivity.description",
                        List.of(
                                "productivity.questions.q1",
                                "productivity.questions.q2",
                                "productivity.questions.q3",
                                "productivity.questions.q4",
                                "productivity.questions.q5"

                        )
                ),
                new EvaluationCategory("qualite_travail", "qualite_travail.title", "qualite_travail.description",
                        List.of(
                                "qualite_travail.questions.q1",
                                "qualite_travail.questions.q2",
                                "qualite_travail.questions.q3",
                                "qualite_travail.questions.q4",
                                "qualite_travail.questions.q5"
                                )
                ),
                new EvaluationCategory("qualite_relation", "qualite_relation.title", "qualite_relation.description",
                        List.of(
                                "qualite_relation.questions.q1",
                                "qualite_relation.questions.q2",
                                "qualite_relation.questions.q3",
                                "qualite_relation.questions.q4",
                                "qualite_relation.questions.q5",
                                "qualite_relation.questions.q6"
                        )
                )
        ));
        try{
            return objectMapper.writeValueAsString(template);
        }
        catch (JsonProcessingException e){
            throw new RuntimeException("Error creating evaluation template", e);
        }
    }

    public EvaluationTemplateDto parseTemplate(String templateJson){
        try{
            return objectMapper.readValue(templateJson, EvaluationTemplateDto.class);
        }
        catch (JsonProcessingException e){
            throw new RuntimeException("Error creating evaluation template", e);
        }
    }
}
