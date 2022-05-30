package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DateUtils utils;

    private Project baseProject;

    private Sprint sprint1;
    private Sprint sprint2;
    private List<Sprint> sprintList;

    @BeforeEach
    public void setUp() throws ParseException {
        sprintList = new ArrayList<>();
        projectRepository.deleteAll();
        baseProject = new Project();
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");

        sprint1 = new Sprint(1, "Sprint 1", "This is S1", utils.toDate("2022-01-01"), utils.toDate("2022-02-02"), "#aabbcc");
        sprint2 = new Sprint(1, "Sprint 2", "This is S2", utils.toDate("2022-02-06"), utils.toDate("2022-03-04"), "#112233");
        sprintList.add(sprint1);
        sprintList.add(sprint2);
    }

    @Test
    void setNullName_getViolation() {
        baseProject.setProjectName(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Project name is required", // this should match the (message = "test") part of the annotation
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    void searchById_getProject() throws Exception {
        projectService.saveProject(baseProject);
        Project foundProject = projectService.getProjectById(baseProject.getId());
        foundProject.setStartDateString(Project.dateToString(foundProject.getProjectStartDate()));
        foundProject.setEndDateString(Project.dateToString(foundProject.getProjectEndDate()));
        assertEquals(foundProject.toString(), baseProject.toString());
    }

    @Test
    void saveNullProject_getException() {
        Project nullProject = new Project();
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(nullProject));
    }

    @Test
    void saveNullNameProject_getException() {
        baseProject.setProjectName(null);
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveEmptyNameProject_getException() {
        baseProject.setProjectName("");
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullDescriptionProject_getException() {
        baseProject.setProjectDescription(null);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullStartDateProject_getException() {
        baseProject.setProjectStartDate(null);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullEndDateProject_getException() {
        baseProject.setProjectEndDate(null);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
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

