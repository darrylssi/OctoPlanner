package nz.ac.canterbury.seng302.portfolio.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockPrincipal(value=TEACHER, id=EditUserControllerTest.USER_ID)
public class EditUserControllerTest {

    static final int USER_ID=1;
    final Logger logger = LoggerFactory.getLogger(EditUserControllerTest.class);

    // The URL which the controller handles requests on.
    private static final String EDIT_USER_URL = "/users/" + USER_ID + "/edit";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountClientService userAccountService;

    @BeforeEach
    private void setup() {
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
        when(userAccountService.getUserAccountById(USER_ID)).thenReturn(testUser);
    }

    @Test
    public void deletePhotoServiceGivesFailure_thenShowMessage() throws Exception {
        /* Given: The delete service returns a failure */
        DeleteUserProfilePhotoResponse noPhotoResponse = DeleteUserProfilePhotoResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("No profile photo uploaded")
                .build();
        when(userAccountService.deleteUserProfilePhoto(USER_ID)).thenReturn(noPhotoResponse);
        /**
         * When: The controller attempts to delete a photo
         * Then: The page displays the message from the service
         */
        this.mockMvc.perform(post(EDIT_USER_URL))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No profile photo uploaded")));
        verify(userAccountService).deleteUserProfilePhoto(USER_ID);
    }

    @Test
    public void deleteExistingPhoto_getRedirected() throws Exception {
        /* Given: The delete service returns a success */
        DeleteUserProfilePhotoResponse successResponse = DeleteUserProfilePhotoResponse.newBuilder()
                .setIsSuccess(true)
                .build();
        when(userAccountService.deleteUserProfilePhoto(USER_ID)).thenReturn(successResponse);

        /**
         * When: The controller attempts to delete a photo
         * Then: The user is redirected to their profile
         */
        this.mockMvc.perform(post(EDIT_USER_URL))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("../" + USER_ID));
        verify(userAccountService).deleteUserProfilePhoto(USER_ID);
    }
}
