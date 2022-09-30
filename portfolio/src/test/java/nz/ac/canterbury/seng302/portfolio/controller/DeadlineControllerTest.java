package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.*;
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for post and delete requests for deadlines handled by the deadline controller
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DeadlineController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeadlineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProjectService projectService;

    @MockBean
    DeadlineService deadlineService;

    @MockBean
    private UserAccountClientService userAccountClientService;

    static Project parentProject = new Project("Project 2022", "This is the project", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-31"));

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteDeadlineAsTeacher_get200Response() throws Exception {
        Mockito.doNothing().when(deadlineService).deleteDeadline(anyInt());

        // deletes the deadline at valid id 1
        mockMvc.perform(delete("/delete-deadline/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deadline deleted."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteDeadlineAsStudent_get403Response() throws Exception {
        Mockito.doNothing().when(deadlineService).deleteDeadline(anyInt());

        // As the user is STUDENT, the access is unauthorized. Therefore, the deadline is not deleted at id 1, and
        // appropriate error is displayed
        mockMvc.perform(delete("/delete-deadline/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addValidDeadlineAsTeacher_get200Response() throws Exception {
        // Creates a new deadline and sets the values
        Deadline deadline = new Deadline("New Deadline", "This is a deadline", DateUtils.toDateTime("2022-09-09 12:00"));
        deadline.setId(1);

        when(deadlineService.saveDeadline(any())).thenReturn(deadline);
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends the valid deadline data, so the deadline is saved
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline")
                        .param("description", "This is a deadline")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankDeadlineNameAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends the empty deadline name, so the appropriate error is shown
        String resultString = mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidDeadlineNameAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends inappropriate deadline name, so the appropriate error is shown
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline 🤯🏋️")
                        .param("description", "This is a deadline")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidDeadlineDescriptionAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline")
                        .param("description", "This is an invalid description 😂")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description can only have letters, numbers, spaces and punctuation"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankDeadlineDateAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends the empty deadline date, so the appropriate error is shown
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline")
                        .param("description", "This is a deadline")
                        .param("startDate", "")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addEarlyDeadlineDateAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Send the deadline date which is less than project start date, so the appropriate error is shown
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline")
                        .param("description", "This is a deadline")
                        .param("startDate", "2021-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deadline date must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addLateDeadlineDateAsTeacher_get400Response() throws Exception {
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends the deadline date which is more than project end date, so appropriate error is shown
        mockMvc.perform(post("/project/0/add-deadline")
                        .param("name", "New Deadline")
                        .param("description", "This is a deadline")
                        .param("startDate", "2023-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deadline date must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addDeadlineAsStudent_get403Response() throws Exception {
        // As the user is STUDENT, so they do not have permission to add deadline form. Therefore, appropriate error
        // is shown
        mockMvc.perform(post("/project/0/add-deadline"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editValidDeadlineAsTeacher_get200Response() throws Exception {
        // Creates a new deadline and sets the values
        Deadline deadline = new Deadline("New Deadline", "This is a deadline", DateUtils.toDateTime("2022-09-09 12:00"));
        deadline.setId(1);

        when(deadlineService.saveDeadline(any())).thenReturn(deadline);
        when(deadlineService.getDeadlineById(1)).thenReturn(deadline);
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // Sends the appropriate new data, so the deadline is updated
        mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("name", "New Deadline")
                        .param("description", "This is a new deadline")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-09")
                        .param("endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidDeadlineNameAsTeacher_get400Response() throws Exception {
        // Creates a new deadline and sets the values
        Deadline deadline = new Deadline("New Deadline", "This is a deadline", DateUtils.toDateTime("2022-09-09 12:00"));
        deadline.setId(1);

        when(deadlineService.saveDeadline(any())).thenReturn(deadline);
        when(deadlineService.getDeadlineById(1)).thenReturn(deadline);
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // As the deadline has an inappropriate name, so appropriate error is shown
        mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("name", "🤯")
                        .param("description", "This is a deadline")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidDeadlineDescriptionAsTeacher_get400Response() throws Exception {
        // Creates a new deadline and sets the values
        Deadline deadline = new Deadline("New Deadline", "This is a deadline", DateUtils.toDateTime("2022-09-09 12:00"));
        deadline.setId(1);

        when(deadlineService.saveDeadline(any())).thenReturn(deadline);
        when(deadlineService.getDeadlineById(1)).thenReturn(deadline);
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // As the deadline has an inappropriate description, so appropriate error is shown
        mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("name", "Deadline")
                        .param("description", "This is a deadline 😠‼️")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description can only have letters, numbers, spaces and punctuation"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editMissingDeadlineId_throw404() throws Exception {
        // Creates a new deadline and sets the values
        Deadline deadline = new Deadline("New Deadline", "This is a deadline", DateUtils.toDateTime("2022-09-09 12:00"));
        deadline.setId(2);

        when(deadlineService.saveDeadline(any())).thenReturn(deadline);
        when(deadlineService.getDeadlineById(anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadline not found"));
        when(projectService.getProjectById(0)).thenReturn(parentProject);

        // As the deadline does not exist at id 1, therefore it returns appropriate 404 error
        this.mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("name", "New Deadline")
                        .param("description", "")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Deadline not found")));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void editDeadlineAsStudent_get403Response() throws Exception {
        // As the user is STUDENT, so they do not have permission to edit deadline form. Therefore, appropriate error
        // is shown
        mockMvc.perform(post("/project/0/edit-deadline/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
}
