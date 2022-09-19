package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Holds tests for the SchedulableMessageOutput class.
 *
 * These tests should cover the cases listed in the manual testing spreadsheet in regard to
 * where events are placed on the project details page after they have been edited.
 */
@SpringBootTest
class SchedulableMessageOutputTest {

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
    void whenCreateEventOutputWithNoSprints_thenOutsideSprintBox() {
        Date eventStartDate = DateUtils.toDate("2022-02-25");
        Date eventEndDate = DateUtils.toDate("2022-02-26");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);
        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(), new ArrayList<>(List.of(event)));

        assertEquals(List.of(SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, event.getId())), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateDeadlineOutputWithNoSprints_thenOutsideSprintBox() {
        Date deadlineDate = DateUtils.toDate("2022-02-25");
        Deadline deadline = new Deadline("Test Deadline",  "This is a deadline", deadlineDate);
        deadline.setId(0);
        deadline.setParentProject(parentProject);
        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(deadline, new ArrayList<>(), new ArrayList<>(List.of(deadline)));

        assertEquals(List.of(SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, deadline.getId())), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateMilestoneOutputWithNoSprints_thenOutsideSprintBox() {
        Date milestoneDate = DateUtils.toDate("2022-02-25");
        Milestone milestone = new Milestone("Test Milestone",  "This is a milestone", milestoneDate);
        milestone.setId(0);
        milestone.setParentProject(parentProject);
        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(milestone, new ArrayList<>(), new ArrayList<>(List.of(milestone)));

        assertEquals(List.of(SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, milestone.getId())), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateDeadlineOutputWithSprints_thenInsideSprintBox() {
        Date deadlineDate = DateUtils.toDate("2022-01-25");
        Deadline deadline = new Deadline("Test Deadline",  "This is a deadline", deadlineDate);
        deadline.setId(0);
        deadline.setParentProject(parentProject);
        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(deadline, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(deadline)));

        assertEquals(List.of(String.format(SchedulableMessageOutput.LIST_IN_ID_FORMAT, sprint1.getId())), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(String.format(SchedulableMessageOutput.SCHEDULABLE_IN_ID_FORMAT, deadline.getId(), sprint1.getId())), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateMilestoneOutputWithSprints_thenInsideSprintBox() {
        Date milestoneDate = DateUtils.toDate("2022-01-25");
        Milestone milestone = new Milestone("Test Milestone",  "This is a milestone", milestoneDate);
        milestone.setId(0);
        milestone.setParentProject(parentProject);
        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(milestone, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(milestone)));

        assertEquals(List.of(String.format(SchedulableMessageOutput.LIST_IN_ID_FORMAT, sprint1.getId())), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(String.format(SchedulableMessageOutput.SCHEDULABLE_IN_ID_FORMAT, milestone.getId(), sprint1.getId())), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateEventOverlappingOneSprint_thenBeforeDuringAndAfter() {
        Date eventStartDate = DateUtils.toDate("2022-01-01");
        Date eventEndDate = DateUtils.toDate("2022-02-02");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME,
                String.format(SchedulableMessageOutput.LIST_IN_ID_FORMAT, sprint1.getId()),
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, event.getId()),
                String.format(SchedulableMessageOutput.SCHEDULABLE_IN_ID_FORMAT, event.getId(), sprint1.getId()),
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1", "-1", "-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateEventNotSpanningAllSprints_thenAppearsOnlyAfterEventStart() {
        Date eventStartDate = DateUtils.toDate("2022-02-04");
        Date eventEndDate = DateUtils.toDate("2022-03-20");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint1, sprint2)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_IN_ID_FORMAT, sprint2.getId()),
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint2.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_IN_ID_FORMAT, event.getId(), sprint2.getId()),
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint2.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1", "-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }


    @Test
    void whenCreateEventBetweenSprints_thenAppearsOnlyBetweenSprints() {
        Date eventStartDate = DateUtils.toDateTime("2022-02-02 02:00");
        Date eventEndDate = DateUtils.toDateTime("2022-02-02 03:00");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint1, sprint2)), new ArrayList<>(List.of(event)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of("-1"), SchedulableMessageOutput.getNextSchedulableIds());
    }

    @Test
    void whenCreateEventBeforeOtherEvents_thenNewEventAppearsBeforeAll() {
        Date eventStartDate = DateUtils.toDate("2022-01-01");
        Date eventEndDate = DateUtils.toDate("2022-03-20");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint2)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME,
                String.format(SchedulableMessageOutput.LIST_IN_ID_FORMAT, sprint2.getId()),
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint2.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, event.getId()),
                String.format(SchedulableMessageOutput.SCHEDULABLE_IN_ID_FORMAT, event.getId(), sprint2.getId()),
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint2.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event1.getType(), event1.getId()),
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event2.getType(), event2.getId()),
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event3.getType(), event3.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateEventBetweenOtherEvents_thenNewEventAppearsBetween() {
        Date eventStartDate = DateUtils.toDateTime("2022-03-01 02:00");
        Date eventEndDate = DateUtils.toDateTime("2022-03-01 03:00");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event3.getType(), event3.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateEventAfterAllEvents_thenNewEventAppearsAfterAll() {
        Date eventStartDate = DateUtils.toDate("2022-05-01");
        Date eventEndDate = DateUtils.toDate("2022-05-05");
        Event event = new Event("Test Event",  "This is an event", eventStartDate, eventEndDate);
        event.setId(0);
        event.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(event, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(event, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, event.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                "-1"
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateDeadlineBeforeEvents_thenNewDeadlineAppearsBeforeAll() {
        Date deadlineDate = DateUtils.toDate("2022-01-01");
        Deadline deadline = new Deadline("Test Deadline",  "This is a deadline", deadlineDate);
        deadline.setId(0);
        deadline.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(deadline, new ArrayList<>(), new ArrayList<>(List.of(deadline, event1, event2, event3)));

        assertEquals(List.of(
                SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, deadline.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event1.getType(), event1.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateDeadlineBetweenEvents_thenNewDeadlineAppearsBetween() {
        Date deadlineDate = DateUtils.toDateTime("2022-03-01 02:00");
        Deadline deadline = new Deadline("Test Deadline",  "This is a deadline", deadlineDate);
        deadline.setId(0);
        deadline.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(deadline, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(deadline, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, deadline.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event3.getType(), event3.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateDeadlineAfterAllEvents_thenNewDeadlineAppearsAfterAll() {
        Date deadlineDate = DateUtils.toDate("2022-05-01");
        Deadline deadline = new Deadline("Test Deadline",  "This is a deadline", deadlineDate);
        deadline.setId(0);
        deadline.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(deadline, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(deadline, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, deadline.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                "-1"
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateMilestoneBeforeEvents_thenNewMilestoneAppearsBeforeAll() {
        Date milestoneDate = DateUtils.toDate("2022-01-01");
        Milestone milestone = new Milestone("Test Milestone",  "This is a milestone", milestoneDate);
        milestone.setId(0);
        milestone.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(milestone, new ArrayList<>(), new ArrayList<>(List.of(milestone, event1, event2, event3)));

        assertEquals(List.of(
                SchedulableMessageOutput.LIST_BEFORE_ALL_ID_NAME
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_BEFORE_ALL_ID_FORMAT, milestone.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event1.getType(), event1.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateMilestoneBetweenOtherEvents_thenNewMilestoneAppearsBetween() {
        Date milestoneDate = DateUtils.toDateTime("2022-03-01 02:00");
        Milestone milestone = new Milestone("Test Milestone",  "This is a milestone", milestoneDate);
        milestone.setId(0);
        milestone.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(milestone, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(milestone, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, milestone.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.NEXT_SCHEDULABLE_ID_FORMAT, event3.getType(), event3.getId())
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

    @Test
    void whenCreateMilestoneAfterAllEvents_thenNewMilestoneAppearsAfterAll() {
        Date milestoneDate = DateUtils.toDate("2022-05-01");
        Milestone milestone = new Milestone("Test Event",  "This is an event", milestoneDate);
        milestone.setId(0);
        milestone.setParentProject(parentProject);

        SchedulableMessageOutput SchedulableMessageOutput = new SchedulableMessageOutput(milestone, new ArrayList<>(List.of(sprint1)), new ArrayList<>(List.of(milestone, event1, event2, event3)));

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.LIST_AFTER_ID_FORMAT, sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableListIds());

        assertEquals(List.of(
                String.format(SchedulableMessageOutput.SCHEDULABLE_AFTER_ID_FORMAT, milestone.getId(), sprint1.getId())
        ), SchedulableMessageOutput.getSchedulableBoxIds());

        assertEquals(List.of(
                "-1"
        ), SchedulableMessageOutput.getNextSchedulableIds());

    }

}
