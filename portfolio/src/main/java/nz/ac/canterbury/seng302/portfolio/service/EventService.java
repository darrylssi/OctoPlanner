package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class holding methods used to access the underlying event repository methods,
 * which actually touch the database.
 *
 * Service methods need to be written manually, unlike repository methods, and should deal with errors (mainly items
 * not being found).
 */
@Service
public class EventService {
    @Autowired
    private EventRepository repository;

    /**
     * Get list of all events
     */
    public List<Event> getAllEvents() {
        return (List<Event>) repository.findAll();
    }

    /**
     * Returns a list of all events that match the given name
     * @param name the event name to search for
     * @return list of matching events, or empty list if no matches
     */
    public List<Event> getEventByName(String name) {
        return repository.findByEventName(name);
    }

    /**
     * Returns a list of all events for a given project
     * @param id the id of the project to find events from
     * @return list of matching events, or empty list if no matches
     */
    public List<Event> getEventByParentProjectId(int id) {
        return repository.findEventByParentProjectId(id);
    }

    /**
     * Get event by id
     */
    public Event getEventById(Integer id) throws ResponseStatusException {
        Event event = repository.findEventById(id);
        if (event != null) {
            return event;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
    }

    /**
     * Gets all the events that belong to a given project.
     *
     */
    public List<Event> getEventsOfProjectById(Integer id) {
        return repository.findEventByParentProjectId(id);
    }

    /**
     * Deletes an event from the repository
     * @param eventId the id of the event to be deleted
     */
    public void deleteEvent(int eventId) {
        repository.deleteById(eventId);
    }

    /**
     * Adds a new event into the database if the event with the given ID does not exist.
     * Otherwise, updates the event with the given ID.
     * @param event event to be added to the database
     */
    public Event saveEvent(Event event) {
        return repository.save(event);
    }

}
