package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

/**
 * Service class holding methods used to access the underlying sprint repository methods,
 * which actually touch the database.
 *
 * Service methods need to be written manually, unlike repository methods, and should deal with errors (mainly items
 * not being found).
 */
@Service
public class SprintService {
    @Autowired
    private SprintRepository repository;

    /**
     * Get list of all sprints
     */
    public List<Sprint> getAllSprints() {
        return (List<Sprint>) repository.findAll();
    }

    /**
     * Returns a list of all sprints that match the given name
     * @param name the sprint name to search for
     * @return list of matching sprints, or empty list if no matches
     */
    public List<Sprint> getSprintByName(String name) {
        return repository.findBySprintName(name);
    }

    /**
     * Returns a list of all sprints for a given project
     * @param id the id of the project to find sprints from
     * @return list of matching sprints, or empty list if no matches
     */
    public List<Sprint> getSprintsInProject(int id) {
        return repository.findByParentProjectId(id);
    }

    /**
     * Get sprint by id
     */
    public Sprint getSprintById(Integer id) throws ResponseStatusException {
        Sprint sprint = repository.findSprintById(id);
        if (sprint != null) {
            return sprint;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found.");
        }
    }

    /**
     * Deletes a sprint from the repository
     * @param sprintId the id of the sprint to be deleted
     */
    public void deleteSprint(int sprintId) {
        repository.deleteById(sprintId);
    }

    /**
     * Adds a new sprint into the database if the sprint with the given ID does not exist.
     * Otherwise, updates the sprint with the given ID.
     * @param sprint sprint to be added to the database
     */
    public Sprint saveSprint(Sprint sprint) {
        return repository.save(sprint);
    }
}
