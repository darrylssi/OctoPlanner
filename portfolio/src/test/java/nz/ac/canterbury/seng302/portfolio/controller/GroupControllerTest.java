package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.forms.GroupForm;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.verify;
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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupClientService groupClientService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserAccountClientService userAccountClientService;

    static final int USER_ID = 1;
    static final int GROUP_ID = 2;
    private GroupForm groupForm;                                // Initialises the group form object

    @BeforeEach
    void setup() {
        // Creates and sets the details to group form
        groupForm = new GroupForm();
        groupForm.setShortName("Test Group");
        groupForm.setLongName("Test Project Group 2022");

        // Creates and sets the details to the new project
        Project parentProject = new Project("Project 2022", "Test Parent Project", "2022-01-01", "2022-12-31");

        // Define the user for the tests; this is done to provide access to the edit page
        UserResponse testUser = UserResponse.newBuilder()
                .setUsername("test_user")
                .setFirstName("Testy")
                .setMiddleName("")
                .setLastName("McUserFace")
                .setNickname("Test")
                .setBio("")
                .setPersonalPronouns("they/them")
                .setEmail("test@user.site")
                .setProfileImagePath("")
                .setId(USER_ID)
                .build();
        GroupDetailsResponse testGroup = GroupDetailsResponse.newBuilder()
                .addMembers(testUser)
                .build();
        when(groupClientService.getGroupDetails(GROUP_ID)).thenReturn(testGroup);
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteGroupAsTeacher_get200Response() throws Exception {
        DeleteGroupResponse deleteGroupResponse = DeleteGroupResponse.newBuilder().setIsSuccess(true).setMessage("Group deleted.").build();
        when(groupClientService.deleteGroup(5)).thenReturn(deleteGroupResponse);

        mockMvc.perform(delete("/groups/5/remove-group"))
                .andExpect(status().isOk())
                .andExpect(content().string("Group deleted."));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteTeachingStaffGroupAsTeacher_get400Response() throws Exception {
        DeleteGroupResponse deleteGroupResponse = DeleteGroupResponse.newBuilder().setIsSuccess(false).setMessage("Default group cannot be deleted").build();
        when(groupClientService.deleteGroup(0)).thenReturn(deleteGroupResponse);

        mockMvc.perform(delete("/groups/0/remove-group"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Default group cannot be deleted"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteMembersWithoutGroupAsTeacher_get400Response() throws Exception {
        DeleteGroupResponse deleteGroupResponse = DeleteGroupResponse.newBuilder().setIsSuccess(false).setMessage("Default group cannot be deleted").build();
        when(groupClientService.deleteGroup(1)).thenReturn(deleteGroupResponse);

        mockMvc.perform(delete("/groups/1/remove-group"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Default group cannot be deleted"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteNonexistentGroupAsTeacher_get404Response() throws Exception {
        DeleteGroupResponse deleteGroupResponse = DeleteGroupResponse.newBuilder().setIsSuccess(false).setMessage(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "5").build();
        when(groupClientService.deleteGroup(5)).thenReturn(deleteGroupResponse);

        mockMvc.perform(delete("/groups/5/remove-group"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "5"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteGroupAsStudent_get403Response() throws Exception {
        mockMvc.perform(delete("/groups/5/remove-group"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addValidGroupAsTeacher_get200Response() throws Exception {
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group 1")
                        .param("longName", "Test Project Group"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankShortNameGroupAsTeacher_get400Response() throws Exception {
        String resultString = mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "")
                        .param("longName", "Test Project Group 2022"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Short name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Short name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankLongNameGroupAsTeacher_get400Response() throws Exception {
        String resultString = mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group")
                        .param("longName", ""))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Long name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Long name must be between 2-128 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidShortNameGroupAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group 🏋")
                        .param("longName", "Test Project Group 2022"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidLongNameGroupAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group")
                        .param("longName", "Test Project Group 2022 🏋"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addUsersToGroupAsTeacher_get200Response() throws Exception{
        AddGroupMembersResponse addGroupMembersResponse = AddGroupMembersResponse.newBuilder().setIsSuccess(true).setMessage("2 users added to group 1").build();
        when(groupClientService.addGroupMembers(1, List.of(1, 2))).thenReturn(addGroupMembersResponse);

        mockMvc.perform(post("/groups/1/add-members")
                .param("user_id", "1")
                .param("user_id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("2 users added to group 1"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addUsersToNonexistentGroupAsTeacher_get404Response() throws Exception{
        AddGroupMembersResponse addGroupMembersResponse = AddGroupMembersResponse.newBuilder().setIsSuccess(false).setMessage(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "5").build();
        when(groupClientService.addGroupMembers(5, List.of(1, 2))).thenReturn(addGroupMembersResponse);

        mockMvc.perform(post("/groups/5/add-members")
                        .param("user_id", "1")
                        .param("user_id", "2"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "5"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addMembersAsStudent_forbidden() throws Exception {
        mockMvc.perform(post("/groups/1/add-members")
                        .param("user_id", "1")
                        .param("user_id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addGroupAsStudent_forbidden() throws Exception {
        this.mockMvc.perform(post("/project/0/add-group"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(value = STUDENT, id = USER_ID)
    void editValidGroupAsMemberOfGroup_get200Response() throws Exception {
        String shortName = "Test Valid Short Name";
        String longName = "Test Valid Long Name";
        // Given: The group service returns a success
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Group edited successfully")
                .build();
        when(groupClientService.modifyGroupDetails(GROUP_ID,
                shortName,longName)).thenReturn(response);
        // When: We attempt to edit a group
        // Then: The request returns an OK (200) status and the message from the service is displayed
        mockMvc.perform(post("/project/0/edit-group/" + GROUP_ID)
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isOk())
                .andExpect(content().string("Group edited successfully"));
        verify(groupClientService).modifyGroupDetails(GROUP_ID, shortName, longName);
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editValidGroupAsTeacherWhoIsNotInGroup_get200Response() throws Exception {
        String shortName = "Test Valid Short Name";
        String longName = "Test Valid Long Name";
        // Given: The group service returns a success
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Group edited successfully")
                .build();
        when(groupClientService.modifyGroupDetails(GROUP_ID,
                shortName,longName)).thenReturn(response);
        // When: We attempt to edit a group
        // Then: The request returns an OK (200) status and the message from the service is displayed
        mockMvc.perform(post("/project/0/edit-group/" + GROUP_ID)
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isOk())
                .andExpect(content().string("Group edited successfully"));
        verify(groupClientService).modifyGroupDetails(GROUP_ID, shortName, longName);
    }

    /**
     * This test case may happen when attempting to edit default groups, even if the short and long names are valid
     */
    @Test
    @WithMockPrincipal(value = STUDENT, id = USER_ID)
    void editInvalidGroupAsMemberOfGroup_get400Response() throws Exception {
        String shortName = "Test Valid Short Name";
        String longName = "Test Valid Long Name";
        // Given: The group service returns a failure
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("Group could not be edited")
                .build();
        when(groupClientService.modifyGroupDetails(GROUP_ID,
                shortName,longName)).thenReturn(response);
        // When: We attempt to edit a group
        // Then: The request returns a Bad Request (400) status and the message from the service is displayed
        mockMvc.perform(post("/project/0/edit-group/" + GROUP_ID)
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Group could not be edited"));
        verify(groupClientService).modifyGroupDetails(GROUP_ID, shortName, longName);
    }

    /**
     * This test case may happen when attempting to edit default groups, even if the short and long names are valid
     */
    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidGroupAsTeacherWhoIsNotInGroup_get400Response() throws Exception {
        String shortName = "Test Valid Short Name";
        String longName = "Test Valid Long Name";
        // Given: The group service returns a failure
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("Group could not be edited")
                .build();
        when(groupClientService.modifyGroupDetails(GROUP_ID,
                shortName,longName)).thenReturn(response);
        // When: We attempt to edit a group
        // Then: The request returns a Bad Request (400) status and the message from the service is displayed
        mockMvc.perform(post("/project/0/edit-group/" + GROUP_ID)
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Group could not be edited"));
        verify(groupClientService).modifyGroupDetails(GROUP_ID, shortName, longName);
    }

    @Test
    @WithMockPrincipal(value = STUDENT)
    void editGroupWhenNotMemberOfGroupAndNotTeacher_forbidden() throws Exception {
        // Default ID is -1
        this.mockMvc.perform(post("/project/0/edit-group/" + GROUP_ID))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not a part of this group"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void removeUsersFromGroupAsTeacher_get200Response() throws Exception {
        RemoveGroupMembersResponse removeGroupMembersResponse = RemoveGroupMembersResponse.newBuilder().setIsSuccess(true).setMessage("2 users removed from group 3").build();
        when(groupClientService.removeGroupMembers(3, List.of(3, 8))).thenReturn(removeGroupMembersResponse);

        mockMvc.perform(delete("/groups/3/remove-members")
                        .param("user_id", "3")
                        .param("user_id", "8"))
                .andExpect(status().isOk())
                .andExpect(content().string("2 users removed from group 3"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void removeUsersFromNonexistentGroupAsTeacher_get404Response() throws Exception {
        RemoveGroupMembersResponse removeGroupMembersResponse = RemoveGroupMembersResponse.newBuilder().setIsSuccess(false).setMessage(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "6").build();
        when(groupClientService.removeGroupMembers(6, List.of(3, 8))).thenReturn(removeGroupMembersResponse);

        mockMvc.perform(delete("/groups/6/remove-members")
                        .param("user_id", "3")
                        .param("user_id", "8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(GlobalVars.GROUP_NOT_FOUND_ERROR_MESSAGE + "6"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void removeUsersFromGroupAsStudent_forbidden() throws Exception {
        mockMvc.perform(delete("/groups/1/remove-members")
                        .param("user_id", "2")
                        .param("user_id", "51"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
}
