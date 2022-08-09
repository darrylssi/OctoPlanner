package nz.ac.canterbury.seng302.identityprovider;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import nz.ac.canterbury.seng302.identityprovider.service.UserService;

@SpringBootTest
@DirtiesContext
class IdentityproviderApplicationTests {

    @Autowired
    UserService userService;

    @Test()
    void contextLoads() {
        assertNotNull(userService);
    }

}
