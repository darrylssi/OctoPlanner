package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.MilestoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class MilestoneServiceTest {

    @Autowired
    private MilestoneService milestoneService;

    @MockBean
    private MilestoneRepository milestoneRepository;

    private Milestone milestone;

    @BeforeEach
    void setup() {
        milestone = new Milestone(0, "milestone", "description", new Date());
    }

    @Test
    void getMilestoneValidId_thenReturnMilestone() {
        when(milestoneRepository.findMilestoneById(1))
                .thenReturn(milestone);
        assertThat(milestoneService.getMilestoneById(1)).isEqualTo(milestone);
    }

    @Test
    void getMilestoneInvalidId_thenThrowException() {
        when(milestoneRepository.findMilestoneById(1))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found."));
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            milestoneService.getMilestoneById(1);
        });
        assertTrue(exception.getMessage().contains("Milestone not found"));
    }

    @Test
    void saveMilestone() {
        when(milestoneRepository.save(milestone)).thenReturn(milestone);
        milestoneService.saveMilestone(milestone);
        verify(milestoneRepository, times(1)).save(milestone);
    }

}
