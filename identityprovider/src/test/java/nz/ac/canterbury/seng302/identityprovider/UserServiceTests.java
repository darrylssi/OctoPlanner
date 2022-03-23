package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

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

    @AfterEach
    public void tearDown() {
        userRepository.delete(testUser);
    }

    @Test
    public void searchByUsername() {
        when(userRepository.findByUsername("testUser"))
                .thenReturn((testUser));
        assertThat(userService.getUserByUsername("testUser")).isEqualTo(testUser);
    }

    @Test
    public void addRole() {
        userService.addRoleToUser(testUser.getID(), Role.TEACHER);
        User userAgain = userRepository.findByUsername("testUser");
        System.out.println(testUser.getRoles());
        System.out.println(userAgain.getRoles());
            
    }

}
