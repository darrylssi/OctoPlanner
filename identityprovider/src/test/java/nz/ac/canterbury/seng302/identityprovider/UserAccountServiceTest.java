package nz.ac.canterbury.seng302.identityprovider;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.Base64DecodedMultipartFile;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@DirtiesContext
@SuppressWarnings("unchecked")
class UserAccountServiceTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    private static final int testUserID = 999;
    private final BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();

    // Used for photo tests only
    private static final String BASE64_PREFIX = "data:image/jpeg;base64,";
    private static final int TEST_PHOTO_USERID = 1;
    private static final String TEST_PHOTO_NAME = "/" + TEST_PHOTO_USERID + "_photo.jpg";
    private static final String TEST_PHOTO_FORMAT = "jpg";

    @Value("${profile-image-folder}")
    private Path profileImageFolder;

    @BeforeEach
    public void setup() {
        testUser = new User("testUser", encoder.encode("testPassword"), "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        testUser.addRole(UserRole.STUDENT);
    }

    @Test
    void testRegister_whenValid() {
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
        assertEquals(1, testUser.getRoles().size());
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
    void testRegister_whenUsernameTooShort() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("u")
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
                .setErrorText("Username must be between 2 to 15 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenUsernameTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsernamelong")
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
                .setErrorText("Username must be between 2 to 15 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingPassword() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
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
                .setUsername("testUsername")
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
                .setErrorText("First name cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenFirstNameTooShort() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("f")
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
                .setErrorText("First name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenFirstNameTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frankabcdefghijklmnop")
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
                .setErrorText("First name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMiddleNameTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michaelabcdefghijklmn")
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
                .setFieldName("MiddleName")
                .setErrorText("Middle name must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingLastName() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
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
                .setErrorText("Last name cannot be empty")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenLastNameTooShort() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("L")
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
                .setErrorText("Last name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenLastNameTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucasabcdefghijklmnop")
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
                .setErrorText("Last name must be between 2 to 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenNicknameTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nickabcdefghijklmnopq")
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
                .setFieldName("Nickname")
                .setErrorText("Nickname must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenBioTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio  This is a test bio  This is a test bio  This is a test bio  " +
                        "This is a test bio  This is a test bio  This is a test bio  This is a test bio  " +
                        "This is a test bio  This is a test bio201")
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
                .setFieldName("Bio")
                .setErrorText("Bio must have less than 200 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenPronounsTooLong() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronounsabcdefgh")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("PersonalPronouns")
                .setErrorText("Personal pronouns must have less than 20 characters")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenPronounsAreInvalid() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("testpronouns")
                .setEmail("test@example.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("PersonalPronouns")
                .setErrorText("Personal pronouns must be in the format \"pronoun/pronoun\"")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenMissingEmail() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
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
    void testRegister_whenEmailIsInvalid() {
        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUsername")
                .setPassword("testPassword")
                .setFirstName("Frank")
                .setMiddleName("Michael")
                .setLastName("Lucas")
                .setNickname("Nick")
                .setBio("This is a test bio")
                .setPersonalPronouns("test/pronouns")
                .setEmail("testexample.com")
                .build();
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Email")
                .setErrorText("Email must be valid")
                .build();

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
    }

    @Test
    void testRegister_whenUsernameIsTaken() {
        when(userRepository.findByUsername("testUser"))
                .thenReturn(testUser);

        UserRegisterRequest request = UserRegisterRequest.newBuilder()
                .setUsername("testUser")
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

        assertFalse(response.getIsSuccess());
        assertEquals(error, response.getValidationErrors(0));
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
                .setErrorText("Personal pronouns must be in the format \"pronoun/pronoun\"")
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

    @Test
    void deleteNonexistentPhoto_getFailure() throws IOException {
        /* Given: There is no photo for the given user */
        Files.deleteIfExists(profileImageFolder.resolve(TEST_PHOTO_NAME));

        /* When: A request is received to delete the user's photo */
        DeleteUserProfilePhotoRequest request = DeleteUserProfilePhotoRequest.newBuilder()
                .setUserId(TEST_PHOTO_USERID)
                .build();
        StreamObserver<DeleteUserProfilePhotoResponse> observer = mock(StreamObserver.class);
        userAccountServerService.deleteUserProfilePhoto(request, observer);

        /* Then: The response will be a failure */
        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<DeleteUserProfilePhotoResponse> captor = ArgumentCaptor.forClass(
                DeleteUserProfilePhotoResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        DeleteUserProfilePhotoResponse response = captor.getValue();

        assertFalse(response.getIsSuccess());

        assertNotNull(response.getMessage());
        assertNotEquals("", response.getMessage());
    }

    /**
     * Creates a 100x100 px image saved in the photo folder for the test user id.
     */
    void createTestUserImage() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, TEST_PHOTO_FORMAT, new File( profileImageFolder + TEST_PHOTO_NAME));
    }

    /**
     * Creates a blank, square image of custom size and packs it into a MultipartFile to sending over grpc.
     * @param imageSize width and height of image
     * @return MultpartFile with this image enclosed
     * @throws IOException if an error occurs when writing the image
     */
    MultipartFile getTestUserImageMultipartFile(int imageSize) throws IOException {
        BufferedImage newImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(newImage, TEST_PHOTO_FORMAT, outputStream);
        byte[] fileBytes = outputStream.toByteArray();
        String base64ImageString = DatatypeConverter.printBase64Binary(fileBytes);
        return new Base64DecodedMultipartFile(BASE64_PREFIX + base64ImageString);
    }

    @Test
    void deleteExistingPhoto_getSuccess() throws IOException {
        /* Given: There is a photo for the given user */
        createTestUserImage();

        /* When: A request is received to delete the user's photo */
        DeleteUserProfilePhotoRequest request = DeleteUserProfilePhotoRequest.newBuilder()
                .setUserId(TEST_PHOTO_USERID)
                .build();
        StreamObserver<DeleteUserProfilePhotoResponse> observer = mock(StreamObserver.class);
        userAccountServerService.deleteUserProfilePhoto(request, observer);

        /* Then: The response will be a success */
        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<DeleteUserProfilePhotoResponse> captor = ArgumentCaptor.forClass(
                DeleteUserProfilePhotoResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        DeleteUserProfilePhotoResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
    }

    @Test
    void addNewPhoto_getSuccessAndFileExists() throws IOException {
        /* Given: There is no photo for the given user */
        Files.deleteIfExists(profileImageFolder.resolve(TEST_PHOTO_NAME));

        /* When: a request is received to create a photo for the user */
        // This code is just copied from UserAccountClientService.java
        MultipartFile file = getTestUserImageMultipartFile(100);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            System.out.println(uploadRequest.getFileContent());
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file exists and the response will be a success */
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME);
        assertTrue(pfp.exists());
        pfp.delete();

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.SUCCESS, response.getStatus());
    }

    @Test
    void replaceExistingPhoto_getSuccessAndFileDifferent() throws IOException {
        /* Given: There is an existing photo for the given user */
        Files.deleteIfExists(profileImageFolder.resolve(TEST_PHOTO_NAME));
        createTestUserImage();
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME);
        assertTrue(pfp.exists());
        Long originalFileSize = pfp.length(); // save this to check that it is different

        /* When: a request is received to create a photo for the user */
        // This code is just copied from UserAccountClientService.java
        // Note that this image is 200x200, so will be larger than the one created by createTestUserImage()!
        MultipartFile file = getTestUserImageMultipartFile(200);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            System.out.println(uploadRequest.getFileContent());
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file will be different (different size), and the response will be a success */
        assertTrue(pfp.exists());
        assertNotEquals(originalFileSize, pfp.length());
        pfp.delete();

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.SUCCESS, response.getStatus());
    }
}
