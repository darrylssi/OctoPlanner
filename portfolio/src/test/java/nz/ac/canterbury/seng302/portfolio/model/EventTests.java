package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Holds unit tests for the Event class.
 */
@SpringBootTest
class EventTests {
    private static List<Sprint> sprintList = new ArrayList<>();
    private static final List<Sprint> sprintList = new ArrayList<>();

    private final String defaultEventColour = "#ff3823";

    /* Some helper functions for creation of events */
    private Event createEventOutsideSprints() {
        Date eventStartDate = DateUtils.toDate("2022-02-25");
        Date eventEndDate = DateUtils.toDate("2022-02-26");
        return new Event("Outside sprints", "", eventStartDate, eventEndDate);
    }

    private Event createEventInsideSprint() {
        Date eventStartDate = DateUtils.toDate("2022-02-06");
        Date eventEndDate = DateUtils.toDate("2022-02-06");
        return new Event("Inside sprints", "", eventStartDate, eventEndDate);
    }

    private Event createEventSpanningSprints() {
        Date eventStartDate = DateUtils.toDate("2022-02-06");
        Date eventEndDate = DateUtils.toDate("2022-03-06");
        return new Event("Spanning sprints", "", eventStartDate, eventEndDate);
    }


    @BeforeAll
    public static void createSprintList() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        int parentProjId = 5;
        for(int i = 1; i < 6; i++) {
            Sprint tempSprint = new Sprint();
            tempSprint.setSprintLabel("Sprint " + i);
            tempSprint.setSprintName("Sprint " + i);
            tempSprint.setParentProjectId(parentProjId);
            tempSprint.setStartDateString("2022-0" + (i+1) + "-05");
            tempSprint.setEndDateString("2022-0" + (i+1) + "-24");
            tempSprint.setSprintColour("#00000" + i);
            sprintList.add(tempSprint);
        }
    }

    @Test
    void checkEventWithoutSprintHasDefaultColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Event event = createEventOutsideSprints();

        assertEquals(defaultEventColour, event.determineColour(sprintList, false));
        assertEquals(defaultEventColour, event.determineColour(sprintList, true));
    }

    @Test
    void checkEventInSprintInheritsColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, false));
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, true));
    }

    @Test
    void checkEventStartingInSprintInheritsStartColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();

        event.setEndDate(DateUtils.toDate("2022-02-26"));
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, false));
        assertEquals(defaultEventColour, event.determineColour(sprintList, true));
    }

    @Test
    void checkEventStartOfSprintInheritsColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();
        event.setStartDate(DateUtils.toDate("2022-02-05"));
        event.setEndDate(DateUtils.toDate("2022-02-05"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, false));
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, true));
    }

    @Test
    void checkEventFinishingInSprintHasSprintEndColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(1);
        Event event = createEventOutsideSprints();
        event.setEndDate(DateUtils.toDate("2022-03-06"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, true));
        assertEquals(defaultEventColour, event.determineColour(sprintList, false));
    }

    @Test
    void checkEventEndOfSprintInheritsColour() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Event event = createEventInsideSprint();
        event.setStartDate(DateUtils.toDate("2022-02-24"));
        event.setEndDate(DateUtils.toDate("2022-02-24"));

        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, false));
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, true));
    }

    @Test
    void checkEventSpanningSprintsInheritsBothColours() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Event event = createEventSpanningSprints();

        Sprint sprint = sprintList.get(0);
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, false));

        sprint = sprintList.get(1);
        assertEquals(sprint.getSprintColour(), event.determineColour(sprintList, true));
    }
}
