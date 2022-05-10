package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

/**
 * Holds unit tests for the Project class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 * For more information on validating annotations, see https://hibernate.org/validator/documentation/getting-started/
 * Note that the annotations don't stop setters from setting invalid values, but they will stop invalid objects from
 * being saved to the database.
 */
@SpringBootTest
public class ProjectTests {
    @Autowired
    private ProjectService projectService;

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @MockBean
    private ProjectRepository projectRepository;

    @Autowired
    private DateUtils utils;

    private Project baseProject;

    private Sprint sprint1;
    private Sprint sprint2;
    private List<Sprint> sprintList = new ArrayList<>();

    @BeforeEach
    public void setUp() throws ParseException {
        baseProject = new Project();
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");
        baseProject.setId(1);

        sprint1 = new Sprint(1, "Sprint 1", "This is S1", utils.toDate("2022-01-01"), utils.toDate("2022-02-02"));
        sprint2 = new Sprint(1, "Sprint 2", "This is S2", utils.toDate("2022-02-06"), utils.toDate("2022-03-04"));
        sprintList.add(sprint1);
        sprintList.add(sprint2);
    }

    @Test
    public void setNullName_getViolation() {
        baseProject.setProjectName(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Project name is required", // this should match the (message = "asdf") part of the annotation
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void searchById_getProject() throws Exception {
        projectRepository.save(baseProject);
        when(projectRepository.findProjectById(baseProject.getId())).thenReturn(baseProject);
        assertThat(projectService.getProjectById(baseProject.getId())).isEqualTo(baseProject);
    }

    @Test
    void saveNullProject_getException() {
        try { // this is how to get at nested exceptions
            projectRepository.save(new Project());
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullNameProject_getException() {
        try {
            baseProject.setProjectName(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyNameProject_getException() {
        try {
            baseProject.setProjectName("");
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullDescriptionProject_getException() {
        try {
            baseProject.setProjectDescription(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullStartDateProject_getException() {
        try {
            baseProject.setProjectStartDate(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Testpackage nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.controller.EditSprintController;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;


/**
 * Holds unit tests for the Sprint class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
public class SprintTests {
    @Autowired
    private SprintService sprintService;

    @Autowired
    private DateUtils utils;

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @MockBean
    private SprintRepository sprintRepository;

    private List<Sprint> sprintList = new ArrayList<>();
    private Sprint baseSprint;

    @BeforeEach
    public void setUp() {
        baseSprint = new Sprint();
        baseSprint.setSprintName("Sprint 1");
        baseSprint.setSprintDescription("The first.");
        baseSprint.setParentProjectId(5);
        baseSprint.setStartDateString("05/FEB/2022");
        baseSprint.setEndDateString("24/MAR/2022");
        sprintList.add(baseSprint);
    }

    @Test
    public void searchByName_getSprint() {
        String nameToSearch = "Sprint 1";
        when(sprintRepository.findBySprintName(nameToSearch)).thenReturn(List.of(baseSprint));
        assertThat(sprintService.getSprintByName(nameToSearch)).isEqualTo(List.of(baseSprint));
    }

    @Test
    public void searchById_getSprint() throws Exception {
        when(sprintRepository.findSprintById(baseSprint.getId())).thenReturn(baseSprint);
        assertThat(sprintService.getSprintById(baseSprint.getId())).isEqualTo(baseSprint);
    }

    @Test
    public void searchByParentProjectId_getSprint() {
        int parentProjectIdToSearch = 5;
        baseSprint.setParentProjectId(parentProjectIdToSearch);
        when(sprintRepository.findByParentProjectId(parentProjectIdToSearch)).thenReturn(List.of(baseSprint));
        assertThat(sprintService.getSprintByParentProjectId(parentProjectIdToSearch)).isEqualTo(List.of(baseSprint));
    }

    @Test
    void saveNullSprint_getException() {
        try { // this is how to get at nested exceptions
            sprintRepository.save(new Sprint());
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullNameSprint_getException() {
        try {
            baseSprint.setSprintName(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyNameSprint_getException() {
        try {
            baseSprint.setSprintName("");
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNameSprint_getSprintName() {
        baseSprint.setSprintName("Sprint 2");
        sprintRepository.save(baseSprint);
        assertEquals("Sprint 2", baseSprint.getSprintName());
    }

    @Test
    void saveNullDescriptionSprint_getException() {
        try {
            baseSprint.setSprintDescription(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyDescriptionSprint_getEmptyDescription() {
        baseSprint.setSprintDescription("");
        sprintRepository.save(baseSprint);
        assertEquals("", baseSprint.getSprintDescription());
    }

    @Test
    void saveDescriptionSprint_getDescription() {
        baseSprint.setSprintDescription("This is Sprint 2");
        sprintRepository.save(baseSprint);
        assertEquals("This is Sprint 2", baseSprint.getSprintDescription());
    }

    @Test
    void saveNullStartDateSprint_getException() {
        try {
            baseSprint.setStartDate(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullEndDateSprint_getException() {
        try {
            baseSprint.setEndDate(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void checkGivenDatesAreValid_getStringMessage() throws Exception {
        Date projectStartDate = utils.toDate("2022-01-02");
        Date projectEndDate = utils.toDate("2022-12-31");
        Date sprintStartDate = utils.toDate("2022-01-02");
        Date sprintEndDate = utils.toDate("2022-02-02");
        String errorMessage = baseSprint.validSprintDateRanges(2, sprintStartDate, sprintEndDate,
                projectStartDate, projectEndDate, sprintList);

        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        // Since our selected dates are 01/01/2022 -- 02/02/2022, this should return the error message as "", which tells
        // us that the dates are valid
        assertEquals("", errorMessage);
    }

    //
    @Test
    void checkSprintStartDateOverlapsProjectStartDate_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-01";
        String sprintEndDate = "2022-02-02";
        String errorMessage = baseSprint.validSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate, errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDate_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";
        String errorMessage = baseSprint.validSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate,
                errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDate_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";
        String errorMessage = baseSprint.validSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDates_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-05";
        String sprintEndDate = "2022-03-24";
        String errorMessage = baseSprint.validSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & and it must not be same, it is overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

    @Test
    void checkCurrentSprintDatesOverlapsSprintListDates_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-25";
        String sprintEndDate = "2022-04-02";
        String errorMessage = baseSprint.validSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & it is overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

}

    void saveNullEndDateProject_getException() {
        try {
            baseProject.setProjectEndDate(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }


    @Test
    void checkValidProjectDateRangesForEditProject_getErrorMessage() throws ParseException {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        String newProjectStartDate = "2022-02-04";
        String newProjectEndDate = "2022-08-05";
        String errorMessage = baseProject.validEditProjectDateRanges(utils.toDate(newProjectStartDate), utils.toDate(newProjectEndDate), sprintList);

        assertEquals("Project dates must not be before or after the sprint dates " + utils.toString(sprint1.getSprintStartDate())
                + " - " + utils.toString(sprint1.getSprintEndDate()) , errorMessage);
    }

    @Test
    void checkProjectStartDateBetweenSprintDatesForEditProject_getErrorMessage() throws ParseException {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        String newProjectStartDate = "2022-01-20";
        String newProjectEndDate = "2022-08-20";
        String errorMessage = baseProject.validEditProjectDateRanges(utils.toDate(newProjectStartDate), utils.toDate(newProjectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & it is overlapping with " + utils.toString(sprint1.getSprintStartDate())
                + " - " + utils.toString(sprint1.getSprintEndDate()) , errorMessage);
    }

    @Test
    void checkProjectEndDateBetweenSprintDatesForEditProject_getErrorMessage() throws ParseException {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        String newProjectStartDate = "2022-01-01";
        String newProjectEndDate = "2022-02-10";
        String errorMessage = baseProject.validEditProjectDateRanges(utils.toDate(newProjectStartDate), utils.toDate(newProjectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & it is overlapping with " + utils.toString(sprint2.getSprintStartDate())
                + " - " + utils.toString(sprint2.getSprintEndDate()) , errorMessage);
    }

}

