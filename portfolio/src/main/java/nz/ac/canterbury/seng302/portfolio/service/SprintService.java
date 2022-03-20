package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
     * Get a list of all sprints for a certain project id
     */
    public List<Sprint> getSprintByParentProjectId(int parentProjectId) {
        List<Sprint> list = repository.findByParentProjectId(parentProjectId);
        return list;
    }

    /**
     * Get sprint by id
     */
    public Sprint getSprintById(Integer id) throws Exception {

        Optional<Sprint> sprint = repository.findById(id);
        if(sprint!=null) {
            return sprint.get();
        }
        else
        {
            throw new Exception("Sprint not found");
        }
    }

    /**
     * Get sprint by name
     */
    public List<Sprint> getSprintBySprintName(String name) {
        List<Sprint> list = repository.findBySprintName(name);
        return list;
    }
}
