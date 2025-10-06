package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Candidature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvocationService {

    public void addConvocation (Candidature candidature) {

        candidature.setStatus(Candidature.Status.CONVENED);

    }
}
