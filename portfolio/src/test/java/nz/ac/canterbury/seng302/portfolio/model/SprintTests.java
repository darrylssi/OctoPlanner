package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
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

/**
 * Holds unit tests for the Sprint class.
 * These tests (should) make sure that the JPA annotations (e.g. @NotEmpty) work correctly.
 */
@SpringBootTest
public class SprintTests {
    @Autowired
    private SprintService sprintService;

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @MockBean
    private SprintRepository sprintRepository;

    private Sprint sprint;

    @BeforeEach
    public void setUp() {
        sprint = new Sprint();
        sprint.setSprintName("Sprint 1");
        sprint.setSprintDescription("The first.");
        sprint.setParentProjectId(5);
        sprint.setStartDateString("05/FEB/2022");
        sprint.setEndDateString("24/MAR/2022");
    }

    @Test
    public void nameNotNull() {
        sprint.setSprintName(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(sprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Sprint name cannot be empty", // this should match the (message = "asdf") bit
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void descriptionNotNull() {
        sprint.setSprintDescription(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(sprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void startDateNotNull() {
        sprint.setStartDate(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(sprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void endDateNotNull() {
        sprint.setEndDate(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(sprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void nameSearch() {
        when(sprintRepository.findBySprintName("Sprint 1")).thenReturn(List.of(sprint));
        assertThat(sprintService.getSprintBySprintName("Sprint 1")).isEqualTo(sprint);
    }

    @Test
    public void idSearch() throws Exception {
        when(sprintRepository.findById(sprint.getId())).thenReturn(sprint);
        assertThat(sprintService.getSprintById(sprint.getId())).isEqualTo(sprint);
    }

    @Test
    public void parentProjectIdSearch() {
        sprint.setParentProjectId(5);
        when(sprintRepository.findByParentProjectId(5)).thenReturn(List.of(sprint));
        assertThat(sprintService.getSprintByParentProjectId(5)).isEqualTo(sprint);
    }

    /**
     * This SHOULD just test whether individual fields are validated correctly,
     * but it needs additional dependencies to get an assert statement.
     */
    @Test
    public void testRepository() {
        sprint.setStartDateString("12/jan/2022");
        sprint.setEndDateString("02/feb/2022");
        sprint.setSprintName("Sprint One");
        sprint.setSprintDescription("The first.");

        sprintRepository.save(sprint);
    }
}