package ca.cal.leandrose.service;


import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.evaluation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EvaluationStagiaireService {

    private final EvaluationStagiaireRepository evaluationStagiaireRepository;
    private final EmployeurRepository employeurRepository;
    private final StudentRepository studentRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final EntenteStageRepository ententeStageRepository;
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
    public EvaluationInfoDto getEvaluationInfo(Long employeurId, Long studentId, Long internshipOfferId) {
        // Check if evaluation is eligible
        boolean isEligible = isEvaluationEligible(employeurId, studentId, internshipOfferId);

        if (!isEligible) {
            throw new IllegalStateException("Evaluation not allowed - agreement not validated or not found");
        }

        // Check if evaluation already exists
        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(internshipOfferId, studentId)) {
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }

        // Fetch related entities for info only
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        InternshipOffer internship = internshipOfferRepository.findById(internshipOfferId)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));

        // Return info without creating evaluation
        StudentInfoDto studentInfo = new StudentInfoDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getProgram()
        );

        InternshipInfoDto internshipInfo = new InternshipInfoDto(
                internship.getId(),
                internship.getDescription(),
                internship.getCompanyName()
        );

        return new EvaluationInfoDto(studentInfo, internshipInfo);
    }

    public EvaluationStagiaireDto generateEvaluationPdf(Long evaluationId, EvaluationFormData formData, String langage){
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        String pdfPath = pdfGeneratorService.genererEvaluationPdf(evaluation, formData, langage);
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
    public boolean isEvaluationEligible(Long employeurId, Long studentId, Long internshipOfferId) {
        Optional<EntenteStage> validAgreement = ententeStageRepository
                .findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                        studentId, internshipOfferId, EntenteStage.StatutEntente.VALIDEE);

        if (validAgreement.isEmpty()) {
            return false;
        }

        EntenteStage agreement = validAgreement.get();
        return agreement.getEmployeur().getId().equals(employeurId);
    }

    public List<EligibleEvaluationDto> getEligibleEvaluations(Long employeurId) {
        // Find all validated agreements for this employer
        List<EntenteStage> validatedAgreements = ententeStageRepository
                .findByCandidature_InternshipOffer_Employeur_IdAndStatut(
                        employeurId, EntenteStage.StatutEntente.VALIDEE);

        return validatedAgreements.stream()
                .map(this::mapToEligibleEvaluationDto)
                .collect(Collectors.toList());
    }

    private EligibleEvaluationDto mapToEligibleEvaluationDto(EntenteStage agreement) {
        Candidature candidature = agreement.getCandidature();
        Student student = candidature.getStudent();
        InternshipOffer offer = candidature.getInternshipOffer();

        return new EligibleEvaluationDto(
                agreement.getId(),
                student.getId(),
                offer.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getProgram(),
                offer.getDescription(),
                offer.getCompanyName(),
                offer.getStartDate(),
                offer.getStartDate().plusWeeks(offer.getDurationInWeeks())
        );
    }
}
