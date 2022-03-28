package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.SprintService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    private Sprint baseSprint;

    @BeforeEach
    public void setUp() {
        baseSprint = new Sprint();
        baseSprint.setSprintName("Sprint 1");
        baseSprint.setSprintDescription("The first.");
        baseSprint.setParentProjectId(5);
        baseSprint.setStartDateString("05/FEB/2022");
        baseSprint.setEndDateString("24/MAR/2022");
    }

    @Test
    public void setNullName_getViolation() {
        baseSprint.setSprintName(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(baseSprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "Sprint name cannot be empty", // this should match the (message = "asdf") bit
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullDescription_getViolation() {
        baseSprint.setSprintDescription(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(baseSprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullStartDate_getViolation() {
        baseSprint.setStartDate(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(baseSprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void setNullEndDate_getViolation() {
        baseSprint.setEndDate(null);
        Set<ConstraintViolation<Sprint>> constraintViolations = validator.validate(baseSprint);
        assertEquals( 1, constraintViolations.size() );
        assertEquals(
                "must not be null",
                constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void searchByName_getSprint() {
        String nameToSearch = "Sprint 1";
        when(sprintRepository.findBySprintName(nameToSearch)).thenReturn(List.of(baseSprint));
        assertThat(sprintService.getSprintByName(nameToSearch)).isEqualTo(List.of(baseSprint));
    }

    @Test
    public void searchById_getSprint() {
        when(sprintRepository.findSprintById(baseSprint.getId())).thenReturn(baseSprint);
        assertThat(sprintService.getSprintById(baseSprint.getId())).isEqualTo(baseSprint);
    }

    @Test
    public void searchByParentProjectId_getSprint() {
        int parentProjectIdToSearch = 5;
        baseSprint.setParentProjectId(parentProjectIdToSearch);
        when(sprintRepository.findByParentProjectId(parentProjectIdToSearch)).thenReturn(List.of(baseSprint));
        assertThat(sprintService.getSprintByParentProjectId(parentProjectIdToSearch)).isEqualTo(List.of(baseSprint));
    }

    @Test
    void saveNullSprint_getException() {
        try { // this is how to get at nested exceptions
            sprintRepository.save(new Sprint());
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullNameSprint_getException() {
        try {
            baseSprint.setSprintName(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveEmptyNameSprint_getException() {
        try {
            baseSprint.setSprintName("");
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullDescriptionSprint_getException() {
        try {
            baseSprint.setSprintDescription(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullStartDateSprint_getException() {
        try {
            baseSprint.setStartDate(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }

    @Test
    void saveNullEndDateSprint_getException() {
        try {
            baseSprint.setEndDate(null);
            sprintRepository.save(baseSprint);
        } catch (TransactionSystemException e) {
            assertInstanceOf(ConstraintViolationException.class, e.getCause().getCause());
        }
    }
}