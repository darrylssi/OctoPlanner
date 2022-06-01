package nz.ac.canterbury.seng302.portfolio;
import io.grpc.Status;
import io.grpc.StatusException;
import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.ListUsersController;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ListUsersController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleEditingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // @MockBean is used over @Mock as for mockmvc tests we require Spring context
    UserAccountClientService userAccountClientService;

    void mockUserWithRole(int id, UserRole role) throws StatusException {
        Mockito.when(userAccountClientService.addRoleToUser(id, role)).thenReturn(false);
        Mockito.when(userAccountClientService.removeRoleFromUser(id, role)).thenReturn(true);
    }

    void mockUserWithoutRole(int id, UserRole role) throws StatusException {
        Mockito.when(userAccountClientService.addRoleToUser(id, role)).thenReturn(true);
        Mockito.when(userAccountClientService.removeRoleFromUser(id, role)).thenReturn(false);
    }

    void mockNonexistentUser(int id, UserRole role) throws StatusException {
        Mockito.when(userAccountClientService.addRoleToUser(id, role)).thenThrow(new StatusException(Status.NOT_FOUND));
        Mockito.when(userAccountClientService.removeRoleFromUser(id, role)).thenThrow(new StatusException(Status.NOT_FOUND));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addRoleToUserAsTeacher_get200Response() throws Exception {
        // user does not have the role TEACHER
        mockUserWithoutRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/add-role/TEACHER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role TEACHER added"));
    }

    @Test
    @WithMockPrincipal(COURSE_ADMINISTRATOR)
    void addRoleToUserAsAdmin_get200Response() throws Exception {
        // user does not have the role TEACHER
        mockUserWithoutRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/add-role/TEACHER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role TEACHER added"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addRoleToUserAsStudent_get401Response() throws Exception {
        // user does not have the role TEACHER
        mockUserWithoutRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/add-role/TEACHER"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised to edit roles"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addDuplicateRoleToUserAsTeacher_get400Response() throws Exception {
        // user has the role TEACHER
        mockUserWithRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/add-role/TEACHER"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role not added"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void removeRoleFromUserAsTeacher_get200Response() throws Exception {
        // user has the role TEACHER
        mockUserWithRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/remove-role/TEACHER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role TEACHER removed"));
    }

    @Test
    @WithMockPrincipal(COURSE_ADMINISTRATOR)
    void removeRoleFromUserAsAdmin_get200Response() throws Exception {
        // user has the role TEACHER
        mockUserWithRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/remove-role/TEACHER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role TEACHER removed"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void removeRoleFromUserAsStudent_get401Response() throws Exception {
        // user has the role TEACHER
        mockUserWithRole(1, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/1/remove-role/TEACHER"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised to edit roles"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void removeNonexistentRoleFromUserAsTeacher_get400Response() throws Exception {
        /*
        The UserAccountClientService returns the same result when attempting to remove a nonexistent role
        as when attempting to remove the last role of a user, and as we are mocking the UserAccountClientService,
        there is no functional difference between those tests. Therefore, we only have a single test.
        Unit testing of removing the final role from a user is done in the idp.
         */

        // user does not have the role TEACHER
        mockUserWithoutRole(1, TEACHER);
        // removed the role TEACHER from the user
        mockMvc.perform(patch("/users/1/remove-role/TEACHER"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role not removed"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void removeRoleFromNonexistentUserAsTeacher_get404Response() throws Exception {
        // user does not exist
        mockNonexistentUser(2, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/2/remove-role/TEACHER"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid User Id"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addRoleToNonexistentUserAsTeacher_get404Response() throws Exception {
        // user does not exist
        mockNonexistentUser(2, TEACHER);
        // add the role TEACHER to the user
        mockMvc.perform(patch("/users/2/add-role/TEACHER"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid User Id"));
    }
}
