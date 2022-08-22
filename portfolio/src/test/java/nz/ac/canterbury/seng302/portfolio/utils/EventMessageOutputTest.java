package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventMessageOutputTest {


    static Project parentProject = new Project("Project 2022", "", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-30"));

    @BeforeAll
    public static void setUp() {
        parentProject.setId(0);
    }

    @Test
    void whenCreateOutput_eventDetailsAdded(){
        Date eventStartDate = DateUtils.toDate("2022-02-25");
        Date eventEndDate = DateUtils.toDate("2022-02-26");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(1);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(), new ArrayList<>());
        assertEquals("Test Event", eventMessageOutput.getName() );
        assertEquals("This is an event", eventMessageOutput.getDescription());
        assertEquals(eventStartDate, eventMessageOutput.getStartDate());
        assertEquals(eventEndDate, eventMessageOutput.getEndDate());
        assertEquals(DateUtils.toDisplayDateTimeString(eventStartDate), eventMessageOutput.getStartDateString());
        assertEquals(DateUtils.toDisplayDateTimeString(eventEndDate), eventMessageOutput.getEndDateString());

    }

    @Test
    void whenCreateOutputWithNoSprints_thenOutsideSprintBox() {
        Date eventStartDate = DateUtils.toDate("2022-02-25");
        Date eventEndDate = DateUtils.toDate("2022-02-26");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(1);
        event.setParentProject(parentProject);
        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(), new ArrayList<>());

        List<String> listIds = new ArrayList<>();
        listIds.add("events-firstOutside");
        assertEquals(listIds, eventMessageOutput.getEventListIds());

        List<String> boxIds = new ArrayList<>();
        boxIds.add("1-before");
        assertEquals(boxIds, eventMessageOutput.getEventBoxIds());

        List<String> nextEventIds = new ArrayList<>();
        nextEventIds.add("-1");
        assertEquals(nextEventIds, eventMessageOutput.getNextEventIds());
    }

}
