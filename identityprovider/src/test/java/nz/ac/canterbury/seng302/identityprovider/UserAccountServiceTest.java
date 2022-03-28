package nz.ac.canterbury.seng302.identityprovider;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@SuppressWarnings("unchecked")
public class UserAccountServiceTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

    @MockBean
    private UserRepository userRepository;

    private static final int testUserID = 999;
    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User("testUser", "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        testUser.addRole(UserRole.STUDENT);
    }

    @Test
    void testValidRegister() {
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("test1")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
    }

    @Test
    void test_CanAddRoleToUser() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);

        // * Given: A user doesn't have a role
        assertFalse(testUser.getRoles().contains(UserRole.COURSE_ADMINISTRATOR));
        
        // * When: We try to give them this role
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.TEACHER)
                .build();
        userAccountServerService.addRoleToUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: They now have this role
        assertTrue(response.getIsSuccess());
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER));
    }
    @Test
    void test_CanRemoveRoleFromUser() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);

        // * Given: A user has a role
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT));

        // * When: We try to take this role away from them
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.TEACHER)
                .build();
        userAccountServerService.addRoleToUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: They no longer have this role
        assertTrue(response.getIsSuccess());
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER));
    }

    @Test
    void test_CanRemoveRolesAfterDuplicateRoleAdds() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.STUDENT)
                .build();

        // * Given: A user is given a role multiple times
        userAccountServerService.addRoleToUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT)); // They've been given the role
        // Add again
        userAccountServerService.addRoleToUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT)); // User still has the role

        // * When: This role is removed
        userAccountServerService.removeRoleFromUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: The role is removed
        assertTrue(response.getIsSuccess());
        assertFalse(testUser.getRoles().contains(UserRole.STUDENT));    // User still has role
    }

    @Test
    void test_FailsToAddDuplicateRoles() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);
        // * Given: A user has a role
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT));
        // * When: A user is given a role they already have
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.STUDENT)
                .build();
        userAccountServerService.addRoleToUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: It fails, and the user's role doesn't change
        assertFalse(response.getIsSuccess());
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT));
    }

    @Test
    void test_FailsToRemoveRoleThatUserDoesntHave() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);

        // * Given: A user doesn't have a role
        
        assertFalse(testUser.getRoles().contains(UserRole.COURSE_ADMINISTRATOR));
        // * When: Someone tries to remove said role
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.COURSE_ADMINISTRATOR)
                .build();
        userAccountServerService.removeRoleFromUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: It fails, and the user's role doesn't change
        assertFalse(response.getIsSuccess()); 
        assertFalse(testUser.getRoles().contains(UserRole.COURSE_ADMINISTRATOR));
    }

    @Test
    void test_FailToAddRoleToNonexistentUser() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<StatusRuntimeException> errorCaptor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        // * When: We try to add a role to a non-existent user
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(-99999)
                .setRole(UserRole.TEACHER)
                .build();
        userAccountServerService.addRoleToUser(request, observer);

        // * Then: The interface throws an error
        verify(observer, times(1)).onError(errorCaptor.capture());
        StatusRuntimeException error = errorCaptor.getValue();

        assertEquals(error.getStatus().getCode(), Status.NOT_FOUND.getCode());
    }

    @Test
    void test_FailToRemoveRoleToNonexistentUser() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<StatusRuntimeException> errorCaptor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        // * When: We try to add a role to a non-existent user
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(-99999)
                .setRole(UserRole.TEACHER)
                .build();
        userAccountServerService.removeRoleFromUser(request, observer);

        // * Then: The interface throws an error
        verify(observer, times(1)).onError(errorCaptor.capture());
        StatusRuntimeException error = errorCaptor.getValue();

        assertEquals(error.getStatus().getCode(), Status.NOT_FOUND.getCode());
    }
}
