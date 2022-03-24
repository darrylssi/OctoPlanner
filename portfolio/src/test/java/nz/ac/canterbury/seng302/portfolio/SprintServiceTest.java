package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SprintServiceTest {

    @Autowired
    private SprintService sprintService;

    @MockBean
    private SprintRepository sprintRepository;

    private Sprint sprint1;

    @BeforeEach
    void setUp() {
        sprint1 = new Sprint(0, "name", "sprint1", "description", new Date(), new Date());
    }

    @Test
    void getProjectValidId() throws Exception {
        when(sprintRepository.findSprintById(1))
                .thenReturn(sprint1);

        assertThat(sprintService.getSprintById(1)).isEqualTo(sprint1);
    }

    @Test
    void getProjectInvalidId() throws Exception{
        when(sprintRepository.findSprintById(2))
                .thenReturn(null);
        assertThat(sprintService.getSprintById(2)).isEqualTo(null);
    }

    @Test
    void saveSprint() {
        when(sprintRepository.save(sprint1)).thenReturn(sprint1);
        sprintService.saveSprint(sprint1);
        verify(sprintRepository, times(1)).save(sprint1);
    }

}
