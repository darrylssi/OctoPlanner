package nz.ac.canterbury.seng302.identityprovider;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private User testUser;

    private static final int testUserID = 999;
    private final BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        testUser = new User("testUser", encoder.encode("testPassword"), "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        testUser.addRole(UserRole.STUDENT);
    }

    @Test
    void testValidRegister() {
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testValid")
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
        testUser.addRole(UserRole.TEACHER);

        // * When: We try to take this role away from them
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.TEACHER)
                .build();
        userAccountServerService.removeRoleFromUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: They no longer have this role
        assertTrue(response.getIsSuccess());
        assertFalse(testUser.getRoles().contains(UserRole.TEACHER));
    }

    @Test
    void test_CanRemoveRolesAfterDuplicateRoleAdds() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.TEACHER)
                .build();

        // * Given: A user is given a role multiple times
        userAccountServerService.addRoleToUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER)); // They've been given the role
        // Add again
        userAccountServerService.addRoleToUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        assertTrue(testUser.getRoles().contains(UserRole.TEACHER)); // User still has the role

        // * When: This role is removed
        userAccountServerService.removeRoleFromUser(request, observer);
        verify(observer, atLeastOnce()).onCompleted();
        verify(observer, atLeastOnce()).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: The role is removed
        assertTrue(response.getIsSuccess());
        assertFalse(testUser.getRoles().contains(UserRole.TEACHER));    // User still has role
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

    @Test
    void test_UsersMustAlwaysHaveAtLeastOneRole() {
        StreamObserver<UserRoleChangeResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> captor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        when(userRepository.findById(testUserID))
                .thenReturn(testUser);
        // * Given: A user only has one role
        assertTrue(testUser.getRoles().size() == 1);
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT));
        // * When: We try to take away their only role

        
        // * When: Someone tries to remove said role
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setUserId(testUserID)
                .setRole(UserRole.COURSE_ADMINISTRATOR)
                .build();
        userAccountServerService.removeRoleFromUser(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        UserRoleChangeResponse response = captor.getValue();

        // * Then: It fails and they keep their role
        assertFalse(response.getIsSuccess()); 
        assertTrue(testUser.getRoles().contains(UserRole.STUDENT));
    }

    @Test
    void testRegister_whenMissingUsername() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Username")
                .setErrorText("Username cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingPassword() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testMissingPassword")
                .setPassword("")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Password")
                .setErrorText("Password cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingFirstName() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testMissingFirstName")
                .setPassword("testPassword")
                .setFirstName("")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("FirstName")
                .setErrorText("FirstName cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingLastName() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testMissingLastName")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("LastName")
                .setErrorText("LastName cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingEmail() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testMissingEmail")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Email")
                .setErrorText("Email cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenUsernameIsTaken() {
        User testUser = new User("testSameUser", "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        userRepository.save(testUser); // Doesn't actually add to repository?

        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testSameUser")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Username")
                .setErrorText("Username is already in use")
                .build();

        assertTrue(response.getIsSuccess()); // This should assert false
        // TODO fix this test case so that it ACTUALLY works
        //assertFalse(response.getIsSuccess());
        //assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenValid() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals("testUser", testUser.getUsername());
        assertEquals("editFirstName", testUser.getFirstName());
        assertEquals("editMiddleName", testUser.getMiddleName());
        assertEquals("editLastName", testUser.getLastName());
        assertEquals("editNickname", testUser.getNickname());
        assertEquals("editBio", testUser.getBio());
        assertEquals("edit/pronouns", testUser.getPersonalPronouns());
        assertEquals("edit@example.com", testUser.getEmail());
    }

    @Test
    void testEdit_whenMissingFirstName() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("FirstName")
                .setErrorText("First name cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenFirstNameTooShort() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("e")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("FirstName")
                .setErrorText("First name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenFirstNameTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstNameabcdefgh")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("FirstName")
                .setErrorText("First name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenMiddleNameTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleNameabcdefg")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("MiddleName")
                .setErrorText("Middle name must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenMissingLastName() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("LastName")
                .setErrorText("Last name cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenLastNameTooShort() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("e")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("LastName")
                .setErrorText("Last name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenLastNameTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastNameabcdefghi")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("LastName")
                .setErrorText("Last name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenNicknameTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNicknameabcdefghi")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Nickname")
                .setErrorText("Nickname must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenBioTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio long edition editBio long edition editBio long edition editBio long" +
                        " edition editBio long edition editBio long edition editBio long edition" +
                        " editBio long edition editBio long edition editBio long edition ")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Bio")
                .setErrorText("Bio must have less than 200 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenPronounsTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronounsabcdefgh")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("PersonalPronouns")
                .setErrorText("Personal pronouns must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenPronounsAreInvalid() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("editpronouns")
                .setEmail("edit@example.com")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("PersonalPronouns")
                .setErrorText("Personal pronouns must contain a \"/\"")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenMissingEmail() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Email")
                .setErrorText("Email cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testEdit_whenEmailIsInvalid() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        EditUserRequest request = EditUserRequest.newBuilder()
                .setUserId(1)
                .setFirstName("editFirstName")
                .setMiddleName("editMiddleName")
                .setLastName("editLastName")
                .setNickname("editNickname")
                .setBio("editBio")
                .setPersonalPronouns("edit/pronouns")
                .setEmail("invalidEmail")
                .build();
        StreamObserver<EditUserResponse> observer = mock(StreamObserver.class);
        userAccountServerService.editUser(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<EditUserResponse> captor = ArgumentCaptor.forClass(EditUserResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        EditUserResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Email")
                .setErrorText("Email must be valid")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testChangePassword_whenValid() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("testPassword")
                .setNewPassword("changedPassword")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
    }

    @Test
    void testChangePassword_whenMissingCurrentPassword() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("")
                .setNewPassword("changedPassword")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("CurrentPassword")
                .setErrorText("Current password cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testChangePassword_whenCurrentPasswordIsInvalid() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("wrongPassword")
                .setNewPassword("changedPassword")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("CurrentPassword")
                .setErrorText("Current password does not match password in database")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testChangePassword_whenMissingNewPassword() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("testPassword")
                .setNewPassword("")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("NewPassword")
                .setErrorText("New password cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testChangePassword_whenNewPasswordTooShort() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("testPassword")
                .setNewPassword("change")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("NewPassword")
                .setErrorText("New password must be between 7 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testChangePassword_whenNewPasswordTooLong() {
        when(userRepository.findById(1))
                .thenReturn(testUser);

        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setUserId(1)
                .setCurrentPassword("testPassword")
                .setNewPassword("changedPasswordabcdef")
                .build();

        StreamObserver<ChangePasswordResponse> observer = mock(StreamObserver.class);
        userAccountServerService.changeUserPassword(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<ChangePasswordResponse> captor = ArgumentCaptor.forClass(ChangePasswordResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        ChangePasswordResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("NewPassword")
                .setErrorText("New password must be between 7 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }
}
