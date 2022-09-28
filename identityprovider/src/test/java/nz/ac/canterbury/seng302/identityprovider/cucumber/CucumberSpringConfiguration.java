package nz.ac.canterbury.seng302.identityprovider.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.identityprovider.IdentityProviderApplication;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = IdentityProviderApplication.class)
public class CucumberSpringConfiguration { }
