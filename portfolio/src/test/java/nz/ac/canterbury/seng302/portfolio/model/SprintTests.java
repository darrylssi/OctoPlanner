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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;


/**
 * Holds unit tests for the Sprint class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
public class SprintTests {
    @Autowired
    private SprintService sprintService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DateUtils utils;

    @MockBean
    private SprintRepository sprintRepository;

    private final List<Sprint> sprintList = new ArrayList<>();
    private Sprint baseSprint;
    private Project baseProject;
    private final int SPRINT_ID = 2;

    @BeforeEach
    public void setUp() {
        baseSprint = new Sprint();
        baseSprint.setSprintName("Sprint 1");
        baseSprint.setSprintDescription("The first.");
        baseSprint.setParentProjectId(5);
        baseSprint.setStartDateString("05/FEB/2022");
        baseSprint.setEndDateString("24/MAR/2022");
        baseSprint.setSprintColour("#abcdef");
        sprintList.add(baseSprint);

        baseProject = new Project();
        baseProject.setId(5);
        baseProject.setProjectName("Project 1");
        baseProject.setProjectDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");
        baseProject.setId(1);
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
    void saveNullDescriptionSprint_getException() {
        try {
            baseSprint.setSprintDescription(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
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
    void saveEmptyColourSprint_getException() {
        try {
            baseSprint.setSprintColour("");
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }


    @Test
    void saveNullColourSprint_getException() {
        try {
            baseSprint.setSprintColour(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void checkGivenDatesAreValidForAddSprints_getStringMessage() throws Exception {
        Date sprintStartDate = utils.toDate("2022-01-02");
        Date sprintEndDate = utils.toDate("2022-02-02");

        String errorMessage = validationService.validateSprintDates(SPRINT_ID, sprintStartDate, sprintEndDate, baseProject);

        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        // Since our selected dates are 01/01/2022 -- 02/02/2022, this should return the error message as "", which tells
        // us that the dates are valid
        assertEquals("", errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsProjectStartDateForAddSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2021-12-31";
        String sprintEndDate = " 2022-02-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForAddSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForAddSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForAddSprints_getStringMessage() throws Exception {
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
    void checkCurrentSprintDatesOverlapsSprintListDatesForAddSprints_getStringMessage() throws Exception {
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
    void checkSprintStartDateOverlapsProjectStartDateForEditSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2021-12-31";
        String sprintEndDate = "2022-02-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForEditSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Sprint dates must be within project date range: " +
                        baseProject.getStartDateString() + " - " + baseProject.getEndDateString(), errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForEditSprints_getStringMessage() throws Exception {
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";

        Date start = utils.toDate(sprintStartDate);
        Date end = utils.toDate(sprintEndDate);
        String errorMessage = validationService.validateSprintDates(SPRINT_ID, start, end, baseProject);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForEditSprints_getStringMessage() throws Exception {
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
    void checkCurrentSprintDatesOverlapsSprintListDatesForEditSprints_getStringMessage() throws Exception {
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
