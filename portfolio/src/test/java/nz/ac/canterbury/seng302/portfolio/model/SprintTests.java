package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.ValidationService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Holds unit tests for the Sprint class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
class SprintTests {
    @Autowired
    private SprintService sprintService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DateUtils utils;

    @Autowired
    private SprintRepository sprintRepository;

    private List<Sprint> sprintList;
    private Sprint baseSprint;

    private Project baseProject;
    private final int SPRINT_ID = 2;
    private final int parentProjId = 5;

    @BeforeEach
    public void setUp() {
        sprintList = new ArrayList<>();

        sprintRepository.deleteAll();
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
        sprintService.saveSprint(baseSprint);
        List<Sprint> foundSprints = sprintService.getSprintByName(nameToSearch);
        Sprint foundSprint = foundSprints.get(0);
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).hasToString(baseSprint.toString());
    }

    @Test
    void searchById_getSprint() throws Exception {
        sprintService.saveSprint(baseSprint);
        Sprint foundSprint = sprintService.getSprintById(baseSprint.getId());
        // TODO fix the date functions in Project and DateUtils so I don't have to write stuff like this
        // this is needed because dates are in a different format, so they need to be reset
        // see https://stackoverflow.com/questions/24620064/comparing-of-date-objects-in-java
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).hasToString(baseSprint.toString());
        // toString used as they're different objects but have the same values
    }

    @Test
    void searchByParentProjectId_getSprint() {
        sprintService.saveSprint(baseSprint);
        List<Sprint> foundSprints = sprintService.getSprintByParentProjectId(baseSprint.getParentProjectId());
        Sprint foundSprint = foundSprints.get(0);
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).hasToString(baseSprint.toString());
        assertThat(foundSprint.toString()).hasToString(baseSprint.toString());
    }

    @Test
    void saveNullSprint_getException() {
        Sprint nullSprint = new Sprint();
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(nullSprint));
    }

    @Test
    void saveNullNameSprint_getException() {
        baseSprint.setSprintName(null);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveEmptyNameSprint_getException() {
        baseSprint.setSprintName("");
        assertThrows(TransactionSystemException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullDescriptionSprint_getException() {
        baseSprint.setSprintDescription(null);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullStartDateSprint_getException() {
        baseSprint.setStartDate(null);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void saveNullEndDateSprint_getException() {
        baseSprint.setEndDate(null);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }


    @Test
    void saveEmptyColourSprint_getException() {
        baseSprint.setSprintColour("");
        assertThrows(TransactionSystemException.class, () -> sprintRepository.save(baseSprint));
    }


    @Test
    void saveNullColourSprint_getException() {
        baseSprint.setSprintColour(null);
        assertThrows(DataIntegrityViolationException.class, () -> sprintRepository.save(baseSprint));
    }

    @Test
    void checkGivenDatesAreValidForAddSprints_getStringMessage() {
        Date sprintStartDate = utils.toDate("2022-01-02");
        Date sprintEndDate = utils.toDate("2022-02-02");

        String errorMessage = validationService.validateSprintDates(SPRINT_ID, sprintStartDate, sprintEndDate, baseProject);

        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        // Since our selected dates are 01/01/2022 -- 02/02/2022, this should return the error message as "", which tells
        // us that the dates are valid
        assertEquals("", errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsProjectStartDateForAddSprints_getStringMessage() {
        String sprintStartDate = "2021-12-31";
        String sprintEndDate = " 2022-02-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForAddSprints_getStringMessage() {
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForAddSprints_getStringMessage() {
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForAddSprints_getStringMessage() {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String sprintStartDate = "2022-02-05";
        String sprintEndDate = "2022-03-24";

        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with "
                + baseSprint.getStartDateString() + " - " + baseSprint.getEndDateString(), errorMessage);
    }

    @Test
    void checkCurrentSprintDatesOverlapsSprintListDatesForAddSprints_getStringMessage() {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String sprintStartDate = "2022-02-25";
        String sprintEndDate = "2022-04-02";

        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with "
                + baseSprint.getStartDateString() + " - " + baseSprint.getEndDateString(), errorMessage);
    }

    //
    @Test
    void checkSprintStartDateOverlapsProjectStartDateForEditSprints_getStringMessage() {
        String sprintStartDate = "2021-12-31";
        String sprintEndDate = "2022-02-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForEditSprints_getStringMessage() {
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                        baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForEditSprints_getStringMessage() {
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForEditSprints_getStringMessage() {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String sprintStartDate = "2022-02-05";
        String sprintEndDate = "2022-03-24";

        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with " +
                baseSprint.getStartDateString() + " - " + baseSprint.getEndDateString(), errorMessage);
    }

    @Test
    void checkCurrentSprintDatesOverlapsSprintListDatesForEditSprints_getStringMessage() {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String sprintStartDate = "2022-02-25";
        String sprintEndDate = "2022-04-02";

        Mockito.when(sprintService.getAllSprints()).thenReturn(sprintList);

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with " +
                baseSprint.getStartDateString() + " - " + baseSprint.getEndDateString(), errorMessage);
    }

}
