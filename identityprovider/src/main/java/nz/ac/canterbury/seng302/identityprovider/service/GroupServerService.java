package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains server-side methods for dealing with groups in the IDP, such as
 * methods dealing with creating, updating, deleting groups and adding/removing members from groups
 */
@GrpcService
public class GroupServerService extends GroupsServiceGrpc.GroupsServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GroupServerService.class);

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        logger.info("createGroup() has been called");
        // TODO implement this
    }

    @Override
    public void addGroupMembers(AddGroupMembersRequest request, StreamObserver<AddGroupMembersResponse> responseObserver) {
        logger.info("addGroupMembers() has been called");
        // TODO implement this
    }

    @Override
    public void removeGroupMembers(RemoveGroupMembersRequest request, StreamObserver<RemoveGroupMembersResponse> responseObserver) {
        logger.info("removeGroupMembers() has been called");
        // TODO implement this
    }

    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        logger.info("modifyGroupDetails() has been called");
        // TODO implement this
    }

    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        logger.info("deleteGroup() has been called");
        // TODO implement this
    }

    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GetGroupDetailsResponse> responseObserver) {
        logger.info("getGroupDetails() has been called");
        // TODO implement this
    }
}
