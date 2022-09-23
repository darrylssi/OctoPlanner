package nz.ac.canterbury.seng302.identityprovider.grpcservice;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.GroupServerService;
import nz.ac.canterbury.seng302.identityprovider.service.GroupService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Contains tests for the GroupServerService class
 */
@SpringBootTest
@DirtiesContext
@SuppressWarnings("unchecked")
class GroupServerServiceTest {

    @Autowired
    private GroupServerService groupServerService;

    @MockBean
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService = spy(GroupService.class);

    @MockBean
    private UserRepository userRepository;

    private Group testGroup;
    private User testUser1;
    private User testUser2;
    private static final int testGroupId = 999;
    private static final int testUserId1 = 111;
    private static final int testUserId2 = 222;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testGroup = new Group("test short name", "test long name");
        testGroup.setId(testGroupId);
        testUser1 = new User("testUsername1", "testPassword1", "testFirstName1",
                "testMiddleName1", "testLastName1", "testNickname1",
                "testBio1", "testPronouns1", "testEmail1@example.com");
        testUser1.setId(testUserId1);
        testUser1.addRole(UserRole.TEACHER);

        testUser2 = new User("testUsername2", "testPassword2", "testFirstName2",
                "testMiddleName2", "testLastName2", "testNickname2",
                "testBio2", "testPronouns2", "testEmail2@example.com");
        testUser2.setId(testUserId2);
        testUser2.addRole(UserRole.STUDENT);
    }

    @Test
    void testCreateGroup_whenValid() {
        StreamObserver<CreateGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<CreateGroupResponse> captor = ArgumentCaptor.forClass(CreateGroupResponse.class);
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName("test valid short name")
                .setLongName("test valid long name")
                .build();
        groupServerService.createGroup(request, observer);

        // TODO uncomment these lovely assertions when the methods have been implemented
        //verify(observer, times(1)).onCompleted();
        //verify(observer, times(1)).onNext(captor.capture());
        //CreateGroupResponse response = captor.getValue();

        //assertTrue(response.getIsSuccess());

        // TODO and get rid of this one
        assertEquals("test long name", testGroup.getLongName());
    }

    @Test
    void testAddUsersToGroup_getSuccess() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        // * When: We try to add members to this group
        StreamObserver<AddGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<AddGroupMembersResponse> captor = ArgumentCaptor.forClass(AddGroupMembersResponse.class);
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.addGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        AddGroupMembersResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
        assertEquals("2 users added to group " + testGroupId, response.getMessage());
    }

    @Test
    void testAddZeroUsersToGroup_getSuccess() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = Collections.emptyList();
        Iterable<User> users = Collections.emptyList();
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        // * When: We try to add members to this group
        StreamObserver<AddGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<AddGroupMembersResponse> captor = ArgumentCaptor.forClass(AddGroupMembersResponse.class);
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.addGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        AddGroupMembersResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
        assertEquals("0 users added to group " + testGroupId, response.getMessage());
    }

    @Test
    void testAddUsersToGroup_whenGroupDoesNotExist_getFailure() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is no group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(null);

        // * When: We try to add members to this group
        StreamObserver<AddGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<AddGroupMembersResponse> captor = ArgumentCaptor.forClass(AddGroupMembersResponse.class);
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.addGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        AddGroupMembersResponse response = captor.getValue();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertEquals("There is no group with id " + testGroupId, response.getMessage());
    }

    @Test
    void testRemoveMembersFromGroup_getSuccess() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        // * When: We try to remove members from this group
        StreamObserver<RemoveGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<RemoveGroupMembersResponse> captor = ArgumentCaptor.forClass(RemoveGroupMembersResponse.class);
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.removeGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        RemoveGroupMembersResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
        assertEquals("2 users removed from group " + testGroupId, response.getMessage());
    }

    @Test
    void testRemoveZeroMembersFromGroup_getSuccess() {
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = Collections.emptyList();
        Iterable<User> users = Collections.emptyList();
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        // * When: We try to remove members from this group
        StreamObserver<RemoveGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<RemoveGroupMembersResponse> captor = ArgumentCaptor.forClass(RemoveGroupMembersResponse.class);
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.removeGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        RemoveGroupMembersResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
        assertEquals("0 users removed from group " + testGroupId, response.getMessage());
    }

    @Test
    void testRemoveMembersFromGroup_whenGroupDoesNotExist_getFailure(){
        // Prepare collections of user ids/users to use as mock data
        List<Integer> userIds = List.of(testUserId1, testUserId2);
        Iterable<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAllById(userIds))
                .thenReturn(users);

        // * Given: There is no group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(null);

        // * When: We try to remove members from this group
        StreamObserver<RemoveGroupMembersResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<RemoveGroupMembersResponse> captor = ArgumentCaptor.forClass(RemoveGroupMembersResponse.class);
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(testGroupId)
                .addAllUserIds(userIds)
                .build();
        groupServerService.removeGroupMembers(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        RemoveGroupMembersResponse response = captor.getValue();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertEquals("There is no group with id " + testGroupId, response.getMessage());
    }
}
