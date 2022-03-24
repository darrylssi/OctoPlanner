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
     * Gets a List of all Sprint objects currently in the database.
     */
    public List<Sprint> getAllSprints() {
        return (List<Sprint>) repository.findAll();
    }

    /**
     * Gets a list of all sprints for a specified project id. Returns a List of Sprint objects.
     * @return list a List object of Sprint objects
     */
    public List<Sprint> getSprintByParentProjectId(int parentProjectId) {
        return repository.findByParentProjectId(parentProjectId);
    }

    /**
     * Returns the one sprint object matching that id.
     * @return the sprint with the specified id
     * @throws Exception if the sprint is not found, an exception is thrown.
     */
    public Sprint getSprintById(Integer id) throws Exception {

       Sprint sprint = repository.findSprintById(id);
        if(sprint!=null) {
            return sprint;
        }
        else
        {
            throw new Exception("Sprint not found");
        }
    }

    /**
     * Returns all sprints that match the described name in a List object - not a sprint object!
     * @return list a list type of sprint objects.
     */
    public List<Sprint> getSprintBySprintName(String name) {
        return repository.findBySprintName(name);
    }
}
