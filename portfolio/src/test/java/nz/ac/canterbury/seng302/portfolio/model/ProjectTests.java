package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

// annotation validation https://hibernate.org/validator/documentation/getting-started/


/**
 * Holds unit tests for the Project class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
public class ProjectTests {
    @Autowired
    private ProjectService projectService;

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @MockBean
    private ProjectRepository projectRepository;

    private Project project;

    @BeforeEach
    public void setUpProject() {
        project = new Project();
        project.setName("Project 1");
        project.setDescription("The first.");
        project.setStartDateString("01/JAN/2022");
        project.setEndDateString("01/OCT/2022");
    }

    @Test
    public void nameNotNull() {
        project.setName(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(project);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Project name cannot be empty", // this should match the (message = "asdf") bit
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void descriptionNotNull() {
        project.setDescription(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(project);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void nameSearch() {
        project.setName("Project 1");

        when(projectRepository.findByProjectName("Project 1")).thenReturn(List.of(project));
        assertThat(projectService.getProjectByProjectName("Project 1")).isEqualTo(project);
    }

    @Test
    public void idSearch() throws Exception {
        when(projectRepository.findById(project.getId())).thenReturn(project);
        assertThat(projectService.getProjectById(project.getId())).isEqualTo(project);
    }
}