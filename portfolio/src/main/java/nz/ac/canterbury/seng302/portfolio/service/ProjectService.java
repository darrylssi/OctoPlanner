package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

/**
 * Service class holding methods used to access the underlying project repository methods,
 * which actually touch the database.
 *
 * Service methods need to be written manually, unlike repository methods, and should deal with errors (mainly items
 * not being found).
 */
@Service
public class ProjectService {
    @Autowired
    private ProjectRepository repository;

    /**
     * Get list of all projects
     */
    public List<Project> getAllProjects() {
        return (List<Project>) repository.findAll();
    }

    /**
     * Get project by id
     */
    public Project getProjectById(Integer id) throws Exception {
        Project project = repository.findProjectById(id);
        if (project != null) {
            return project;
        } else {
            throw new Exception("Project not found.");
        }

    }

    /**
     * Saves a new project to the database if the project of the given ID does not exist.
     * Otherwise, it updates the details of the project if there are any changes.
     * @param project Project to be saved or updated.
     */
    public void saveProject(Project project) {
        repository.save(project);
    }
}
