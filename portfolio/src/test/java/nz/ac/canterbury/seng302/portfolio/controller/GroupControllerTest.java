package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersResponse;
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

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static org.mockito.Mockito.when;
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

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addUsersToGroupAsTeacher_get200Response() throws Exception{
        AddGroupMembersResponse addGroupMembersResponse = AddGroupMembersResponse.newBuilder().setIsSuccess(true).setMessage("2 users added to group 1").build();
        when(groupClientService.addGroupMembers(1, List.of(1, 2))).thenReturn(addGroupMembersResponse);

        mvc.perform(post("/groups/1/add-members")
                .param("user_id", "1")
                .param("user_id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("2 users added to group 1"));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addUsersToNonexistentGroupAsTeacher_get404Response() throws Exception{
        AddGroupMembersResponse addGroupMembersResponse = AddGroupMembersResponse.newBuilder().setIsSuccess(false).setMessage("There is no group with id 5").build();
        when(groupClientService.addGroupMembers(5, List.of(1, 2))).thenReturn(addGroupMembersResponse);

        mvc.perform(post("/groups/5/add-members")
                        .param("user_id", "1")
                        .param("user_id", "2"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("There is no group with id 5"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void editEventAsStudent_forbidden() throws Exception {
        mvc.perform(post("/groups/1/add-members")
                .param("user_id", "1")
                .param("user_id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

}
