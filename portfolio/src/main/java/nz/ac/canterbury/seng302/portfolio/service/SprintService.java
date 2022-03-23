package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

@Service
public class SprintService {
    @Autowired
    private SprintRepository repository;

    /**
     * Get list of all sprints
     */
    public List<Sprint> getAllSprints() {
        List<Sprint> list = (List<Sprint>) repository.findAll();
        return list;
    }

    /**
     * Get sprint by id
     */
    public Sprint getSprintById(Integer id) {
        return repository.findSprintById(id);
    }

    /**
     * Adds a new sprint into the database if the sprint with the given ID does not exist.
     * Otherwise, updates the sprint with the given ID.
     * @param sprint sprint to be added to the database
     */
    public void saveSprint(Sprint sprint) {
        repository.save(sprint);
    }

    /**
     * Savdes a sprint to the repository
     * @param sprint The sprint object to save to the repository
     */
    public void saveOrUpdateSprint(Sprint sprint) {
        repository.save(sprint);
    }

    /**
     * Deletes a specific user from the repository
     * @param id The id of the sprint to delete
     */
    public void delete(int id) {
        repository.deleteById(id);
    }

}
