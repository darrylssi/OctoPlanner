package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.DeadlineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DeadlineService {

    @Autowired
    private DeadlineRepository repository;

    /**
     * Get list of all deadlines
     */
    public List<Deadline> getAllDeadlines() {
        return (List<Deadline>) repository.findAll();
    }

    /**
     * Returns a list of all deadlines that match the given name
     * @param name the deadline name to search for
     * @return list of matching deadlines, or empty list if no matches
     */
    public List<Deadline> getDeadlineByName(String name) {
        return repository.findByDeadlineName(name);
    }

    /**
     * Returns a list of all deadlines for a given project
     * @param id the id of the project to find deadlines from
     * @return list of matching deadlines, or empty list if no matches
     */
    public List<Deadline> getDeadlineByParentProjectId(int id) {
        return repository.findDeadlineByParentProjectId(id);
    }

    /**
     * Get deadline by id
     */
    public Deadline getDeadlineById(Integer id) throws ResponseStatusException {
        Deadline deadline = repository.findDeadlineById(id);
        if (deadline != null) {
            return deadline;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadline not found.");
        }
    }

    /**
     * Gets all the deadlines that belong to a given project.
     */
    public List<Deadline> getDeadlinesInProject(Integer id) {
        return repository.findDeadlineByParentProjectId(id);
    }

    /**
     * Deletes a deadline from the repository
     * @param deadlineId the id of the deadline to be deleted
     */
    public void deleteDeadline(int deadlineId) {
        repository.deleteById(deadlineId);
    }

    /**
     * Adds a new deadline into the database if the deadline with the given ID does not exist.
     * Otherwise, updates the deadline with the given ID.
     * @param deadline deadline to be added to the database
     */
    public void saveDeadline(Deadline deadline) {
        repository.save(deadline);
    }
}
