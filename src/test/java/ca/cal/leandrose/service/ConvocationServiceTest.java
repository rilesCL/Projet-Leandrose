package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.Convocation;
import ca.cal.leandrose.repository.CandidatureRepository;
import ca.cal.leandrose.repository.ConvocationRepository;
import ca.cal.leandrose.service.dto.ConvocationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConvocationServiceTest {

    @Mock
    private ConvocationRepository convocationRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @InjectMocks
    private ConvocationService convocationService;

    private Candidature candidature;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        futureDate = LocalDateTime.now().plusDays(7);

        candidature = Candidature.builder()
                .id(1L)
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDate.now())
                .build();
    }

    @Test
    void addConvocation_withValidData_createsConvocation() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(convocationRepository.save(any(Convocation.class))).thenReturn(new Convocation());

        // Act
        convocationService.addConvocation(1L, futureDate, "Bureau 301", "Message personnalisé");

        // Assert
        ArgumentCaptor<Convocation> convocationCaptor = ArgumentCaptor.forClass(Convocation.class);
        verify(convocationRepository).save(convocationCaptor.capture());

        Convocation savedConvocation = convocationCaptor.getValue();
        assertEquals(candidature, savedConvocation.getCandidature());
        assertEquals(futureDate, savedConvocation.getConvocationDate());
        assertEquals("Bureau 301", savedConvocation.getLocation());
        assertEquals("Message personnalisé", savedConvocation.getPersonnalMessage());
        assertEquals(Candidature.Status.CONVENED, candidature.getStatus());
    }

    @Test
    void addConvocation_withNullMessage_usesDefaultMessage() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(convocationRepository.save(any(Convocation.class))).thenReturn(new Convocation());

        // Act
        convocationService.addConvocation(1L, futureDate, "Bureau 301", null);

        // Assert
        ArgumentCaptor<Convocation> convocationCaptor = ArgumentCaptor.forClass(Convocation.class);
        verify(convocationRepository).save(convocationCaptor.capture());

        Convocation savedConvocation = convocationCaptor.getValue();
        assertEquals("Vous êtes convoqué(e) pour un entretien.", savedConvocation.getPersonnalMessage());
    }

    @Test
    void addConvocation_withEmptyMessage_usesDefaultMessage() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(convocationRepository.save(any(Convocation.class))).thenReturn(new Convocation());

        // Act
        convocationService.addConvocation(1L, futureDate, "Bureau 301", "   ");

        // Assert
        ArgumentCaptor<Convocation> convocationCaptor = ArgumentCaptor.forClass(Convocation.class);
        verify(convocationRepository).save(convocationCaptor.capture());

        Convocation savedConvocation = convocationCaptor.getValue();
        assertEquals("Vous êtes convoqué(e) pour un entretien.", savedConvocation.getPersonnalMessage());
    }

    @Test
    void addConvocation_candidatureNotFound_throwsRuntimeException() {
        // Arrange
        when(candidatureRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                convocationService.addConvocation(999L, futureDate, "Bureau 301", "Message")
        );

        assertEquals("Candidature non trouvée", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void addConvocation_nullConvocationDate_throwsIllegalArgumentException() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                convocationService.addConvocation(1L, null, "Bureau 301", "Message")
        );

        assertEquals("La date de convocation ne peut pas être nulle", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void addConvocation_pastDate_throwsIllegalArgumentException() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                convocationService.addConvocation(1L, pastDate, "Bureau 301", "Message")
        );

        assertEquals("La date de convocation ne peut pas être dans le passé", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void addConvocation_nullLocation_throwsIllegalArgumentException() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                convocationService.addConvocation(1L, futureDate, null, "Message")
        );

        assertEquals("Le lieu ne peut pas être vide", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void addConvocation_emptyLocation_throwsIllegalArgumentException() {
        // Arrange
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                convocationService.addConvocation(1L, futureDate, "   ", "Message")
        );

        assertEquals("Le lieu ne peut pas être vide", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void addConvocation_alreadyConvened_throwsIllegalStateException() {
        // Arrange
        candidature.setStatus(Candidature.Status.CONVENED);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                convocationService.addConvocation(1L, futureDate, "Bureau 301", "Message")
        );

        assertEquals("Cette candidature a déjà une convocation", exception.getMessage());
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void getAllConvocationsByInterShipOfferId_returnsConvocationDtos() {
        // Arrange
        Candidature candidature1 = Candidature.builder()
                .id(1L)
                .status(Candidature.Status.CONVENED)
                .build();

        Candidature candidature2 = Candidature.builder()
                .id(2L)
                .status(Candidature.Status.CONVENED)
                .build();

        Convocation convocation1 = new Convocation();
        convocation1.setId(10L);
        convocation1.setCandidature(candidature1);
        convocation1.setConvocationDate(LocalDateTime.now().plusDays(5));
        convocation1.setLocation("Bureau 301");
        convocation1.setPersonnalMessage("Message 1");

        Convocation convocation2 = new Convocation();
        convocation2.setId(20L);
        convocation2.setCandidature(candidature2);
        convocation2.setConvocationDate(LocalDateTime.now().plusDays(10));
        convocation2.setLocation("Bureau 302");
        convocation2.setPersonnalMessage("Message 2");

        when(convocationRepository.findByCandidatureInternshipOfferId(100L))
                .thenReturn(List.of(convocation1, convocation2));

        // Act
        List<ConvocationDto> result = convocationService.getAllConvocationsByInterShipOfferId(100L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).getId());
        assertEquals(1L, result.get(0).getCandidatureId());
        assertEquals("Bureau 301", result.get(0).getLocation());

        assertEquals(20L, result.get(1).getId());
        assertEquals(2L, result.get(1).getCandidatureId());
        assertEquals("Bureau 302", result.get(1).getLocation());

        verify(convocationRepository).findByCandidatureInternshipOfferId(100L);
    }

    @Test
    void getAllConvocationsByInterShipOfferId_noConvocations_returnsEmptyList() {
        // Arrange
        when(convocationRepository.findByCandidatureInternshipOfferId(100L))
                .thenReturn(List.of());

        // Act
        List<ConvocationDto> result = convocationService.getAllConvocationsByInterShipOfferId(100L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(convocationRepository).findByCandidatureInternshipOfferId(100L);
    }

    @Test
    void addConvocation_updatesStatusToConvened() {
        // Arrange
        assertEquals(Candidature.Status.PENDING, candidature.getStatus());
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(convocationRepository.save(any(Convocation.class))).thenReturn(new Convocation());

        // Act
        convocationService.addConvocation(1L, futureDate, "Bureau 301", "Message");

        // Assert
        assertEquals(Candidature.Status.CONVENED, candidature.getStatus());
        verify(convocationRepository).save(any(Convocation.class));
    }
}