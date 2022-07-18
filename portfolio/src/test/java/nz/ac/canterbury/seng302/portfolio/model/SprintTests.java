package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for the Sprint class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
class SprintTests {
    @Mock
    private SprintService sprintService;

    @Mock
    private SprintRepository sprintRepository;

    private final List<Sprint> sprintList = new ArrayList<>();
    private Sprint baseSprint;

    private Project baseProject;
    private final int SPRINT_ID = 2;

    @BeforeEach
    public void setUp() {
        int parentProjId = 5;

        baseSprint = new Sprint();
        baseSprint.setSprintLabel("Sprint 1");
        baseSprint.setSprintName("Sprint 1");
        baseSprint.setSprintDescription("The first.");
        baseSprint.setParentProjectId(parentProjId);
        baseSprint.setStartDateString("2022-02-05");
        baseSprint.setEndDateString("2022-03-24");
        baseSprint.setSprintColour("#abcdef");
        sprintList.add(baseSprint);

        baseProject = new Project();
        baseProject.setId(parentProjId);
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("2022-01-01");
        baseProject.setEndDateString("2022-10-01");
        baseProject.setId(1);
    }

    @Test
    void searchByName_getSprint() {
        String nameToSearch = "Sprint 1";
        when(sprintService.getSprintByName(nameToSearch)).thenReturn(sprintList);
        List<Sprint> foundSprints = sprintService.getSprintByName(nameToSearch);
        Sprint foundSprint = foundSprints.get(0);
        assertEquals(baseSprint, foundSprint);
    }

    @Test
    void searchById_getSprint() throws Exception {
        when(sprintService.getSprintById(baseSprint.getId())).thenReturn(baseSprint);
        Sprint foundSprint = sprintService.getSprintById(baseSprint.getId());
        assertEquals(baseSprint, foundSprint);
    }

    @Test
    void searchByParentProjectId_getSprint() {
        when(sprintService.getSprintByParentProjectId(baseSprint.getParentProjectId())).thenReturn(sprintList);
        List<Sprint> foundSprints = sprintService.getSprintByParentProjectId(baseSprint.getParentProjectId());
        Sprint foundSprint = foundSprints.get(0);
        assertEquals(baseSprint, foundSprint);
    }

    @Test
    void saveNullSprint_getException() {
        Sprint nullSprint = new Sprint();
        when(sprintRepository.save(nullSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(nullSprint));
    }

    @Test
    void saveNullNameSprint_getException() {
        baseSprint.setSprintName(null);
        when(sprintRepository.save(baseSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveEmptyNameSprint_getException() {
        baseSprint.setSprintName("");
        when(sprintRepository.save(baseSprint)).thenThrow(TransactionSystemException.class);
        assertThrows(TransactionSystemException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullDescriptionSprint_getException() {
        baseSprint.setSprintDescription(null);
        when(sprintRepository.save(baseSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullStartDateSprint_getException() {
        baseSprint.setStartDate(null);
        when(sprintRepository.save(baseSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullEndDateSprint_getException() {
        baseSprint.setEndDate(null);
        when(sprintRepository.save(baseSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }


    @Test
    void saveEmptyColourSprint_getException() {
        baseSprint.setSprintColour("");
        when(sprintRepository.save(baseSprint)).thenThrow(TransactionSystemException.class);
        assertThrows(TransactionSystemException.class, () -> sprintRepository.save(baseSprint));
    }


    @Test
    void saveNullColourSprint_getException() {
        baseSprint.setSprintColour(null);
        when(sprintRepository.save(baseSprint)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void checkGivenDatesAreValid_getStringMessage() {
        Date start = DateUtils.toDate("2022-01-02");
        Date end = DateUtils.toDate("2022-02-02");

        assert start != null;
        ValidationError error = ValidationUtils.validateSprintDates(SPRINT_ID, start,
                end, baseProject, sprintList);
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        // Since our selected dates are 01/01/2022 -- 02/02/2022, this should return the error message as "", which tells
        // us that the dates are valid
        assertFalse(error.isError());
    }

    @ParameterizedTest
    @CsvSource({"2021-12-31,2022-02-02",
            "2022-11-02,2023-01-02"})
    void checkSprintDatesWithinProjectDates_getErrorMessage(String startString, String endString){
        // Project dates are 2022-01-01 -- 2022-10-01
        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        ValidationError error = ValidationUtils.validateSprintDates(SPRINT_ID, start, end,
                baseProject, sprintList);
        String actual = error.getFirstError();

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), actual);
    }

    @ParameterizedTest
    @CsvSource({"2022-02-04,2022-04-02",
            "2022-02-05,2022-04-02",
            "2022-02-06,2022-04-02",
            "2022-01-05,2022-02-05",
            "2022-01-05,2022-02-06",
            "2022-01-05,2022-03-24"})
    void checkSprintDatesOverlap_getErrorMessage(String startString, String endString){
        // Sprint list has one sprint with dates 2022-02-05 -- 2022-03-24
        when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = DateUtils.toDate(startString);
        Date end = DateUtils.toDate(endString);
        assert start != null;
        ValidationError error = ValidationUtils.validateSprintDates(SPRINT_ID, start, end, baseProject, sprintList);
        String actual = error.getFirstError();

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with "
                + baseSprint.getStartDateString() + " - " + baseSprint.getEndDateString(), actual);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDate_getStringMessage() {
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";

        Date start = DateUtils.toDate(sprintStartDate);
        Date end = DateUtils.toDate(sprintEndDate);
        assert start != null;
        ValidationError error = ValidationUtils.validateSprintDates(SPRINT_ID, start, end,
                baseProject, sprintList);
        String actual = error.getFirstError();

        assertEquals("Start date must always be before end date", actual);
    }

}
