package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.ValidationService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for the Project class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 * For more information on validating annotations, see https://hibernate.org/validator/documentation/getting-started/
 * Note that the annotations don't stop setters from setting invalid values, but they will stop invalid objects from
 * being saved to the database.
 */
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class ProjectTests {
    @Mock
    private ProjectService projectService;

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Mock
    private ProjectRepository projectRepository;

    private Project baseProject;

    private Date creationDate;

    private Sprint sprint1;
    private Sprint sprint2;
    private final List<Sprint> sprintList = new ArrayList<>();

    /**
     * This exists to check that dates are equivalent for the purposes of these tests. It checks
     * that the dates are on the same day, and disregards the time.
     *
     * @param expectedDate {Date} a date with the expected date value
     * @param givenDate {Date} the given/actual date value
     */
    private void assertDatesEqual(Date expectedDate, Date givenDate) throws AssertionError {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String expectedDateString = formatter.format(expectedDate);
        String givenDateString = formatter.format(givenDate);

        assertEquals(expectedDateString, givenDateString);
    }

    @BeforeEach
    public void setUp() {
        baseProject = new Project();
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("2022-01-01");
        baseProject.setEndDateString("2022-10-01");
        baseProject.setId(1);

        // Artificially set for validation here so that tests aren't dependent on when they are run
        creationDate = DateUtils.toDate("2022-05-27");

        sprint1 = new Sprint(1, "Sprint 1", "This is S1", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-02-02"), "#aabbcc");
        sprint2 = new Sprint(1, "Sprint 2", "This is S2", DateUtils.toDate("2022-02-06"), DateUtils.toDate("2022-03-04"), "#112233");
        sprint1.setSprintLabel("Sprint 1");
        sprint2.setSprintLabel("Sprint 2");
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
        Mockito.when(projectService.getProjectById(baseProject.getId())).thenReturn(baseProject);

        Project foundProject = projectService.getProjectById(baseProject.getId());
        foundProject.setStartDateString(DateUtils.toString(foundProject.getProjectStartDate()));
        foundProject.setEndDateString(DateUtils.toString(foundProject.getProjectEndDate()));
        assertEquals(foundProject.toString(), baseProject.toString());
    }

    @Test
    void saveNullProject_getException() {
        Project nullProject = new Project();
        when(projectRepository.save(nullProject)).thenThrow(TransactionSystemException.class);
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(nullProject));
    }

    @Test
    void saveNullNameProject_getException() {
        baseProject.setProjectName(null);
        when(projectRepository.save(baseProject)).thenThrow(TransactionSystemException.class);
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveEmptyNameProject_getException() {
        baseProject.setProjectName("");
        when(projectRepository.save(baseProject)).thenThrow(TransactionSystemException.class);
        assertThrows(TransactionSystemException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullDescriptionProject_getException() {
        baseProject.setProjectDescription(null);
        when(projectRepository.save(baseProject)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullStartDateProject_getException() {
        baseProject.setProjectStartDate(null);
        when(projectRepository.save(baseProject)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void saveNullEndDateProject_getException() {
        baseProject.setProjectEndDate(null);
        when(projectRepository.save(baseProject)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(baseProject));
    }

    @Test
    void checkValidProjectDateRangesForEditProject_getErrorMessage() {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        Date start = DateUtils.toDate("2022-02-04");
        Date end = DateUtils.toDate("2022-08-05");
        assert start != null;
        ValidationError error = ValidationService.validateProjectDates(start, end, creationDate, sprintList);
        String actual = error.getFirstError();
        assertEquals(sprint1.getSprintLabel() + ": " +
                sprint1.getStartDateString() + " - " + sprint1.getEndDateString() +
                " is outside the project dates" , actual);
    }

    @Test
    void checkProjectStartDateBetweenSprintDatesForEditProject_getErrorMessage() {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        Date start = DateUtils.toDate("2022-01-20");
        Date end = DateUtils.toDate("2022-01-20");
        assert start != null;
        ValidationError error = ValidationService.validateProjectDates(start, end, creationDate, sprintList);
        String actual = error.getFirstError();
        assertEquals(sprint1.getSprintLabel() + ": " +
                sprint1.getStartDateString() + " - " + sprint1.getEndDateString() +
                " is outside the project dates" , actual);
    }

    @Test
    void checkProjectEndDateBetweenSprintDatesForEditProject_getErrorMessage() {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04

        Date start = DateUtils.toDate("2022-01-01");
        Date end = DateUtils.toDate("2022-02-10");
        assert start != null;
        ValidationError error = ValidationService.validateProjectDates(start, end, creationDate, sprintList);
        String actual = error.getFirstError();
        assertEquals(sprint2.getSprintLabel() + ": " +
                sprint2.getStartDateString() + " - " + sprint2.getEndDateString() +
                " is outside the project dates" , actual);
    }

    @Test
    void checkProjectStartDateNotTooEarlyForEditProject_getSuccess() {
        // Project start date cannot be more than a year before the artificially set creation date

        /* Given: setup() has been run */
        /* When: these (valid) dates are validated */
        Date start = DateUtils.toDate("2021-05-27");
        Date end = DateUtils.toDate("2022-10-01");
        assert start != null;
        ValidationError error = ValidationService.validateProjectDates(start, end, creationDate, sprintList);
        String actual = error.getFirstError();
        /* Then: the validation should return a success */
        assertEquals("" , actual);
    }

    @Test
    void checkProjectStartDateNotTooEarlyForEditProject_getErrorMessage() {
        /* Given: setup() has been run */
        /* When: these (invalid) dates are validated */
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(creationDate);
        startCal.add(Calendar.YEAR, -1);
        Date earliestStart = startCal.getTime();

        Date start = DateUtils.toDate("2021-05-26");
        Date end = DateUtils.toDate("2022-10-01");
        assert start != null;
        ValidationError error = ValidationService.validateProjectDates(start, end, creationDate, sprintList);
        String actual = error.getFirstError();
        /* Then: the validator should catch that the start date is too early */
        assertEquals("Project cannot be set to start more than a year before it was created " +
                     "(cannot start before " + DateUtils.toDisplayString(earliestStart) + ")" , actual);
    }

    @Test
    void checkProjectCreationDateRecordedCorrectly_getSuccess() {
        /* Given: setup() has been run */
        /* When: the creation date is fetched */
        Date recordedCreation = baseProject.getProjectCreationDate();

        /* Then: the current date should match the creation date (unless it has just crossed over
         * midnight, in which case it could match yesterday) */
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String currentTime = formatter.format(currentDate);
        if (!currentTime.equals("00:00")) {
            assertDatesEqual(currentDate, recordedCreation);
        } else {
            /* Check if the date matches today; otherwise check if it was yesterday */
            try {
                assertDatesEqual(currentDate, recordedCreation);
            } catch (AssertionError err) {
                /* Convert to calculate yesterday */
                Calendar conversionCal = Calendar.getInstance();
                conversionCal.setTime(currentDate);
                conversionCal.add(Calendar.DATE, -1);
                Date yesterday = conversionCal.getTime();

                assertDatesEqual(yesterday, recordedCreation);
            }
        }
    }
}

