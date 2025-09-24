package ca.cal.leandrose.service;


import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.GestionnaireRepository;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final CvRepository cvRepository;
    private final PasswordEncoder passwordEncoder;
    private final GestionnaireRepository gestionnaireRepository;

    @Transactional
    public CvDto approveCv(Long cvId){
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(()-> new RuntimeException("Cv non trouvé"));
        cv.setStatus(Cv.Status.APPROVED);
        Cv saved = cvRepository.save(cv);

        return CvDto.create(saved);
    }

    @Transactional
    public CvDto rejectCv(Long cvId){
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(()-> new RuntimeException("Cv non trouvé"));
        cv.setStatus(Cv.Status.REJECTED);
        Cv saved = cvRepository.save(cv);

        return CvDto.create(saved);
    }

    @Transactional
    public GestionnaireDto createManager(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            String matricule,
            String phoneNumber
    ) {
        try{
            Gestionnaire manager = Gestionnaire.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .matricule(matricule)
                    .phoneNumber(phoneNumber)
                    .build();

            Gestionnaire savedManager = gestionnaireRepository.save(manager);
            return GestionnaireDto.create(savedManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<CvDto> getPendingCvs(){
        return cvRepository.findByStatus(Cv.Status.PENDING)
                .stream()
                .map(CvDto::create)
                .toList();
    }
}
