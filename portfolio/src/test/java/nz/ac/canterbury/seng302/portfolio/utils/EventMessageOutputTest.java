package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Holds tests for the EventMessageOutput class.
 *
 * These tests should cover the cases listed in the manual testing spreadsheet in regard to
 * where events are placed on the project details page after they have been edited.
 */
@SpringBootTest
class EventMessageOutputTest {

    static Project parentProject = new Project("Project 2022", "", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-30"));
    static Sprint sprint1 = new Sprint(0, "name", "description", DateUtils.toDate("2022-01-02"), DateUtils.toDate("2022-02-01"), "#ff00aa");
    static Sprint sprint2 = new Sprint(0, "name", "description", DateUtils.toDate("2022-02-03"), DateUtils.toDate("2022-03-01"), "#ff00aa");

    static Event event1 = new Event("Event name", "desc", DateUtils.toDate("2022-01-05"), DateUtils.toDate("2022-02-01"));
    static Event event2 = new Event("Event name", "desc", DateUtils.toDate("2022-02-05"), DateUtils.toDate("2022-02-27"));
    static Event event3 = new Event("Event name", "desc", DateUtils.toDate("2022-03-02"), DateUtils.toDate("2022-04-01"));

    @BeforeAll
    public static void setUp() {
        parentProject.setId(0);
        sprint1.setId(1);
        sprint2.setId(2);
        event1.setId(1);
        event2.setId(2);
        event3.setId(3);
    }



    @Test
    void whenCreateOutput_eventDetailsAdded(){
        Date eventStartDate = DateUtils.toDate("2022-02-25");
        Date eventEndDate = DateUtils.toDate("2022-02-26");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(1);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(), new ArrayList<>(List.of(event)));
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
        event.setId(0);
        event.setParentProject(parentProject);
        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(), new ArrayList<>(List.of(event)));

        assertEquals(List.of(EventMessageOutput.LIST_BEFORE_ALL_ID_NAME), eventMessageOutput.getEventListIds());

        assertEquals(List.of(String.format(EventMessageOutput.EVENT_BEFORE_ALL_ID_FORMAT, event.getId())), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of("-1"), eventMessageOutput.getNextEventIds());
    }

    @Test
    void whenCreateEventOverlappingOneSprint_thenBeforeDuringAndAfter() {
        Date eventStartDate = DateUtils.toDate("2022-01-01");
        Date eventEndDate = DateUtils.toDate("2022-02-02");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                EventMessageOutput.LIST_BEFORE_ALL_ID_NAME,
                String.format(EventMessageOutput.LIST_IN_ID_FORMAT, sprint1.getId()),
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_BEFORE_ALL_ID_FORMAT, event.getId()),
                String.format(EventMessageOutput.EVENT_IN_ID_FORMAT, event.getId(), sprint1.getId()),
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of("-1", "-1", "-1"), eventMessageOutput.getNextEventIds());
    }

    @Test
    void whenCreateEventNotSpanningAllSprints_thenAppearsOnlyAfterEventStart() {
        Date eventStartDate = DateUtils.toDate("2022-02-04");
        Date eventEndDate = DateUtils.toDate("2022-03-20");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint1, sprint2)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                String.format(EventMessageOutput.LIST_IN_ID_FORMAT, sprint2.getId()),
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint2.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_IN_ID_FORMAT, event.getId(), sprint2.getId()),
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint2.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of("-1", "-1"), eventMessageOutput.getNextEventIds());
    }


    @Test
    void whenCreateEventBetweenSprints_thenAppearsOnlyBetweenSprints() {
        Date eventStartDate = DateUtils.toDateTime("2022-02-02T02:00");
        Date eventEndDate = DateUtils.toDateTime("2022-02-02T03:00");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint1, sprint2)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of("-1"), eventMessageOutput.getNextEventIds());
    }

    @Test
    void whenCreateEventBeforeOtherEvents_thenNewEventAppearsBeforeAll() {
        Date eventStartDate = DateUtils.toDate("2022-01-01");
        Date eventEndDate = DateUtils.toDate("2022-03-20");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint2)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                EventMessageOutput.LIST_BEFORE_ALL_ID_NAME,
                String.format(EventMessageOutput.LIST_IN_ID_FORMAT, sprint2.getId()),
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint2.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_BEFORE_ALL_ID_FORMAT, event.getId()),
                String.format(EventMessageOutput.EVENT_IN_ID_FORMAT, event.getId(), sprint2.getId()),
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint2.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.NEXT_EVENT_ID_FORMAT, event1.getId()),
                String.format(EventMessageOutput.NEXT_EVENT_ID_FORMAT, event2.getId()),
                String.format(EventMessageOutput.NEXT_EVENT_ID_FORMAT, event3.getId())
        ), eventMessageOutput.getNextEventIds());

    }

    @Test
    void whenCreateEventBetweenOtherEvents_thenNewEventAppearsBetween() {
        Date eventStartDate = DateUtils.toDateTime("2022-03-01T02:00");
        Date eventEndDate = DateUtils.toDateTime("2022-03-01T03:00");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.NEXT_EVENT_ID_FORMAT, event3.getId())
        ), eventMessageOutput.getNextEventIds());

    }

    @Test
    void whenCreateEventAfterAllEvents_thenNewEventAppearsAfterAll() {
        Date eventStartDate = DateUtils.toDate("2022-05-01");
        Date eventEndDate = DateUtils.toDate("2022-05-05");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        EventMessageOutput eventMessageOutput = new EventMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                String.format(EventMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), eventMessageOutput.getEventListIds());

        assertEquals(List.of(
                String.format(EventMessageOutput.EVENT_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), eventMessageOutput.getEventBoxIds());

        assertEquals(List.of(
                "-1"
        ), eventMessageOutput.getNextEventIds());

    }

}
