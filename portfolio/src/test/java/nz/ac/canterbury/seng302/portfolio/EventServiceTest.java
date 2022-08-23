package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.EventRepository;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Holds unit tests for the EventService class.
 */
@SpringBootTest
class EventServiceTest {
    @Autowired
    private EventService eventService;

    @MockBean
    private EventRepository eventRepository;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event("name", "description", new Date(), new Date());
    }

    @Test
    void getEventValidId_thenReturnEvent() {
        when(eventRepository.findEventById(1))
                .thenReturn(event);

        assertThat(eventService.getEventById(1)).isEqualTo(event);
    }

    @Test
    void getEventInvalidId_thenThrowException() {
        Exception e = assertThrows(Exception.class, () -> eventService.getEventById(2));
        String expectedMessage = "Event not found.";
        assertTrue(e.getMessage().contains(expectedMessage));
    }

    @Test
    void saveEvent() {
        when(eventRepository.save(event)).thenReturn(event);
        eventService.saveEvent(event);
        verify(eventRepository, times(1)).save(event);
    }

}
