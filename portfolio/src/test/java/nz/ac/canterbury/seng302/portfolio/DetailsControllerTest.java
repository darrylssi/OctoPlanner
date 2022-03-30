package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class DetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProjectMissingId_throw404() throws Exception {
        AuthState state = AuthState.newBuilder().build();
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/delete-sprint/999").requestAttr("principal", state))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised."));

    }
}
