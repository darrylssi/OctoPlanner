package nz.ac.canterbury.seng302.identityprovider;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig(classes = UserAccountServiceTestConfiguration.class)
public class UserAccountServiceTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

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
}
