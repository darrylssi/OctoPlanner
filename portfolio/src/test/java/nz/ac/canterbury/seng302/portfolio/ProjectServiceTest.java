package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    private Project project1;

    @BeforeEach
    void setUp() {
        project1 = new Project("Name", "Description", "02/Mar/2021", "03/Mar/2022");
    }

    @Test
    void getProjectValidId() {
        when(projectRepository.findProjectById(1))
                .thenReturn(project1);

        assertThat(projectService.getProjectById(1)).isEqualTo(project1);
    }

    @Test
    void getProjectInvalidId() {
        when(projectRepository.findProjectById(2))
                .thenReturn(null);
        assertThat(projectService.getProjectById(2)).isEqualTo(null);
    }

    @Test
    void saveProject() {
        when(projectRepository.save(project1)).thenReturn(project1);
        projectService.saveProject(project1);
        verify(projectRepository, times(1)).save(project1);
    }

}
