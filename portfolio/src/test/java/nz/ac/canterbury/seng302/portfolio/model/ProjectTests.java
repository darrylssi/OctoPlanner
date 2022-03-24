package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionSystemException;

import javax.validation.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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
        project.setId(1);
    }

    @Test // these constraints don't actually stop the setter from working, but the test checks that an error is raised
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
        projectRepository.save(project);
        when(projectRepository.findByProjectName("Project 1")).thenReturn(List.of(project));
        assertThat(projectService.getProjectByProjectName("Project 1")).isEqualTo(List.of(project));
    }

    @Test
    public void idSearch() throws Exception {
        projectRepository.save(project);
        when(projectRepository.findProjectById(project.getId())).thenReturn(project);
        assertThat(projectService.getProjectById(project.getId())).isEqualTo(project);
    }

    @Test
    void saveNullProject_getException() {
        try { // this is how to get at nested exceptions
            projectRepository.save(new Project());
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullNameProject_getException() {
        try {
            Project nullNameProject = new Project(null, "Desc", "01/jan/2022", "02/jan/2022");
            projectRepository.save(nullNameProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyNameProject_getException() {
        try {
            Project emptyNameProject = new Project("", "Desc", "01/jan/2022", "02/jan/2022");
            projectRepository.save(emptyNameProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullDescriptionProject_getException() {
        try {
            Project nullDescriptionProject = new Project("P1", null, "01/jan/2022", "02/jan/2022");
            projectRepository.save(nullDescriptionProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }


    @Test
    void saveNullStartDateProject_getException() {
        try {
            Project nullStartDateProject = new Project("P1", "This is Project 1", null, "02/jan/2022");
            projectRepository.save(nullStartDateProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }


    @Test
    void saveNullEndDateProject_getException() {
        try {
            Project nullEndDateProject = new Project("P1", "This is Project 1", "02/jan/2022", null);
            projectRepository.save(nullEndDateProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }
}