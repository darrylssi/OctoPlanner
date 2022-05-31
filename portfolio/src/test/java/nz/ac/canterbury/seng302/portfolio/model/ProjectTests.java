package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.ValidationService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
@RunWith(MockitoJUnitRunner.class)
public class ProjectTests {
    @Autowired
    private ProjectService projectService;

    @Mock
    SprintService sprintService;

    @InjectMocks
    ValidationService validationService = Mockito.spy(ValidationService.class);

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @MockBean
    private ProjectRepository projectRepository;

    @Spy
    private DateUtils utils;

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
    public void setUp() throws ParseException {
        baseProject = new Project();
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");
        baseProject.setId(1);

        // Artificially set for validation here so that tests aren't dependent on when they are run
        creationDate = utils.toDate("2022-05-27");

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
                "Project name is required", // this should match the (message = "asdf") part of the annotation
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    void searchById_getProject() throws Exception {
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
        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate("2022-02-04");
        Date end = utils.toDate("2022-08-05");
        String errorMessage = validationService.validateProjectDates(start, end, creationDate);

        assertEquals("The sprint with dates: " +
                sprint1.getStartDateString() + " - " + sprint1.getEndDateString() +
                " is outside the project dates" , errorMessage);
    }

    @Test
    void checkProjectStartDateBetweenSprintDatesForEditProject_getErrorMessage() throws ParseException {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04
        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate("2022-01-20");
        Date end = utils.toDate("2022-01-20");
        String errorMessage = validationService.validateProjectDates(start, end, creationDate);

        assertEquals("The sprint with dates: " +
                sprint1.getStartDateString() + " - " + sprint1.getEndDateString() +
                " is outside the project dates" , errorMessage);
    }

    @Test
    void checkProjectEndDateBetweenSprintDatesForEditProject_getErrorMessage() throws ParseException {
        // Sprint list has two sprints with dates:
        // Sprint 1:  2022-01-01 -- 2022-02-02
        // Sprint 2:  2022-02-06 -- 2022-03-04
        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate("2022-01-01");
        Date end = utils.toDate("2022-02-10");
        String errorMessage = validationService.validateProjectDates(start, end, creationDate);

        assertEquals("The sprint with dates: " +
                sprint2.getStartDateString() + " - " + sprint2.getEndDateString() +
                " is outside the project dates" , errorMessage);
    }

    @Test
    void checkProjectStartDateNotTooEarlyForEditProject_getSuccess() throws ParseException {
        // Project start date cannot be more than a year before the artificially set creation date

        /* Given: setup() has been run */
        /* When: these (valid) dates are validated */
        Date start = utils.toDate("2021-05-27");
        Date end = utils.toDate("2022-10-01");
        String errorMessage = validationService.validateProjectDates(start, end, creationDate);

        /* Then: the validation should return a success */
        assertEquals("" , errorMessage);
    }

    @Test
    void checkProjectStartDateNotTooEarlyForEditProject_getErrorMessage() {
        /* Given: setup() has been run */
        /* When: these (invalid) dates are validated */
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(creationDate);
        startCal.add(Calendar.YEAR, -1);
        Date earliestStart = startCal.getTime();

        Date start = utils.toDate("2021-05-26");
        Date end = utils.toDate("2022-10-01");
        String errorMessage = validationService.validateProjectDates(start, end, creationDate);

        /* Then: the validator should catch that the start date is too early */
        assertEquals("Project cannot be set to start more than a year before it was created " +
                     "(cannot start before " + utils.toString(earliestStart) + ")" , errorMessage);
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

