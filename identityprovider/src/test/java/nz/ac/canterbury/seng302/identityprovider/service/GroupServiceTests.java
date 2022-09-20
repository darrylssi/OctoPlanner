package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
class GroupServiceTests {

    @Autowired
    private GroupService groupService;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private UserRepository userRepository;

    private Group testGroup;
    private User testUser;
    private static final int testGroupId = 999;
    private static final int testUserId = 999;

    @BeforeEach
    public void setup() {
        testGroup = new Group("test short name", "test long name");
        testUser = new User("testUsername", "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        testUser.addRole(UserRole.TEACHER);
    }

    @Test
    void test_addUsersToGroup() {
        Set<Integer> usersToAdd = new HashSet<>(testUserId);
        // TODO figure out how to mock findAllById
        //when(userRepository.findAllById(usersToAdd))
        //        .thenReturn(testUserId);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        groupService.addUsersToGroup(testGroupId, usersToAdd);

        assertTrue(testGroup.getMembers().contains(testUser));
        assertTrue(testUser.getGroups().contains(testGroup));
    }

}
