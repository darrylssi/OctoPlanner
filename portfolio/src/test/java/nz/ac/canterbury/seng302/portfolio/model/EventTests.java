package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

/**
 * Holds unit tests for the Event class.
 */
@SpringBootTest
class EventTests {
    @Autowired
    private DateUtils utils;

    private static List<Sprint> sprintList = new ArrayList<Sprint>();

    private String defaultEventColour = "#ff3823";

    /* Some helper functions for creation of events */
    private Event createEventOutsideSprints() throws Exception {
        Date eventStartDate = utils.toDate("2022-02-25");
        Date eventEndDate = utils.toDate("2022-02-26");
        int parentProjId = 5;
        return new Event(parentProjId, "Outside sprints", "", eventStartDate, eventEndDate);
    }

    private Event createEventInsideSprint() throws Exception {
        Date eventStartDate = utils.toDate("2022-02-06");
        Date eventEndDate = utils.toDate("2022-02-06");
        int parentProjId = 5;
        return new Event(parentProjId, "Inside sprints", "", eventStartDate, eventEndDate);
    }

    private Event createEventSpanningSprints() throws Exception {
        Date eventStartDate = utils.toDate("2022-02-06");
        Date eventEndDate = utils.toDate("2022-03-06");
        int parentProjId = 5;
        return new Event(parentProjId, "Spanning sprints", "", eventStartDate, eventEndDate);
    }


    @BeforeAll
    public static void createSprintList() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        int parentProjId = 5;
        List<String> months = Arrays.asList("FEB", "MAR", "APR", "MAY", "JUN");
        for(int i = 1; i < 6; i++) {
            Sprint tempSprint = new Sprint();
            tempSprint.setSprintLabel("Sprint " + i);
            tempSprint.setSprintName("Sprint " + i);
            tempSprint.setParentProjectId(parentProjId);
            tempSprint.setStartDateString("05/" + months.get(i-1) + "/2022");
            tempSprint.setEndDateString("24/" + months.get(i-1) + "/2022");
            tempSprint.setSprintColour("#00000" + i);
            sprintList.add(tempSprint);
        }
    }

    @Test
    void checkEventWithoutSprintHasDefaultColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Event event = createEventOutsideSprints();

        assertEquals(defaultEventColour, event.determineColour(sprintList));
    }

    @Test
    void checkEventInSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }

    @Test
    void checkEventEndingOutOfSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();

        event.setEndDate(utils.toDate("2022-02-26"));
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }

    @Test
    void checkEventStartOfSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();
        event.setStartDate(utils.toDate("2022-02-05"));
        event.setEndDate(utils.toDate("2022-02-05"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }

    @Test
    void checkEventFinishingInSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(1);
        Event event = createEventOutsideSprints();
        event.setEndDate(utils.toDate("2022-03-06"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }

    @Test
    void checkEventEndOfSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();
        event.setStartDate(utils.toDate("2022-02-24"));
        event.setEndDate(utils.toDate("2022-02-24"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }

    @Test
    void checkEventSpanningSprintsInheritsFirstColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventSpanningSprints();

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList));
    }
}
