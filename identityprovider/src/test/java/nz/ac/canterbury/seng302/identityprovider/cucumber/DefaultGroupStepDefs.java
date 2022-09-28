package nz.ac.canterbury.seng302.identityprovider.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.service.GroupServerService;
import nz.ac.canterbury.seng302.identityprovider.service.GroupService;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.MEMBERS_WITHOUT_GROUPS_ID;
import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Class containing the step definitions for the teaching_staff_group and members_without_a_group Cucumber features
 */
@SpringBootTest
public class DefaultGroupStepDefs {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupServerService groupServerService;

    private Group testTeacherGroup = null;
    private Group testMembersWithoutAGroup = null;
    private DeleteGroupResponse response;

    @When("I try to access the Teaching Staff group")
    public void iTryToAccessTheTeachingStaffGroup() {
        testTeacherGroup = groupService.getGroup(TEACHER_GROUP_ID);
    }

    @When("I try to access the Members Without A Group group")
    public void iTryToAccessTheMembersWithoutAGroupGroup() {
        testMembersWithoutAGroup = groupService.getGroup(MEMBERS_WITHOUT_GROUPS_ID);
    }

    @Then("Teaching Staff can be accessed")
    public void TeachingStaffCanBeAccessed() {
        assertNotNull(testTeacherGroup);
    }

    @Then("Members Without A Group can be accessed")
    public void MembersWithoutAGroupCanBeAccessed() {
        assertNotNull(testMembersWithoutAGroup);
    }

    @When("I try to delete the Teaching Staff group")
    public void iTryToDeleteTheTeachingStaffGroup() {
        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(TEACHER_GROUP_ID)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        response = captor.getValue();
    }

    @When("I try to delete the Members Without A Group group")
    public void iTryToDeleteTheMembersWithoutAGroupGroup() {
        StreamObserver<DeleteGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<DeleteGroupResponse> captor = ArgumentCaptor.forClass(DeleteGroupResponse.class);
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(MEMBERS_WITHOUT_GROUPS_ID)
                .build();
        groupServerService.deleteGroup(request, observer);

        verify(observer, times(1)).onCompleted();
        verify(observer, times(1)).onNext(captor.capture());
        response = captor.getValue();
    }

    @Then("the request to delete Teaching Staff fails")
    public void theRequestToDeleteTeachingStaffFails() {
        assertFalse(response.getIsSuccess());
        assertEquals("The group \"Teaching Staff\" cannot be deleted", response.getMessage());
    }

    @Then("the request to delete Members Without A Group fails")
    public void theRequestToDeleteMembersWithoutAGroupFails() {
        assertFalse(response.getIsSuccess());
        assertEquals("The group \"Members Without A Group\" cannot be deleted", response.getMessage());
    }
}
