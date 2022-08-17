package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
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
class DeadlineServiceTest {
    @Autowired
    private DeadlineService deadlineService;

    @MockBean
    private DeadlineRepository deadlineRepository;

    private Deadline deadline;

    @BeforeEach
    void setup() {
        deadline = new Deadline(0, "deadline", "description", new Date());
    }

    @Test
    void getDeadlineValidId_thenReturnDeadline() {
        when(deadlineRepository.findDeadlineById(1))
                .thenReturn(deadline);
        assertThat(deadlineService.getDeadlineById(1)).isEqualTo(deadline);
    }

    @Test
    void getDeadlineInvalidId_thenThrowException() {
        when(deadlineRepository.findDeadlineById(1))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadline not found."));
        Exception exception = assertThrows(ResponseStatusException.class, () -> deadlineService.getDeadlineById(1));
        assertTrue(exception.getMessage().contains("Deadline not found"));
    }

    @Test
    void saveDeadline() {
        when(deadlineRepository.save(deadline)).thenReturn(deadline);
        deadlineService.saveDeadline(deadline);
        verify(deadlineRepository, times(1)).save(deadline);
    }
}
