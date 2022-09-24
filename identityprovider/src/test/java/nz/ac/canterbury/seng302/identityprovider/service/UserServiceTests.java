package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

@SpringBootTest
@DirtiesContext
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GroupRepository groupRepository;

    private static final String testUsername = "testUser";
    private static final int userID = 999;
    private User testUser;
    private Group testGroup;

    @BeforeEach
    public void setup() {
        testGroup = new Group("Teaching Staff", "teaching staff test long name");
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
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);
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

    @Test
    void test_addTeacherRoleToUser_userAddedToTeachingGroup() {
        when(userRepository.findById(userID))
                .thenReturn(testUser);
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);
        when(userRepository.findAllById(List.of(userID)))
                .thenReturn(List.of(testUser));
        // When: We add the Teacher role to a user
        userService.addRoleToUser(userID, UserRole.TEACHER);
        // Then: The user is added to the Teaching Staff group
        assertTrue(testGroup.getMembers().contains(testUser));
        assertTrue(testUser.getGroups().contains(testGroup));
    }


    @Test
    void test_removeTeacherRoleFromUser_userRemovedFromTeachingGroup() {
        testGroup.addMember(testUser);
        when(userRepository.findById(userID))
                .thenReturn(testUser);
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);
        when(userRepository.findAllById(List.of(userID)))
                .thenReturn(List.of(testUser));
        // When: We remove the Teacher role from a user
        userService.removeRoleFromUser(userID, UserRole.TEACHER);
        // Then: The user is removed from the Teaching Staff group
        assertFalse(testGroup.getMembers().contains(testUser));
        assertFalse(testUser.getGroups().contains(testGroup));
    }
}
