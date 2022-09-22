package nz.ac.canterbury.seng302.identityprovider.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber-report.html"},
        glue = {"nz.ac.canterbury.seng302.identityprovider.cucumber"},
        features = {"src/test/resources/features"}
)
public class RunCucumberTest {

}