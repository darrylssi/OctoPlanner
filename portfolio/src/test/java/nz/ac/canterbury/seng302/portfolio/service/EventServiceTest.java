package nz.ac.canterbury.seng302.portfolio.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.EventRepository;

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
    void getEventValidId_thenReturnEvent() throws Exception {
        when(eventRepository.findEventById(ID))
                .thenReturn(event);

        Assertions.assertThat(eventService.getEventById(ID)).isEqualTo(event);
    }

    @Test
    void getEventInvalidId_thenThrowException() {
        // TODO: We've really gotta stop throwing base Exceptions. Maybe try optionals or something
        when(eventRepository.findEventById(ID+1)).thenReturn(null);
        Exception e = assertThrows(Exception.class, () -> {
            eventService.getEventById(ID + 1);
        });
        String expectedMessage = "Event not found.";
        assertThat(e.getMessage(), containsString(expectedMessage));
    }

    @Test
    void saveEvent() {
        when(eventRepository.save(event)).thenReturn(event);
        eventService.saveEvent(event);
        verify(eventRepository, times(1)).save(event);
    }

}
