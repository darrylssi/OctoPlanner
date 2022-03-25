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
     * Get sprint by id
     */
    public Sprint getSprintById(Integer id) throws Exception {

        Optional<Sprint> sprint = repository.findById(id);
        if(sprint!=null) {
            return sprint.get();
        }
        else
        {
            throw new Exception("Project not found");
        }
    }

    /**
     * Gets all the sprints that belong to a given project.
     * 
     * TODO [Andrew]: Tried making the relationship part of the Project class using @OneToMany & @ManyToOne, didn't work. If someone can figure it out, I'd rather use that.
     */
    public List<Sprint> getSprintsOfProjectById(Integer id) {
        return repository.findByParentProjectId(id);
    }
}
