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

/**
 * Holds unit tests for the Project class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 * For more information on validating annotations, see https://hibernate.org/validator/documentation/getting-started/
 * Note that the annotations don't stop setters from setting invalid values, but they will stop invalid objects from
 * being saved to the database.
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

    private Project baseProject;

    @BeforeEach
    public void setUpProject() {
        baseProject = new Project();
        baseProject.setName("Project 1");
        baseProject.setDescription("The first.");
        baseProject.setStartDateString("01/JAN/2022");
        baseProject.setEndDateString("01/OCT/2022");
        baseProject.setId(1);
    }

    @Test
    public void setNullName_getViolation() {
        baseProject.setName(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Project name cannot be empty", // this should match the (message = "asdf") part of the annotation
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullDescription_getViolation() {
        baseProject.setDescription(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullStartDate_getViolation() {
        baseProject.setStartDate(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullEndDate_getViolation() {
        baseProject.setEndDate(null);
        Set<ConstraintViolation<Project>> constraintViolations = validator.validate(baseProject);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void searchByName_getProject() {
        String nameToSearch = "Project 1";
        baseProject.setName(nameToSearch);
        projectRepository.save(baseProject);
        when(projectRepository.findByProjectName(nameToSearch)).thenReturn(List.of(baseProject));
        assertThat(projectService.getProjectByProjectName(nameToSearch)).isEqualTo(List.of(baseProject));
    }

    @Test
    public void searchById_getProject() throws Exception {
        projectRepository.save(baseProject);
        when(projectRepository.findProjectById(baseProject.getId())).thenReturn(baseProject);
        assertThat(projectService.getProjectById(baseProject.getId())).isEqualTo(baseProject);
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
            baseProject.setName(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyNameProject_getException() {
        try {
            baseProject.setName("");
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullDescriptionProject_getException() {
        try {
            baseProject.setDescription(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullStartDateProject_getException() {
        try {
            baseProject.setStartDate(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullEndDateProject_getException() {
        try {
            baseProject.setEndDate(null);
            projectRepository.save(baseProject);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }
}