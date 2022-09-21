package nz.ac.canterbury.seng302.portfolio;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;

@SpringBootTest
class PortfolioApplicationTests {

    @Autowired
    ProjectService projectService;

    @Test
    void contextLoads() {
        assertNotNull(projectService);
    }

}
