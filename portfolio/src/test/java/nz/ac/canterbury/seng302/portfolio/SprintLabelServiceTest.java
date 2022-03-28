package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
public class SprintLabelServiceTest {
    @Autowired
    private SprintLabelService sprintLabelService;
    @MockBean
    private SprintService sprintService;
    @MockBean
    private ProjectService projectService;
    @Autowired
    private DateUtils utils;

    private Project testProject1;
    private Project testProject2;
    private Sprint sprint1;
    private Sprint sprint2;
    private Sprint sprint3;
    private final int PROJECT_ID_1 = 0;
    private final int PROJECT_ID_2 = 1;

    @BeforeEach
    void setUp() throws ParseException {
        testProject1 = new Project("Name1", "desc1", utils.toDate("2022-01-01"), utils.toDate("2022-12-30"));
        testProject1.setId(PROJECT_ID_1);
        testProject2 = new Project("Name2", "desc2", utils.toDate("2023-01-01"), utils.toDate("2023-12-30"));
        testProject2.setId(PROJECT_ID_2);

        sprint1 = new Sprint(PROJECT_ID_1, "Sprint 1", "label1", "desc", utils.toDate("2022-01-01"), utils.toDate("2022-02-01"));
        sprint2 = new Sprint(PROJECT_ID_1, "Sprint 2", "label2", "desc", utils.toDate("2022-02-02"), utils.toDate("2022-03-01"));
        sprint3 = new Sprint(PROJECT_ID_1, "Sprint 3", "label3", "desc", utils.toDate("2022-04-02"), utils.toDate("2022-05-01"));
        List<Sprint> sprintList = Arrays.asList(sprint1, sprint2, sprint3);

        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(sprintList);
    }

    // NOTE: some methods aren't tested with both the Project object and Id in inputs
    // this is because Project inputs just call the id method but with project.getId()
    // and we know that Project.getID() works, so it is only tested as necessary
    // all actual implementations of methods in SprintLabelService ARE tested
    // also, refreshAllLabels just calls other methods, so it isn't checked for adding/deleting sprints etc.
    // as there are other tests for that

    @Test
    void refreshAllLabels_labelsAssignedProperlyAndNextLabel() throws ParseException {
        when(projectService.getAllProjects()).thenReturn(Arrays.asList(testProject1, testProject2));
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint2));
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_2)).thenReturn(List.of(sprint3));

        sprint3.setParentProjectId(PROJECT_ID_2);
        sprint3.setStartDate(utils.toDate("2023-04-02"));
        sprint3.setEndDate(utils.toDate("2022-05-01"));

        sprintLabelService.refreshAllSprintLabels();
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint1", sprint3.getLabel());

        Assertions.assertEquals("sprint3", sprintLabelService.nextLabel(PROJECT_ID_1));
        Assertions.assertEquals("sprint2", sprintLabelService.nextLabel(PROJECT_ID_2));
    }

    @Test
    void refreshLabelsWithID_labelsAssignedProperly() {
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint3", sprint3.getLabel());
    }
    @Test
    void refreshLabelsWithProject_labelsAssignedProperly() {
        sprintLabelService.refreshProjectSprintLabels(testProject1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint3", sprint3.getLabel());
    }

    @Test
    void refreshLabelsWithId_getNextLabel() {
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint4", sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void refreshLabelsWithProject_getNextLabel() {
        sprintLabelService.refreshProjectSprintLabels(testProject1);
        Assertions.assertEquals("sprint4", sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void deleteSprint_labelsAndNextLabelAdjust() {
        // assign base labels & verify
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint3", sprint3.getLabel());

        // "delete" a sprint, and expect that the labels adjust, including nextLabel()
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint3));
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint3.getLabel());
        Assertions.assertEquals("sprint3", sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void addSprint_labelsAndNextLabelAdjust() throws ParseException {
        // assign base labels & verify
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint3", sprint3.getLabel());

        // "add" a sprint, and expect that the labels adjust, including nextLabel()
        Sprint sprint4 = new Sprint(PROJECT_ID_1, "Sprint 4", "label4", "desc", utils.toDate("2022-03-02"), utils.toDate("2022-04-01"));
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint2, sprint3, sprint4));
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals("sprint1", sprint1.getLabel());
        Assertions.assertEquals("sprint2", sprint2.getLabel());
        Assertions.assertEquals("sprint3", sprint4.getLabel()); // sprint4 starts before sprint3, so it should slot in here
        Assertions.assertEquals("sprint4", sprint3.getLabel());
        Assertions.assertEquals("sprint5", sprintLabelService.nextLabel(PROJECT_ID_1));
    }
}
