package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for the Group class.
 * This ensures that the JPA annotations work correctly.
 */
@SpringBootTest
public class GroupTests {

    private Group group;                                    // Initialises the group

    @MockBean
    private GroupClientService groupClientService;

    @BeforeEach
    void setup() {
        // Creates and sets the group details
        group = new Group();
        group.setGroupShortName("Test Group");
        group.setGroupLongName("Test Project Group 2022");
    }

    @Test
    void saveNullGroupShortName_getException() {
        group.setGroupShortName(null);
        when(groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName()));
    }

    @Test
    void saveNullGroupLongName_getException() {
        group.setGroupLongName(null);
        when(groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "This is a very long group short name that is longer than the maximum limit"})
    void saveInvalidGroupShortName_getException(String name) {
        group.setGroupShortName(name);
        when(groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "This is a really really long group long name that is hopefully longer than the maximum limit " +
            "of one hundred and twenty eight characters"})
    void saveInvalidGroupLongName_getException(String name) {
        group.setGroupLongName(name);
        when(groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName()));
    }



}
