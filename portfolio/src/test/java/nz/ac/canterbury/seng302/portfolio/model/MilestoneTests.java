package nz.ac.canterbury.seng302.portfolio.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for the Milestone class.
 * This ensures that the JPA annotations work correctly.
 */
@SpringBootTest
class MilestoneTests {

    private Milestone milestone;

    @MockBean
    private MilestoneRepository milestoneRepository;

    @BeforeEach
    void setup() {
        milestone = new Milestone("milestone", "description", new Date());
    }

    @Test
    void saveNullMilestoneName_getException() {
        milestone.setName(null);
        when(milestoneRepository.save(milestone)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> milestoneRepository.save(milestone));
    }

    @ParameterizedTest
    @ValueSource (strings = {"a", "This is a very long milestone name that is longer than the maximum limit"})
    void saveInvalidMilestoneName_getException(String name) {
        milestone.setName(name);
        when(milestoneRepository.save(milestone)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> milestoneRepository.save(milestone));
    }

    @Test
    void saveLongMilestoneDescription_getException() {
        milestone.setDescription("This is a really really long milestone description that is hopefully longer than the maximum limit of two hundred " +
                "characters which is a lot longer than I thought it would be so here are few more characters");
        when(milestoneRepository.save(milestone)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> milestoneRepository.save(milestone));
    }

    @Test
    void saveNullMilestoneDescription_getException() {
        when(milestoneRepository.save(milestone)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> milestoneRepository.save(milestone));
    }

    @Test
    void saveNullMilestoneDate_getException() {
        milestone.setStartDate(null);
        when(milestoneRepository.save(milestone)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> milestoneRepository.save(milestone));
    }

}
