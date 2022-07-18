package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;

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

/**
 * This class tests the SprintLabelService, which is responsible for assigning labels to Sprint objects such that
 * each sprint has a label like "Sprint 1", "Sprint 2" etc., in order of starting date.
 */
@SpringBootTest
class SprintLabelServiceTest {
    @Autowired
    private SprintLabelService sprintLabelService;
    @MockBean
    private SprintService sprintService;
    @MockBean
    private ProjectService projectService;

    private Project testProject1;
    private Project testProject2;
    private Sprint sprint1;
    private Sprint sprint2;
    private Sprint sprint3;
    private final int PROJECT_ID_1 = 0;
    private final int PROJECT_ID_2 = 1;
    private final String BASE = SprintLabelService.SPRINT_LABEL_BASE; // to type it more easily

    @BeforeEach
    void setUp() {
        testProject1 = new Project("Name1", "desc1", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-30"));
        testProject1.setId(PROJECT_ID_1);
        testProject2 = new Project("Name2", "desc2", DateUtils.toDate("2023-01-01"), DateUtils.toDate("2023-12-30"));
        testProject2.setId(PROJECT_ID_2);

        sprint1 = new Sprint(PROJECT_ID_1, "Sprint 1", "desc", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-02-01"), "#aabbcc");
        sprint2 = new Sprint(PROJECT_ID_1, "Sprint 2", "desc", DateUtils.toDate("2022-02-02"), DateUtils.toDate("2022-03-01"), "#884477");
        sprint3 = new Sprint(PROJECT_ID_1, "Sprint 3", "desc", DateUtils.toDate("2022-04-02"), DateUtils.toDate("2022-05-01"), "#aa0055");
        List<Sprint> sprintList = Arrays.asList(sprint1, sprint2, sprint3);

        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(sprintList);
    }

    // NOTE: some methods aren't tested with both the Project object and Id in inputs
    // this is because Project inputs just call the id method but with project.getId()
    // and we know that Project.getID() works, so it is only tested as necessary
    // all actual implementations of methods in SprintLabelService ARE tested
    // also, refreshAllLabels just calls other methods, so it isn't checked for adding/deleting sprints etc.
    // as there are other tests for that

    /**
     * This test covers sprints across two projects being correctly assigned labels when refreshing all labels.
     */
    @Test
    void refreshAllLabels_labelsAssignedProperlyAndNextLabel() {
        sprint3.setParentProjectId(PROJECT_ID_2);
        sprint3.setStartDate(DateUtils.toDate("2023-04-02"));
        sprint3.setEndDate(DateUtils.toDate("2022-05-01"));

        when(projectService.getAllProjects()).thenReturn(Arrays.asList(testProject1, testProject2));
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint2));
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_2)).thenReturn(Arrays.asList(sprint3));
        // DO NOT change this (or anything else) to List.of(), even though Intellij wants you to, because it will break things
        // see https://stackoverflow.com/questions/46579074/what-is-the-difference-between-list-of-and-arrays-aslist

        sprintLabelService.refreshAllSprintLabels();

        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 1, sprint3.getSprintLabel());

        Assertions.assertEquals(BASE + 3, sprintLabelService.nextLabel(PROJECT_ID_1));
        Assertions.assertEquals(BASE + 2, sprintLabelService.nextLabel(PROJECT_ID_2));
    }

    /**
     * Checks that labels are refreshed correctly with just one sprint, and nextLabel() returns correctly.
     */
    @Test
    void refreshLabelsOneSprint_labelAssignedProperly() {
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1)); // no List.of()
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);

        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprintLabelService.nextLabel(testProject1));
    }

    /**
     * Checks that refreshing works using a project id (integer).
     */
    @Test
    void refreshLabelsWithID_labelsAssignedProperly() {
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprint3.getSprintLabel());
    }

    /**
     * Checks that refreshing works using a project object.
     */
    @Test
    void refreshLabelsWithProject_labelsAssignedProperly() {
        sprintLabelService.refreshProjectSprintLabels(testProject1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprint3.getSprintLabel());
    }

    @Test
    void refreshLabelsWithId_getNextLabel() {
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 4, sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void refreshLabelsWithProject_getNextLabel() {
        sprintLabelService.refreshProjectSprintLabels(testProject1);
        Assertions.assertEquals(BASE + 4, sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void deleteSprint_labelsAndNextLabelAdjust() {
        // assign base labels & verify
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprint3.getSprintLabel());

        // "delete" a sprint, and expect that the labels adjust, including nextLabel()
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint3));
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint3.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprintLabelService.nextLabel(PROJECT_ID_1));
    }

    @Test
    void addSprint_labelsAndNextLabelAdjust() {
        // assign base labels & verify
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprint3.getSprintLabel());

        // "add" a sprint, and expect that the labels adjust, including nextLabel()
        Sprint sprint4 = new Sprint(PROJECT_ID_1, "Sprint 4", "desc", DateUtils.toDate("2022-03-02"), DateUtils.toDate("2022-04-01"), "#987654");
        when(sprintService.getSprintsOfProjectById(PROJECT_ID_1)).thenReturn(Arrays.asList(sprint1, sprint2, sprint3, sprint4));
        sprintLabelService.refreshProjectSprintLabels(PROJECT_ID_1);
        Assertions.assertEquals(BASE + 1, sprint1.getSprintLabel());
        Assertions.assertEquals(BASE + 2, sprint2.getSprintLabel());
        Assertions.assertEquals(BASE + 3, sprint4.getSprintLabel()); // sprint4 starts before sprint3, so it should slot in here
        Assertions.assertEquals(BASE + 4, sprint3.getSprintLabel());
        Assertions.assertEquals(BASE + 5, sprintLabelService.nextLabel(PROJECT_ID_1));
    }
}
