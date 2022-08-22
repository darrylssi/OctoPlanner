package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for the Deadline class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
class DeadlineTests {
    @Mock
    private DeadlineService deadlineService;

    @Mock
    private DeadlineRepository deadlineRepository;

    private final List<Deadline> deadlineList = new ArrayList<>();
    private Deadline baseDeadline;

    @BeforeEach
    public void setUp() {
        int parentProjId = 5;
        Project baseProject = new Project();
        baseProject.setId(parentProjId);
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("2022-01-01");
        baseProject.setEndDateString("2022-10-01");
        baseProject.setId(parentProjId);

        baseDeadline = new Deadline();
        baseDeadline.setName("Deadline 1");
        baseDeadline.setDescription("The first.");
        baseDeadline.setParentProject(baseProject);
        baseDeadline.setStartDate(DateUtils.toDateTime("2022-02-05 17:00"));
        deadlineList.add(baseDeadline);
    }

    @Test
    void searchByName_getDeadline() {
        String nameToSearch = "Deadline 1";
        when(deadlineService.getDeadlineByName(nameToSearch)).thenReturn(deadlineList);
        List<Deadline> foundDeadlines = deadlineService.getDeadlineByName(nameToSearch);
        Deadline foundDeadline = foundDeadlines.get(0);
        assertEquals(baseDeadline, foundDeadline);
    }

    @Test
    void searchById_getDeadline() {
        when(deadlineService.getDeadlineById(baseDeadline.getId())).thenReturn(baseDeadline);
        Deadline foundDeadline = deadlineService.getDeadlineById(baseDeadline.getId());
        assertEquals(baseDeadline, foundDeadline);
    }

    @Test
    void searchByParentProjectId_getDeadline() {
        when(deadlineService.getDeadlinesInProject(baseDeadline.getParentProject().getId())).thenReturn(deadlineList);
        List<Deadline> foundDeadlines = deadlineService.getDeadlinesInProject(baseDeadline.getParentProject().getId());
        Deadline foundDeadline = foundDeadlines.get(0);
        assertEquals(baseDeadline, foundDeadline);
    }

}
