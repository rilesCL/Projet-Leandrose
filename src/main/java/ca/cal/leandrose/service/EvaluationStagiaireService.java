package ca.cal.leandrose.service;


import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.dto.evaluation.prof.EntrepriseTeacherDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationProfFormDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationTeacherInfoDto;
import ca.cal.leandrose.service.dto.evaluation.prof.StudentTeacherDto;
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
    private final ProfRepository profRepository;
    private final PDFGeneratorService pdfGeneratorService;

    public EvaluationStagiaireDto createEvaluationByEmployer(Long employerId, Long studentId, Long internshipId){
        return createEvaluationInternal(CreatorTypeEvaluation.EMPLOYER, employerId, studentId, internshipId);

    }
    public EvaluationStagiaireDto createEvaluationByProf(Long profId, Long studentId, Long internshipId){
        return createEvaluationInternal(CreatorTypeEvaluation.PROF, profId, studentId, internshipId);
    }

    private EvaluationStagiaireDto createEvaluationInternal(CreatorTypeEvaluation creator, Long creatorId,
                                                            Long studentId, Long internshipId){
        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(internshipId, studentId)) {
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));

        EntenteStage stage = ententeStageRepository
                .findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(studentId, internshipId)
                .orElseThrow(() -> new RuntimeException("Aucune entente de stage trouvée"));
        Prof prof = stage.getProf();

        Employeur emp = null;


        if (creator == CreatorTypeEvaluation.EMPLOYER){
            emp = employeurRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("Employeur non trouvé"));

        }
        if (creator == CreatorTypeEvaluation.PROF){
           Prof professeur = profRepository.findById(creatorId)
                   .orElseThrow(() -> new RuntimeException("Professeur non trouvé"));
           stage = ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                   professeur.getId(),
                   student.getId(),
                   internshipOffer.getId()
           )
                   .orElseThrow(() -> new RuntimeException("Aucune entente de stage trouvée pour ce professeur, " +
                           "ce stagiaire et cette offre"));

           prof = professeur;
           emp = stage.getEmployeur();
        }
        EvaluationStagiaire evaluation = EvaluationStagiaire.builder()
                .dateEvaluation(LocalDate.now())
                .student(student)
                .internshipOffer(internshipOffer)
                .employeur(emp)
                .professeur(prof)
                .ententeStage(stage)
                .submitted(false)
                .build();
        evaluationStagiaireRepository.save(evaluation);
        return mapToDto(evaluation);
    }

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
    public EvaluationInfoDto getEvaluationInfoForEmployer(Long employeurId, Long studentId, Long internshipOfferId) {
        boolean isEligible = isEvaluationEligible(CreatorTypeEvaluation.EMPLOYER, employeurId, studentId, internshipOfferId);

        if (!isEligible) {
            throw new IllegalStateException("Evaluation not allowed - agreement not validated or not found");
        }

        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(internshipOfferId, studentId)) {
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        InternshipOffer internship = internshipOfferRepository.findById(internshipOfferId)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));

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

    public EvaluationTeacherInfoDto getEvaluationInfoForTeacher(Long profId, Long studentId, Long internshipOfferId){
        EntenteStage entente = ententeStageRepository
                .findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                        profId, studentId, internshipOfferId
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune entente de stage associée à ce professeur pour cet étudiant et ce stage."
                ));
        if (evaluationStagiaireRepository.existsByInternshipOfferIdAndStudentId(internshipOfferId, studentId)) {
            throw new IllegalStateException("Une évaluation existe déjà pour ce stagiaire et ce stage");
        }
        Student student = entente.getCandidature().getStudent();
        InternshipOffer internship = entente.getCandidature().getInternshipOffer();
        Employeur employeur = entente.getEmployeur();

        if (employeur == null)
            throw new IllegalStateException("Aucun employeur associé à cette ofre de stage");
        EntrepriseTeacherDto entrepriseTeacherDto = new EntrepriseTeacherDto(
                internship.getCompanyName(),
                employeur.getFirstName() + " " + employeur.getLastName(),
                internship.getAddress(),
                employeur.getEmail()
        );
        StudentTeacherDto studentTeacherDto = new StudentTeacherDto(
                student.getFirstName() + " " + student.getLastName(),
                internship.getStartDate()
        );

        return new EvaluationTeacherInfoDto(entrepriseTeacherDto, studentTeacherDto);
    }

    public EvaluationStagiaireDto generateEvaluationPdfByEmployer(Long evaluationId, EvaluationFormData formData, String langage){
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        Prof professeur = getProfesseurFromEntenteStage(evaluation);
        String pdfPath = pdfGeneratorService.generatedEvaluationByEmployer(evaluation, formData, langage,
                professeur.getFirstName(), professeur.getLastName(),
                professeur.getNameCollege(), professeur.getAddress(), professeur.getFax_machine());
        evaluation.setPdfFilePath(pdfPath);
        evaluation.setSubmitted(true);
        EvaluationStagiaire savedEvaluation = evaluationStagiaireRepository.save(evaluation);
        return mapToDto(savedEvaluation);
    }
    public EvaluationStagiaireDto generateEvaluationByTeacher(Long evaluationId, EvaluationProfFormDto formData, String langage){
        EvaluationStagiaire evaluation = evaluationStagiaireRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        String pdfPath = pdfGeneratorService.generatedEvaluationByTeacher(evaluation, formData, langage);
        evaluation.setPdfFilePath(pdfPath);
        evaluation.setSubmitted(true);
        EvaluationStagiaire savedEvaluation = evaluationStagiaireRepository.save(evaluation);
        return mapToDto(savedEvaluation);
    }

    private Prof getProfesseurFromEntenteStage(EvaluationStagiaire evaluation){
        Optional<EntenteStage> ententeStage = ententeStageRepository
                .findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                        evaluation.getStudent().getId(),
                        evaluation.getInternshipOffer().getId()
                );

        if (ententeStage.isEmpty()) {
            throw new RuntimeException("No entente stage found for student " +
                    evaluation.getStudent().getId() + " and offer " +
                    evaluation.getInternshipOffer().getId());
        }

        Prof professor = ententeStage.get().getProf();
        System.out.println("Donne : " + professor);
        if (professor == null) {
            return getDefaultProfessor();
        }
        return professor;
    }

    private Prof getDefaultProfessor() {
        return Prof.builder()
                .firstName("Patrice")
                .lastName("Brodeur")
                .employeeNumber("DEFAULT001")
                .department("Stage")
                .build();
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
        return getEvaluationByCreator(CreatorTypeEvaluation.EMPLOYER, employeurId);
    }

    public List<EvaluationStagiaireDto> getEvaluationsByProfesseur(Long profId) {
        return getEvaluationByCreator(CreatorTypeEvaluation.PROF, profId);
    }
    private List<EvaluationStagiaireDto> getEvaluationByCreator(CreatorTypeEvaluation creator, Long creatorId){
        List<EvaluationStagiaire> evaluations;

        switch(creator){
            case EMPLOYER ->
                evaluations = evaluationStagiaireRepository.findByEmployeurId(creatorId);
            case PROF ->
                evaluations = evaluationStagiaireRepository.findByProfesseurId(creatorId);
            default ->
                throw new IllegalArgumentException("Unknown creator type: " + creator);

        }
        return evaluations.stream()
                .map(this::mapToDto)
                .toList();
    }

    private EvaluationStagiaireDto mapToDto(EvaluationStagiaire evaluation){
        return new EvaluationStagiaireDto(
                evaluation.getId(),
                evaluation.getDateEvaluation(),
                evaluation.getStudent().getId(),
                evaluation.getEmployeur().getId(),
                evaluation.getProfesseur().getId(),
                evaluation.getInternshipOffer().getId(),
                evaluation.getPdfFilePath(),
                evaluation.isSubmitted()
        );
    }
    public boolean isEvaluationEligible(CreatorTypeEvaluation creatorType, Long creatorId, Long studentId, Long internshipOfferId) {
        return findValidEntenteForCreator(creatorType, creatorId, studentId, internshipOfferId).isPresent();
    }

    public List<EligibleEvaluationDto> getEligibleEvaluations(CreatorTypeEvaluation creatorType, Long creatorId) {
        List<EntenteStage> ententes = null;

        if(creatorType == CreatorTypeEvaluation.EMPLOYER){
            ententes = ententeStageRepository
                    .findByCandidature_InternshipOffer_Employeur_IdAndStatut(
                            creatorId, EntenteStage.StatutEntente.VALIDEE
                    );
        }
        if(creatorType == CreatorTypeEvaluation.PROF){
            ententes = ententeStageRepository
                    .findByProf_IdAndStatut(creatorId, EntenteStage.StatutEntente.VALIDEE);
        }
        return ententes.stream()
                .map(entente -> {
                    Optional<EvaluationStagiaire> existingEval =
                            evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(
                                    entente.getStudent().getId(),
                                    entente.getCandidature().getInternshipOffer().getId()
                            );
                    return mapToEligibleEvaluationDto(entente, existingEval.orElse(null));
                })
                .collect(Collectors.toList());
    }

    private Optional<EntenteStage> findValidEntenteForCreator(
            CreatorTypeEvaluation creatorType,
            Long creatorId,
            Long studentId,
            Long internshipOfferId
    ){
        if(creatorType == CreatorTypeEvaluation.EMPLOYER){
            return ententeStageRepository
                    .findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                            studentId,
                            internshipOfferId,
                            EntenteStage.StatutEntente.VALIDEE
                    )
                    .filter(ent -> ent.getEmployeur().getId().equals(creatorId));

        }
        return ententeStageRepository
                .findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                        creatorId, studentId, internshipOfferId, EntenteStage.StatutEntente.VALIDEE
                );
    }

    private EligibleEvaluationDto mapToEligibleEvaluationDto(EntenteStage agreement, EvaluationStagiaire evaluation) {
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
                offer.getStartDate().plusWeeks(offer.getDurationInWeeks()),
                evaluation != null,  // hasEvaluation
                evaluation != null ? evaluation.getId() : null,  // evaluationId
                evaluation != null ? evaluation.isSubmitted() : false  // evaluationSubmitted
        );
    }

    public Optional<EvaluationStagiaireDto> getExistingEvaluation(Long studentId, Long internshipOfferId) {
        return evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(studentId, internshipOfferId)
                .map(this::mapToDto);
    }
}
