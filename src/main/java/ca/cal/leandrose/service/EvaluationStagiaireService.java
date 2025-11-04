package ca.cal.leandrose.service;


import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.repository.EvaluationStagiaireRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.service.dto.evaluation.*;
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
    private final PDFGeneratorService pdfGeneratorService;

    public EvaluationStagiaireDto createEvaluation(Long employeurId, Long studentId, Long internshipId){

        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(internshipId, studentId)){
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }

        Employeur employeur = employeurRepository.findById(employeurId)
                .orElseThrow(() -> new RuntimeException("Employeur non trouvé"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Entente non trouvé"));

        EvaluationStagiaire stage = EvaluationStagiaire.builder()
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .internshipOffer(internshipOffer)
                .submitted(false)
                .build();

        EvaluationStagiaire evaluationStagiaire = evaluationStagiaireRepository.save(stage);
        return mapToDto(evaluationStagiaire);
    }

    public EvaluationStagiaireDto generateEvaluationPdf(Long evaluationId, EvaluationFormData formData){
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        String pdfPath = pdfGeneratorService.genererEvaluationPdf(evaluation, formData);
        evaluation.setPdfFilePath(pdfPath);
        evaluation.setSubmitted(true);
        EvaluationStagiaire savedEvaluation = evaluationStagiaireRepository.save(evaluation);
        return mapToDto(savedEvaluation);
    }

    public byte[] getEvaluationPdf(Long evaluationId){
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        if (evaluation.getPdfFilePath() == null) {
            throw new RuntimeException("PDF non généré pour cette évaluation");
        }

        return pdfGeneratorService.lireFichierPDF(evaluation.getPdfFilePath());
    }
    public EvaluationStagiaireDto getEvaluationById(Long id){
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

    private EvaluationStagiaireDto mapToDto(EvaluationStagiaire evaluation){
        return new EvaluationStagiaireDto(
                evaluation.getId(),
                evaluation.getDateEvaluation(),
                evaluation.getEmployeur().getId(),
                evaluation.getStudent().getId(),
                evaluation.getInternshipOffer().getId(),
                evaluation.getPdfFilePath(),
                evaluation.isSubmitted()
        );
    }
}
