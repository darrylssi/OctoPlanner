package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
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

    private Project baseProject;

    @BeforeEach
    public void setUpProject() {
        baseProject = new Project();
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");
        baseProject.setId(1);
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
    void saveNameProject_getProjectName() {
        baseProject.setProjectName("Project 2");
        projectRepository.save(baseProject);
        assertEquals("Project 2", baseProject.getProjectName());
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
    void saveEmptyDescriptionProject_getEmptyDescription() {
        baseProject.setProjectDescription("");
        projectRepository.save(baseProject);
        assertEquals("", baseProject.getProjectDescription());
    }

    @Test
    void saveDescriptionProject_getProjectName() {
        baseProject.setProjectDescription("This is Project 2");
        projectRepository.save(baseProject);
        assertEquals("This is Project 2", baseProject.getProjectDescription());
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

    @Test
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

