package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Holds unit tests for the DeadlineService class.
 */
@SpringBootTest
public class DeadlineServiceTest {
    @Autowired
    private DeadlineService deadlineService;
    @MockBean
    private DeadlineRepository deadlineRepository;

    private Deadline deadline1;

    @BeforeEach
    void setUp() {
        deadline1 = new Deadline(0, "name", "description", new Date());
    }

    @Test
    void getDeadlineValidId_thenReturnDeadline() throws Exception {
        when(deadlineRepository.findDeadlineById(1))
                .thenReturn(deadline1);

        assertThat(deadlineService.getDeadlineById(1)).isEqualTo(deadline1);
    }

    @Test
    void getDeadlineInvalidId_thenThrowException() {
        Exception e = assertThrows(Exception.class, () -> {
            deadlineService.getDeadlineById(2);
        });
        String expectedMessage = "Deadline not found.";
        assertTrue(e.getMessage().contains(expectedMessage));
    }

    @Test
    void saveDeadline() {
        when(deadlineRepository.save(deadline1)).thenReturn(deadline1);
        deadlineService.saveDeadline(deadline1);
        verify(deadlineRepository, times(1)).save(deadline1);
    }
}
