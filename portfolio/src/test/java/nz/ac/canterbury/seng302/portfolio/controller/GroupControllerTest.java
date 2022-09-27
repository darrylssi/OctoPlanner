package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.RemoveGroupMembersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for group endpoints
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupControllerTest {
    @MockBean
    GroupClientService groupClientService;

    @MockBean
    UserAccountClientService userAccountClientService;

    @Autowired
    MockMvc mvc;

    // remove users as teacher, remove from nonexistent group, remove as student

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void removeUsersFromGroupAsTeacher_get200Response() throws Exception {
        RemoveGroupMembersResponse removeGroupMembersResponse = RemoveGroupMembersResponse.newBuilder().setIsSuccess(true).setMessage("2 users removed from group 3").build();
        when(groupClientService.removeGroupMembers(3, List.of(3, 8))).thenReturn(removeGroupMembersResponse);

        mvc.perform(delete("/groups/3/remove-members")
                        .param("user_id", "3")
                        .param("user_id", "8"))
                .andExpect(status().isOk())
                .andExpect(content().string("2 users removed from group 3"));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void removeUsersFromNonexistentGroupAsTeacher_get404Response() throws Exception {
        RemoveGroupMembersResponse removeGroupMembersResponse = RemoveGroupMembersResponse.newBuilder().setIsSuccess(false).setMessage("There is no group with id 6").build();
        when(groupClientService.removeGroupMembers(6, List.of(3, 8))).thenReturn(removeGroupMembersResponse);

        mvc.perform(delete("/groups/6/remove-members")
                .param("user_id", "3")
                .param("user_id", "8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("There is no group with id 6"));
    }

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void removeUsersFromGroupAsStudent_forbidden() throws Exception {
        mvc.perform(delete("/groups/1/remove-members")
                .param("user_id", "1")
                .param("user_id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
}
