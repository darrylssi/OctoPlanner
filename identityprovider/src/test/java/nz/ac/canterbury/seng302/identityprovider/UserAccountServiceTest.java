package nz.ac.canterbury.seng302.identityprovider;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
public class UserAccountServiceTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    
    @BeforeEach
    public void setup() {
        testUser = new User("testUser", "testPassword", "testFirstName",
                "testMiddleName", "testLastName", "testNickname",
                "testBio", "testPronouns", "testEmail@example.com");
        userRepository.save(testUser);
    }

    @Test
    void testValidRegister() {
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
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
    }

    @Test
    void testEdit_whenValid() {
        when(userRepository.findById(1))
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
                .thenReturn(Optional.ofNullable((testUser)));

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
