package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
// @TestInstance(Lifecycle.PER_CLASS)
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private static String testUsername = "testUser";
    private int userId;

    @BeforeEach
    public void setup() {
        testUser = new User(testUsername, "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        userRepository.save(testUser);
        userId = testUser.getID();
    }

    @AfterEach
    public void teardown() {
        userRepository.delete(testUser);
    }

    @Test
    public void searchByUsername() {
        when(userRepository.findByUsername(testUsername))
            .thenReturn(testUser);
        assertThat(userService.getUserByUsername(testUsername)).isNotNull().isEqualTo(testUser);
    }

    @Test
    public void test_userCanBeGivenARole() {
        when(userRepository.findById(testUser.getID()))
            .thenReturn(testUser);

        // Given: A user doesn't have a 'TEACHER' role
        assertFalse(testUser.getRoles().contains(UserRole.TEACHER));
        // When: A user is given the 'TEACHER' role
        userService.addRoleToUser(testUser.getID(), UserRole.TEACHER);
        // Then: The user's account will show them as a 'TEACHER'
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER), "addRoleToUser() couldn't add a new role to the class");
    }

    @Test
    public void test_userCanHaveRoleRemoved() {
        when(userRepository.findById(userId))
            .thenReturn(testUser);
                
        // Given: A user has a 'STUDENT' role
        userService.addRoleToUser(testUser.getID(), UserRole.TEACHER);
        // When: We take away their 'STUDENT' role
        userService.removeRoleFromUser(userId, UserRole.STUDENT);
        // Then: The user's account will no longer show them as a 'STUDENT'
        assertFalse(testUser.getRoles().contains(UserRole.STUDENT), "removeRoleToUser() couldn't remove the STUDENT role from the class");
    }

}
