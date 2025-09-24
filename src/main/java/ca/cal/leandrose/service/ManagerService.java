package ca.cal.leandrose.service;


import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.service.dto.CvDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final CvRepository cvRepository;

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

    public List<CvDto> getPendingCvs(){
        return cvRepository.findByStatus(Cv.Status.PENDING)
                .stream()
                .map(CvDto::create)
                .toList();
    }
}
