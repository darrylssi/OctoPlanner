package nz.ac.canterbury.seng302.portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.EventRepository;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EventServiceTest {

    private static final int ID = 1;

    @Autowired
    private EventService eventService;
    
    @MockBean
    private EventRepository eventRepository;
    
    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(ID);
        event.setParentProjectId(1);
        event.setEventName("Test Event");
        event.setEventDescription("Testing patience, once course at a time");
        event.setStartDate(new Date());
        event.setEndDate(new Date());
    }

    @Test
    void getSprintValidId_thenReturnSprint() throws Exception {
        when(eventRepository.findEventById(ID))
                .thenReturn(event);

        assertThat(eventService.getEventById(ID)).isEqualTo(event);
    }

    @Test
    void getSprintInvalidId_thenThrowException() {
        // TODO: We've really gotta stop throwing base Exceptions. Maybe try optionals or something
        when(eventRepository.findEventById(ID+1)).thenReturn(null);
        Exception e = assertThrows(Exception.class, () -> {
            eventService.getEventById(ID + 1);
        });
        String expectedMessage = "Event not found!";
        assertTrue(e.getMessage().contains(expectedMessage));
    }

    @Test
    void saveSprint() {
        when(eventRepository.save(event)).thenReturn(event);
        eventService.saveEvent(event);
        verify(eventRepository, times(1)).save(event);
    }

}
