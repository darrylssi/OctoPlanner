package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Integer> {
    List<Event> findByEventName(String eventName);
    Event findEventById(int id);
    List<Event> findEventByParentProjectId(int parentProjectId);
}
