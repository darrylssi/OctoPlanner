package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class SprintServiceTest {

    @Autowired
    private SprintService sprintService;

    @MockBean
    private SprintRepository sprintRepository;

    private Sprint sprint1;

    @BeforeEach
    void setUp() {
        sprint1 = new Sprint(0, "name", "description", new Date(), new Date(), "#ff00aa");
    }

    @Test
    void getSprintValidId_thenReturnSprint() {
        when(sprintRepository.findSprintById(1))
                .thenReturn(sprint1);

        assertThat(sprintService.getSprintById(1)).isEqualTo(sprint1);
    }

    @Test
    void getSprintInvalidId_thenThrowException() {
        Exception e = assertThrows(ResponseStatusException.class, () -> sprintService.getSprintById(2));
        String expectedMessage = "Sprint not found.";
        assertTrue(e.getMessage().contains(expectedMessage));
    }

    @Test
    void saveSprint() {
        when(sprintRepository.save(sprint1)).thenReturn(sprint1);
        sprintService.saveSprint(sprint1);
        verify(sprintRepository, times(1)).save(sprint1);
    }

}
