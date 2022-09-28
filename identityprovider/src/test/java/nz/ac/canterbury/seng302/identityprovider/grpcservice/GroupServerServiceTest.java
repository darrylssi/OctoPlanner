package nz.ac.canterbury.seng302.identityprovider.grpcservice;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.GroupServerService;
import nz.ac.canterbury.seng302.identityprovider.service.GroupService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.MEMBERS_WITHOUT_GROUPS_ID;
import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;
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
        testGroup.setId(testGroupId);
        testUser1 = new User("testUsername1", "testPassword1", "testFirstName1",
                "testMiddleName1", "testLastName1", "testNickname1",
                "testBio1", "testPronouns1", "testEmail1@example.com");
        testUser1.setId(testUserId1);
        testUser1.setCreated(Instant.now());
        testUser1.addRole(UserRole.TEACHER);

        testUser2 = new User("testUsername2", "testPassword2", "testFirstName2",
                "testMiddleName2", "testLastName2", "testNickname2",
                "testBio2", "testPronouns2", "testEmail2@example.com");
        testUser2.setId(testUserId2);
        testUser2.setCreated(Instant.now());
        testUser2.addRole(UserRole.STUDENT);
    }

    @Test
    void testCreateGroup_whenValid_getSuccess() {
        StreamObserver<CreateGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<CreateGroupResponse> captor = ArgumentCaptor.forClass(CreateGroupResponse.class);
        // * When: We try to create a group
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName("test valid short name")
                .setLongName("test valid long name")
                .build();
        groupServerService.createGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        CreateGroupResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
    }

    @ParameterizedTest
    @CsvSource({"'',Group short name cannot be empty",
            "a,Group short name must be between 2 and 32 characters",
            "Thirty-Three Character Long Name3,Group short name must be between 2 and 32 characters"})
    void testCreateGroup_whenShortNameInvalid_getFailure(String shortName, String errorMessage) {
        StreamObserver<CreateGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<CreateGroupResponse> captor = ArgumentCaptor.forClass(CreateGroupResponse.class);
        // * When: We try to create a group
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName("test valid long name")
                .build();
        groupServerService.createGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        CreateGroupResponse response = captor.getValue();

        // Expected error message
        ValidationError error = ValidationError.newBuilder()
                .setFieldName("shortName")
                .setErrorText(errorMessage)
                .build();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertTrue(response.getValidationErrorsList().contains(error));
    }

    @Test
    void testCreateGroup_whenLongNameTooLong_getFailure() {
        StreamObserver<CreateGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<CreateGroupResponse> captor = ArgumentCaptor.forClass(CreateGroupResponse.class);
        // * When: We try to create a group
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName("test valid short name")
                .setLongName("One hundred and twenty-nine character long long name Lorem ipsum dolor sit amet, " +
                        "consectetur adipiscing elit, sed do eiusmod temp")
                .build();
        groupServerService.createGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        CreateGroupResponse response = captor.getValue();

        // Expected error message
        ValidationError error = ValidationError.newBuilder()
                .setFieldName("longName")
                .setErrorText("Group long name must not exceed 128 characters")
                .build();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertTrue(response.getValidationErrorsList().contains(error));
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
        when(groupRepository.findById(MEMBERS_WITHOUT_GROUPS_ID))
                .thenReturn(testMembersWithoutAGroup);

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
        when(groupRepository.findById(MEMBERS_WITHOUT_GROUPS_ID))
                .thenReturn(testMembersWithoutAGroup);

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

    @Test
    void testDeleteGroup_whenValid_getSuccess() {
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);
        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        // * When: We try to delete a group
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(testGroupId)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        DeleteGroupResponse response = captor.getValue();

        // * Then: The request succeeds
        assertTrue(response.getIsSuccess());
        assertFalse(testUser1.getGroups().contains(testGroup));
        assertFalse(testUser2.getGroups().contains(testGroup));
    }

    @Test
    void testDeleteGroup_whenGroupDoesNotExist_getFailure() {
        // * Given: There is no group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(null);

        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        // * When: We try to delete a group
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(testGroupId)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        DeleteGroupResponse response = captor.getValue();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertEquals("There is no group with id " + testGroupId, response.getMessage());
    }

    @Test
    void testDeleteGroup_whenDeletingTeachingStaff_getFailure() {
        // * Given: This group is the teaching staff group
        when(groupRepository.findById(TEACHER_GROUP_ID))
                .thenReturn(testGroup);

        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        // * When: We try to delete the teaching staff group
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(TEACHER_GROUP_ID)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        DeleteGroupResponse response = captor.getValue();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertEquals("The group \"Teaching Staff\" cannot be deleted", response.getMessage());
    }

    @Test
    void testDeleteGroup_whenDeletingMembersWithoutAGroup_getFailure() {
        // * Given: This group is the group for members without groups
        when(groupRepository.findById(MEMBERS_WITHOUT_GROUPS_ID))
                .thenReturn(testGroup);

        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        // * When: We try to delete the group for members without groups
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(MEMBERS_WITHOUT_GROUPS_ID)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        DeleteGroupResponse response = captor.getValue();

        // * Then: The request fails
        assertFalse(response.getIsSuccess());
        assertEquals("The group \"Members Without A Group\" cannot be deleted", response.getMessage());
    }

    @Test
    void testGetGroupDetails_whenValid_detailsRetrieved() {
        testGroup.addMember(testUser1);
        testGroup.addMember(testUser2);
        // * Given: There is a group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(testGroup);

        StreamObserver<GetGroupDetailsResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<GetGroupDetailsResponse> captor = ArgumentCaptor.forClass(GetGroupDetailsResponse.class);
        // * When: We try to get a group's details
        GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                .setGroupId(testGroupId)
                .build();
        groupServerService.getGroupDetails(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        GetGroupDetailsResponse response = captor.getValue();

        // * Then: We get the group's details
        assertEquals(testGroup.getShortName(), response.getShortName());
        assertEquals(testGroup.getLongName(), response.getLongName());
        assertTrue(testGroup.getMembers().contains(testUser1));
        assertTrue(testGroup.getMembers().contains(testUser2));
    }

    @Test
    void testGetGroupDetails_whenGroupDoesNotExist_noDetailsReturned() {
        // * Given: There is no group with this id
        when(groupRepository.findById(testGroupId))
                .thenReturn(null);

        StreamObserver<GetGroupDetailsResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<GetGroupDetailsResponse> captor = ArgumentCaptor.forClass(GetGroupDetailsResponse.class);
        // * When: We try to get a group's details
        GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                .setGroupId(testGroupId)
                .build();
        groupServerService.getGroupDetails(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        GetGroupDetailsResponse response = captor.getValue();

        // * Then: We don't get any details
        assertEquals("", response.getShortName());
        assertEquals("", response.getLongName());
        assertEquals(0, response.getMembersCount());
    }
}
