package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

@SpringBootTest
@DirtiesContext
class UserServiceTests {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private static final String testUsername = "testUser";
    private static final int userID = 999;
    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User(testUsername, "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        testUser.addRole(UserRole.TEACHER);
    }

    @Test
    void test_SearchByUsername() {
        when(userRepository.findByUsername(testUsername))
                .thenReturn(testUser);
        assertThat(userService.getUserByUsername(testUsername)).isNotNull().isEqualTo(testUser);
    }

    @Test
    void test_UserCanBeGivenARole() {
        when(userRepository.findById(userID))
                .thenReturn(testUser);
        // When: A user is given the 'TEACHER' role
        userService.addRoleToUser(userID, UserRole.TEACHER);
        // Then: The user's account will show them as a 'TEACHER'
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER),
                "addRoleToUser() couldn't add a new role to the class");
    }

    @Test
    void test_UserCanHaveRoleRemoved() {
        when(userRepository.findById(userID))
                .thenReturn(testUser);
        // When: We take away their 'STUDENT' role
        userService.removeRoleFromUser(userID, UserRole.STUDENT);
        // Then: The user's account will no longer show them as a 'STUDENT'
        assertFalse(testUser.getRoles().contains(UserRole.STUDENT),
                "removeRoleToUser() couldn't remove the STUDENT role from the class");
    }

    @Test
    void test_CantRemoveRoleFromNonexistentUser() {
        // When: We take away their 'STUDENT' role
        assertThrows(NoSuchElementException.class, () -> 
            userService.removeRoleFromUser(userID, UserRole.STUDENT)
        );
    }

}
