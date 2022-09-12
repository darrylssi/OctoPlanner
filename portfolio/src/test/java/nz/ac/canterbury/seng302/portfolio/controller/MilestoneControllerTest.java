package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.MilestoneService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for post and delete requests for milestones handled by the milestone controller
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MilestoneController.class)
@AutoConfigureMockMvc(addFilters = false)
class MilestoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MilestoneService milestoneService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserAccountClientService userAccountClientService;

    static Project parentProject = new Project("Project 2022", "This is the project", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-31"));

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteMilestoneAsTeacher_get200Response() throws Exception {
        Mockito.doNothing().when(milestoneService).deleteMilestone(1);
        mockMvc.perform(delete("/delete-milestone/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Milestone deleted."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteMilestoneAsStudent_get403Response() throws Exception {
        Mockito.doNothing().when(milestoneService).deleteMilestone(1);
        mockMvc.perform(delete("/delete-milestone/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addValidMilestoneAsTeacher_get200Response() throws Exception {
        Milestone milestone = new Milestone("New Milestone", "This is a milestone", DateUtils.toDate("2022-09-09"));
        milestone.setId(1);
        Mockito.when(milestoneService.saveMilestone(any())).thenReturn(milestone);
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "New Milestone")
                        .param("description", "This is a milestone")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankNameMilestoneAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        String resultString = mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInValidNameMilestoneAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "New Milestone!")
                        .param("description", "This is a milestone")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have alphanumeric and . - _ characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankDateMilestoneAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "New Milestone")
                        .param("description", "This is a milestone")
                        .param("startDate", "")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addEarlyDateMilestoneAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "New Milestone")
                        .param("description", "This is a milestone")
                        .param("startDate", "2021-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Milestone dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addLateDateMilestoneAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-milestone")
                        .param("name", "New Milestone")
                        .param("description", "This is a milestone")
                        .param("startDate", "2023-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Milestone dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addMilestoneAsStudent_get403Response() throws Exception {
        mockMvc.perform(post("/project/0/add-milestone"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

}
