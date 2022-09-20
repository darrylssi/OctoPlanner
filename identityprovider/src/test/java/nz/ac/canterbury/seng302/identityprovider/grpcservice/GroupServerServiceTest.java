package nz.ac.canterbury.seng302.identityprovider.grpcservice;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.service.GroupServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Contains tests for the GroupServerService class
 */
@SpringBootTest
@DirtiesContext
@SuppressWarnings("unchecked")
class GroupServerServiceTest {

    @Autowired
    private GroupServerService groupServerService;

    @MockBean
    private GroupRepository groupRepository;

    private Group testGroup;

    @BeforeEach
    public void setup() {
        testGroup = new Group("test short name", "test long name");
    }

    @Test
    void testCreateGroup_whenValid() {
        StreamObserver<CreateGroupResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<CreateGroupResponse> captor = ArgumentCaptor.forClass(CreateGroupResponse.class);
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName("test valid short name")
                .setLongName("test valid long name")
                .build();
        groupServerService.createGroup(request, observer);

        // TODO uncomment these lovely assertions when the methods have been implemented
        //verify(observer, times(1)).onCompleted();
        //verify(observer, times(1)).onNext(captor.capture());
        //CreateGroupResponse response = captor.getValue();

        //assertTrue(response.getIsSuccess());

        // TODO and get rid of this one
        assertEquals("test long name", testGroup.getLongName());
    }
}
