package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the ValidationUtils, which is responsible for validating dates in the model classes
 * such as projects, sprints and events.
 */
@SpringBootTest
class ValidationUtilsTest {

    private Project baseProject;
    private Sprint sprint1;
    private Sprint sprint2;
    private Sprint sprint3;
    private Sprint testSprint;
    private List<Sprint> sprintList;

    @BeforeEach
    void setUp(){
        baseProject = new Project("Base Project", "Base Description",
                DateUtils.toDate("2022-01-01"), DateUtils.toDate("2023-01-01"));
        int BASE_PROJECT_ID = 0;
        baseProject.setId(BASE_PROJECT_ID);
        baseProject.setProjectCreationDate(DateUtils.toDate("2022-01-01"));

        sprint1 = new Sprint(BASE_PROJECT_ID, "Sprint One", "First Sprint Description",
                DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-02-01"), "#aabbcc");
        sprint1.setSprintLabel("Sprint 1");
        sprint1.setId(1);
        sprint2 = new Sprint(BASE_PROJECT_ID, "Sprint Two", "Second Sprint Description",
                DateUtils.toDate("2022-02-02"), DateUtils.toDate("2022-03-01"), "#bbccdd");
        sprint2.setSprintLabel("Sprint 2");
        sprint2.setId(2);
        sprint3 = new Sprint(BASE_PROJECT_ID, "Sprint Three", "Third Sprint Description",
                DateUtils.toDate("2022-03-02"), DateUtils.toDate("2022-04-01"), "#ccddee");
        sprint3.setSprintLabel("Sprint 3");
        sprint3.setId(3);

        testSprint = new Sprint(BASE_PROJECT_ID, "Test Sprint", "Test Sprint Description",
                DateUtils.toDate("2022-01-15"), DateUtils.toDate("2022-02-15"), "#ddeeff");

        sprintList = Arrays.asList(sprint1, sprint2, sprint3);
    }

    @Test
    void validSprintDates_noErrorFlag() {
        ValidationError result = ValidationUtils.validateSprintDates(sprint1.getId(), sprint1.getSprintStartDate(),
                sprint1.getSprintEndDate(), baseProject, sprintList);
        assertFalse(result.isError());
    }

    @Test
    void validProjectDates_noErrorFlag() {
        ValidationError result = ValidationUtils.validateProjectDates(baseProject.getProjectStartDate(), baseProject.getProjectEndDate(),
                baseProject.getProjectCreationDate(), sprintList);
        assertFalse(result.isError());
    }

    @Test
    void sprintDatesInWrongOrder_getErrorMessage() {
        ValidationError result = ValidationUtils.validateSprintDates(sprint1.getId(), sprint1.getSprintEndDate(),
                sprint1.getSprintStartDate(), baseProject, sprintList);
        String actual = result.getFirstError();
        assertTrue(result.isError());
        assertEquals(ValidationUtils.DATES_IN_WRONG_ORDER_MESSAGE, actual);
    }

    @Test
    void projectDatesInWrongOrder_getErrorMessage() {
        ValidationError result = ValidationUtils.validateProjectDates(baseProject.getProjectEndDate(), baseProject.getProjectStartDate(),
                baseProject.getProjectCreationDate(), sprintList);
        String actual = result.getFirstError();
        assertTrue(result.isError());
        assertEquals(ValidationUtils.DATES_IN_WRONG_ORDER_MESSAGE, actual);
        assertEquals(4, result.getErrorMessages().size());
    }

    @ParameterizedTest
    @CsvSource({"2021-12-31,2022-02-01",
    "2005-01-01,2005-02-01",
    "2030-01-01,2030-02-01",
    "2022-11-01,2023-01-02"})
    void testDatesOutsideProject_getTrue(String startString, String endString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        boolean result = ValidationUtils.datesOutsideProject(start, end,
                baseProject.getProjectStartDate(), baseProject.getProjectEndDate());
        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"2021-12-31", "2005-01-01",
            "2005-02-01", "2030-01-01",
            "2030-02-01", "2023-01-02"})
    void testDateOutsideProject_getTrue(String dateString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date date = DateUtils.toDate(dateString);
        assert date != null;
        boolean result = ValidationUtils.dateOutsideProject(date,
                baseProject.getProjectStartDate(), baseProject.getProjectEndDate());
        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"2021-12-31,2022-02-01",
            "2005-01-01,2005-02-01",
            "2030-01-01,2030-02-01",
            "2022-11-01,2023-01-02"})
    void testSprintDatesOutsideProject_getErrorMessage(String startString, String endString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        ValidationError result = ValidationUtils.validateSprintDates(4, start, end, baseProject, sprintList);
        assertTrue(result.isError());
        String expected = "Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString();
        assertTrue(result.getErrorMessages().contains(expected));
    }

    @ParameterizedTest
    @CsvSource({"2022-01-01,2022-02-01",
    "2022-01-02,2022-12-31",
    "2022-06-08,2023-01-01",
    "2022-01-01,2023-01-01"})
    void testValidDatesOutsideProject_getFalse(String startString, String endString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        boolean result = ValidationUtils.datesOutsideProject(start, end,
                baseProject.getProjectStartDate(), baseProject.getProjectEndDate());
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"2022-01-01", "2022-02-01",
            "2022-01-02", "2022-12-31",
            "2022-06-08", "2023-01-01",
            "2022-01-01", "2023-01-01"})
    void testValidDateOutsideProject_getFalse(String dateString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date date = DateUtils.toDate(dateString);
        assert date != null;
        boolean result = ValidationUtils.dateOutsideProject(date,
                baseProject.getProjectStartDate(), baseProject.getProjectEndDate());
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"2022-01-01,2022-02-01",
            "2022-01-02,2022-12-31",
            "2022-06-08,2023-01-01",
            "2022-01-01,2023-01-01"})
    void testValidSprintDatesOutsideProject_noErrorFlag(String startString, String endString) {
        // Project dates are 2022-01-01 to 2023-01-01
        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        ValidationError result = ValidationUtils.validateSprintDates(4, start, end, baseProject,
                Collections.emptyList());
        assertFalse(result.isError());
    }

    @Test
    void testProjectStartDateTooEarly_getErrorMessage() {
        Date newStart = DateUtils.toDate("2020-01-01");
        assert newStart != null;
        ValidationError result = ValidationUtils.validateProjectDates(newStart, baseProject.getProjectEndDate(),
                baseProject.getProjectCreationDate(), sprintList);
        String actual = result.getFirstError();
        assertTrue(result.isError());
        assertEquals("Project cannot be set to start more than a year before it was created " +
                "(cannot start before 01/Jan/2021)", actual);
    }

    @Test
    void testSprintBeforeOtherDontOverlap_getTrue() {
        boolean result = ValidationUtils.sprintDatesOverlap(sprint1.getSprintStartDate(),
                sprint1.getSprintEndDate(), sprint2);
        assertTrue(result);
    }

    @Test
    void testSprintAfterOtherDontOverlap_getTrue() {
        boolean result = ValidationUtils.sprintDatesOverlap(sprint3.getSprintStartDate(),
                sprint3.getSprintEndDate(), sprint2);
        assertTrue(result);
    }

    @Test
    void testSprintDatesOverlap_getFalse() {
        boolean result = ValidationUtils.sprintDatesOverlap(testSprint.getSprintStartDate(),
                testSprint.getSprintEndDate(), sprint2);
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({ "''", "' '", "🤯", "'hello, world'"})
    void testInvalidName_getErrorMessage(String name) {
        ValidationError result = ValidationUtils.validateText(name, GlobalVars.NAME_REGEX, GlobalVars.NAME_ERROR_MESSAGE);
        assertTrue(result.isError());
        assertEquals("Name can only have letters, numbers, punctuations except commas, and spaces.", result.getFirstError());
    }

    @Test
    void testNullText_getErrorMessage() {
        ValidationError result = ValidationUtils.validateText(null, GlobalVars.NAME_REGEX, GlobalVars.NAME_ERROR_MESSAGE);
        assertTrue(result.isError());
        assertEquals("Cannot be null.", result.getFirstError());
    }

    @ParameterizedTest
    @CsvSource({"!@#", "Sprint @", "Sprint 1", "Sprint_1", "Sprint-1", "Sprint.1"})
    void testValidName(String name) {
        ValidationError result = ValidationUtils.validateText(name, GlobalVars.NAME_REGEX, GlobalVars.NAME_ERROR_MESSAGE);
        assertFalse(result.isError());
    }

    @ParameterizedTest
    @CsvSource({"This is valid", "''", "This! 1s. v@Lid,", "ㅍ미ㅑㅇ", "123!!?"})
    void testValidDescription(String desc) {
        ValidationError result = ValidationUtils.validateText(desc, GlobalVars.DESC_REGEX, GlobalVars.DESC_ERROR_MESSAGE);
        assertFalse(result.isError());
    }

    @ParameterizedTest
    @CsvSource({"Emojis are not valid 🤨", "These too apparently %^"})
    void testInvalidDescription_getErrorMessage(String desc) {
        ValidationError result = ValidationUtils.validateText(desc, GlobalVars.DESC_REGEX, GlobalVars.DESC_ERROR_MESSAGE);
        assertTrue(result.isError());
        assertEquals("Description can only have letters, numbers, punctuations, and spaces.", result.getFirstError());
    }
}
