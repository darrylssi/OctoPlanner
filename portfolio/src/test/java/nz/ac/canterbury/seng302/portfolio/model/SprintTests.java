package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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

    @Autowired
    private SprintRepository sprintRepository;

    private List<Sprint> sprintList = new ArrayList<>();
    private Sprint baseSprint;

    @BeforeEach
    public void setUp() {
        int parentProjId = 5;
        sprintRepository.deleteAll();
        baseSprint = new Sprint();
        baseSprint.setSprintLabel("Sprint 1");
        baseSprint.setSprintName("Sprint 1");
        baseSprint.setSprintDescription("The first.");
        baseSprint.setParentProjectId(parentProjId);
        baseSprint.setStartDateString("05/FEB/2022");
        baseSprint.setEndDateString("24/MAR/2022");
        baseSprint.setSprintColour("#abcdef");
        sprintList.add(baseSprint);
    }

    @Test
    public void searchByName_getSprint() {
        String nameToSearch = "Sprint 1";
        sprintService.saveSprint(baseSprint);
        List<Sprint> foundSprints = sprintService.getSprintByName(nameToSearch);
        Sprint foundSprint = foundSprints.get(0);
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).isEqualTo(baseSprint.toString());
    }

    @Test
    public void searchById_getSprint() throws Exception {
        sprintService.saveSprint(baseSprint);
        Sprint foundSprint = sprintService.getSprintById(baseSprint.getId());
        // TODO fix the date functions in Project and DateUtils so I don't have to write stuff like this
        // this is needed because dates are in a different format, so they need to be reset
        // see https://stackoverflow.com/questions/24620064/comparing-of-date-objects-in-java
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).isEqualTo(baseSprint.toString());
        // toString used as they're different objects but have the same values
    }

    @Test
    public void searchByParentProjectId_getSprint() {
        sprintService.saveSprint(baseSprint);
        List<Sprint> foundSprints = sprintService.getSprintByParentProjectId(baseSprint.getParentProjectId());
        Sprint foundSprint = foundSprints.get(0);
        foundSprint.setStartDateString(Project.dateToString(foundSprint.getSprintStartDate()));
        foundSprint.setEndDateString(Project.dateToString(foundSprint.getSprintEndDate()));
        assertThat(foundSprint.toString()).isEqualTo(baseSprint.toString());
        assertThat(foundSprint.toString()).isEqualTo(baseSprint.toString());
    }

    @Test
    void saveNullSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            sprintRepository.save(new Sprint());
        });
    }

    @Test
    void saveNullNameSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            baseSprint.setSprintName(null);
            sprintRepository.save(baseSprint);
        });
    }

    @Test
    void saveEmptyNameSprint_getException() {
        assertThrows(TransactionSystemException.class, () -> {
            baseSprint.setSprintName("");
            sprintRepository.save(baseSprint);
        });
    }

    @Test
    void saveNullDescriptionSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            baseSprint.setSprintDescription(null);
            sprintRepository.save(baseSprint);
        });
    }

    @Test
    void saveNullStartDateSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            baseSprint.setStartDate(null);
            sprintRepository.save(baseSprint);
        });
    }

    @Test
    void saveNullEndDateSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            baseSprint.setEndDate(null);
            sprintRepository.save(baseSprint);
        });
    }


    @Test
    void saveEmptyColourSprint_getException() {
        assertThrows(TransactionSystemException.class, () -> {
            baseSprint.setSprintColour("");
            sprintRepository.save(baseSprint);
        });
    }


    @Test
    void saveNullColourSprint_getException() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            baseSprint.setSprintColour(null);
            sprintRepository.save(baseSprint);
        });
    }

    @Test
    void checkGivenDatesAreValidForAddSprints_getStringMessage() throws Exception {
        Date projectStartDate = utils.toDate("2022-01-02");
        Date projectEndDate = utils.toDate("2022-12-31");
        Date sprintStartDate = utils.toDate("2022-01-02");
        Date sprintEndDate = utils.toDate("2022-02-02");
        String errorMessage = baseSprint.validAddSprintDateRanges(sprintStartDate, sprintEndDate,
                projectStartDate, projectEndDate, sprintList);

        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        // Since our selected dates are 01/01/2022 -- 02/02/2022, this should return the error message as "", which tells
        // us that the dates are valid
        assertEquals("", errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsProjectStartDateForAddSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-01";
        String sprintEndDate = " 2022-02-02";
        String errorMessage = baseSprint.validAddSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate, errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForAddSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";
        String errorMessage = baseSprint.validAddSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate,
                errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForAddSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";
        String errorMessage = baseSprint.validAddSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForAddSprints_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-05";
        String sprintEndDate = "2022-03-24";
        String errorMessage = baseSprint.validAddSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & and it must not be same, it is overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

    @Test
    void checkCurrentSprintDatesOverlapsSprintListDatesForAddSprints_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-25";
        String sprintEndDate = "2022-04-02";
        String errorMessage = baseSprint.validAddSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must not overlap with other sprints & it is overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

    //
    @Test
    void checkSprintStartDateOverlapsProjectStartDateForEditSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-01";
        String sprintEndDate = "2022-02-02";
        String errorMessage = baseSprint.validEditSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate, errorMessage);
    }

    @Test
    void checkSprintEndDateOverlapsProjectEndDateForEditSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-11-02";
        String sprintEndDate = "2023-01-02";
        String errorMessage = baseSprint.validEditSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Dates must be within the project dates of " + projectStartDate + " - " + projectEndDate,
                errorMessage);
    }

    @Test
    void checkSprintStartDateOverlapsSprintEndDateForEditSprints_getStringMessage() throws Exception {
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-01-10";
        String sprintEndDate = "2022-01-08";
        String errorMessage = baseSprint.validEditSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Start date must always be before end date", errorMessage);
    }

    @Test
    void checkCurrentSprintDatesHasSameDateAsOneSprintListDatesForEditSprints_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-05";
        String sprintEndDate = "2022-03-24";
        String errorMessage = baseSprint.validEditSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

    @Test
    void checkCurrentSprintDatesOverlapsSprintListDatesForEditSprints_getStringMessage() throws Exception {
        // Sprint list has one sprint with dates 05/02/2022 -- 24/03/2022
        String projectStartDate = "2022-01-02";
        String projectEndDate = "2022-12-31";
        String sprintStartDate = "2022-02-25";
        String sprintEndDate = "2022-04-02";
        String errorMessage = baseSprint.validEditSprintDateRanges(2, utils.toDate(sprintStartDate), utils.toDate(sprintEndDate),
                utils.toDate(projectStartDate), utils.toDate(projectEndDate), sprintList);

        assertEquals("Sprint dates must not overlap with other sprints. Dates are overlapping with " +
                utils.toString(baseSprint.getSprintStartDate()) + " - " + utils.toString(baseSprint.getSprintEndDate()), errorMessage);
    }

}
