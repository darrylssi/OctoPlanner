package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

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
        if(project !=  null) {
            return project;
        } else {
            throw new Exception("Project not found");
        }
    }

    /**
     * Get project by name
     */
    public List<Project> getProjectByProjectName(String name) {

        return repository.findByProjectName(name);
    }
}
