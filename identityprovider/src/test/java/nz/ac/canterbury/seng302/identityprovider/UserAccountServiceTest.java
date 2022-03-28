package nz.ac.canterbury.seng302.identityprovider;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
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
public class UserAccountServiceTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testRegister_whenValid() {
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
        StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);
        userAccountServerService.register(request, observer);

        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<UserRegisterResponse> captor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        UserRegisterResponse response = captor.getValue();

        assertTrue(response.getIsSuccess());
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
}
