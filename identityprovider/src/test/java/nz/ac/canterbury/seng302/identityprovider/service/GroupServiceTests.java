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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.*;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.MEMBERS_WITHOUT_GROUPS_ID;
import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;
import static org.junit.jupiter.api.Assertions.*;
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
    private Group testMembersWithoutAGroup;
    private User testUser1;
    private User testUser2;
    private static final int testGroupId = 999;
    private static final int testUserId1 = 111;
    private static final int testUserId2 = 222;

    @BeforeEach
    public void setup() {
        testGroup = new Group("test short name", "test long name");
        testMembersWithoutAGroup = new Group("Members Without A Group",
                "test long name for members without a group");
        testUser1 = new User("testUsername1", "testPassword1", "testFirstName1",
                "testMiddleName1", "testLastName1", "testNickname1",
                "testBio1", "testPronouns1", "testEmail1@example.com");

        testUser2 = new User("testUsername2", "testPassword2", "testFirstName2",
                "testMiddleName2", "testLastName2", "testNickname2",
                "testBio2", "testPronouns2", "testEmail2@example.com");

        when(groupRepository.findById(MEMBERS_WITHOUT_GROUPS_ID))
                .thenReturn(testMembersWithoutAGroup);
    }

    @Test
    void test_getAllGroups() {
        when(groupRepository.findAll())
                .thenReturn(List.of(testGroup));
        List<Group> groups = groupService.getAllGroups();
        assertEquals(1, groups.size());
    }

    @Test
    void test_getGroupById() {
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        Group expected = groupService.getGroup(testGroupId);
        assertEquals(testGroup, expected);
    }

    @Test
    void test_getGroupById_throwsException() {
        when(groupRepository.findById(testGroupId))
                .thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> groupService.getGroup(testGroupId));
    }

    @Test
    void test_addUsersToGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToAdd = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(usersToAdd))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        int numUsersAdded = groupService.addUsersToGroup(testGroupId, usersToAdd);

        // Test that the users are added to the groups members, and that the group is added to the users joined groups
        assertEquals(2, numUsersAdded);
        assertTrue(testGroup.getMembers().contains(testUser1));
        assertTrue(testGroup.getMembers().contains(testUser2));
        assertTrue(testUser1.getGroups().contains(testGroup));
        assertTrue(testUser2.getGroups().contains(testGroup));
    }

    @Test
    void test_removeOneUserFromGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1);
        Iterable<User> users = List.of(testUser1);
        // Add users to group
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        int numUsersRemoved = groupService.removeUsersFromGroup(testGroupId, usersToRemove);

        // Test that user1 is removed, and user2 is still in the group
        assertEquals(1, numUsersRemoved);
        assertFalse(testGroup.getMembers().contains(testUser1));
        assertTrue(testGroup.getMembers().contains(testUser2));
        assertFalse(testUser1.getGroups().contains(testGroup));
        assertTrue(testUser2.getGroups().contains(testGroup));
    }

    @Test
    void test_removeListOfUsersFromGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        // Add users to group
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);
        int numUsersRemoved = groupService.removeUsersFromGroup(testGroupId, usersToRemove);

        // Test that both users are removed from the group
        assertEquals(2, numUsersRemoved);
        assertFalse(testGroup.getMembers().contains(testUser1));
        assertFalse(testGroup.getMembers().contains(testUser2));
        // Test that users no longer have the group in their set of joined groups
        assertFalse(testUser1.getGroups().contains(testGroup));
        assertFalse(testUser2.getGroups().contains(testGroup));
    }

    @Test
    void test_removeAllUsersFromGroup() {
        // Add users to group
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        testGroup.removeAllMembers();

        // Test that all users are removed from the group
        assertFalse(testGroup.getMembers().contains(testUser1));
        assertFalse(testGroup.getMembers().contains(testUser2));
        // Test that users no longer have the group in their set of joined groups
        assertFalse(testUser1.getGroups().contains(testGroup));
        assertFalse(testUser2.getGroups().contains(testGroup));
    }

    @Test
    void test_addUsersToTeacherGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToAdd = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(usersToAdd))
                .thenReturn(users);
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);

        int numUsersAdded = groupService.addUsersToGroup(TEACHER_GROUP_ID, usersToAdd);

        // Test that the users are added to the groups members, and that the group is added to the users joined groups
        assertEquals(2, numUsersAdded);
        assertTrue(testGroup.getMembers().contains(testUser1));
        assertTrue(testGroup.getMembers().contains(testUser2));
        assertTrue(testUser1.getGroups().contains(testGroup));
        assertTrue(testUser2.getGroups().contains(testGroup));
        // Test that users gained the teacher role
        assertTrue(testUser1.getRoles().contains(UserRole.TEACHER));
        assertTrue(testUser2.getRoles().contains(UserRole.TEACHER));
    }

    @Test
    void test_removeUsersFromTeachingGroup() {
        testUser1.addRole(UserRole.TEACHER);
        testUser2.addRole(UserRole.TEACHER);
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        // Add users to group
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);
        int numUsersRemoved = groupService.removeUsersFromGroup(TEACHER_GROUP_ID, usersToRemove);

        // Test that both users are removed from the group
        assertEquals(2, numUsersRemoved);
        assertFalse(testGroup.getMembers().contains(testUser1));
        assertFalse(testGroup.getMembers().contains(testUser2));
        // Test that users no longer have the group in their set of joined groups
        assertFalse(testUser1.getGroups().contains(testGroup));
        assertFalse(testUser2.getGroups().contains(testGroup));
        // Test that users no longer have the teacher role
        assertFalse(testUser1.getRoles().contains(UserRole.TEACHER));
        assertFalse(testUser2.getRoles().contains(UserRole.TEACHER));
        // Test that users have been given the student role instead
        assertTrue(testUser1.getRoles().contains(UserRole.STUDENT));
        assertTrue(testUser2.getRoles().contains(UserRole.STUDENT));
    }

    @Test
    void test_addUsersToMembersWithoutAGroup_doNothing() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToAdd = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);

        when(userRepository.findAllById(usersToAdd))
                .thenReturn(users);

        int numUsersAdded = groupService.addUsersToGroup(MEMBERS_WITHOUT_GROUPS_ID, usersToAdd);

        // Test that the users are not added to the group
        assertEquals(0, numUsersAdded);
    }

    @Test
    void test_removeUsersFromMembersWithoutAGroup_doNothing() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);

        int numUsersRemoved = groupService.removeUsersFromGroup(MEMBERS_WITHOUT_GROUPS_ID, usersToRemove);

        // Test that the users are not added to the group
        assertEquals(0, numUsersRemoved);
    }

    @Test
    void test_removeUsersFromGroup_addedToMembersWithoutAGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        // Add users to group
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        int numUsersRemoved = groupService.removeUsersFromGroup(testGroupId, usersToRemove);

        // Test that the users were added to Members Without A Group
        assertEquals(2, numUsersRemoved);
        assertTrue(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertTrue(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_removeUsersFromOneGroup_notAddedToMembersWithoutAGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToRemove = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        Group testGroup2 = new Group("test short name2", "test long name2");
        // Add users to groups
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);
        testGroup2.addMember(testUser1);
        testGroup2.addMember(testUser2);

        when(userRepository.findAllById(usersToRemove))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        int numUsersRemoved = groupService.removeUsersFromGroup(testGroupId, usersToRemove);

        // Test that the users were not added to Members Without A Group
        assertEquals(2, numUsersRemoved);
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_addUsersToGroup_removedFromMembersWithoutAGroup() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> usersToAdd = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        // Add users to group
        testMembersWithoutAGroup.addMember(testUser1);
        testMembersWithoutAGroup.addMember(testUser2);

        when(userRepository.findAllById(usersToAdd))
                .thenReturn(users);
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        int numUsersAdded = groupService.addUsersToGroup(testGroupId, usersToAdd);

        // Test that the users were removed from Members Without A Group
        assertEquals(2, numUsersAdded);
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_populateMembersWithoutAGroup_noUsersHaveGroups_twoUsersAdded() {
        // Prepare collections of users to use as mock data
        List<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAll())
                .thenReturn(users);

        groupService.populateMembersWithoutAGroup();
        assertTrue(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertTrue(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_populateMembersWithoutAGroup_oneUserHasGroup_oneUserAdded() {
        // Prepare collections of users to use as mock data
        List<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAll())
                .thenReturn(users);
        testGroup.addMember(testUser1);

        groupService.populateMembersWithoutAGroup();
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertTrue(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_populateMembersWithoutAGroup_bothUsersHaveGroups_noUsersAdded() {
        // Prepare collections of users to use as mock data
        List<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAll())
                .thenReturn(users);
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);

        groupService.populateMembersWithoutAGroup();
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser1));
        assertFalse(testMembersWithoutAGroup.getMembers().contains(testUser2));
    }

    @Test
    void test_getPaginatedGroupsAscendingByLongName() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("longName"));
        when(groupRepository.findAll(pageable))
                .thenReturn(List.of(testGroup, testMembersWithoutAGroup));
        List<Group> groups = groupService.getPaginatedGroups(0,2, "longName", true);
        assertEquals(2, groups.size());
        assertEquals("test long name", groups.get(0).getLongName());
    }

    @Test
    void test_getPaginatedGroupsDescendingByShortName() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("shortName").descending());
        when(groupRepository.findAll(pageable))
                .thenReturn(List.of(testGroup, testMembersWithoutAGroup));
        List<Group> groups = groupService.getPaginatedGroups(0,2, "shortName", false);
        assertEquals(2, groups.size());
        assertEquals("test short name", groups.get(0).getShortName());
    }

    @Test
    void test_getPaginatedGroupsByInvalid_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            groupService.getPaginatedGroups(0,2, "invalid", true);
        });
    }
}
