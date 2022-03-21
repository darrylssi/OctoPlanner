package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAccountServiceTestConfiguration {

    @Bean
    UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    UserAccountServerService userAccountServerService() {
        return new UserAccountServerService();
    }
}
