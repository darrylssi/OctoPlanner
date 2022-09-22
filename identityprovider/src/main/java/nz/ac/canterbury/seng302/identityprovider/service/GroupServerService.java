package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class contains server-side methods for dealing with groups in the IDP, such as
 * methods dealing with creating, updating, deleting groups and adding/removing members from groups
 */
@GrpcService
public class GroupServerService extends GroupsServiceGrpc.GroupsServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GroupServerService.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    /**
     * Creates a new group, adds it to the database and returns a CreateGroupResponse
     * @param request An object containing all the details of the group to create
     */
    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        logger.info("createGroup() has been called");
        CreateGroupResponse.Builder reply = CreateGroupResponse.newBuilder();

        Group group = new Group(request.getShortName(), request.getLongName());
        List<ValidationError> errors = getValidationErrors(group);

        if(!errors.isEmpty()) { // If there are errors in the request
            for (ValidationError error : errors) {
                logger.error("Create new group: {} - {}",
                        error.getFieldName(), error.getErrorText());
            }
            reply
                    .setIsSuccess(false)
                    .setMessage("Group could not be created")
                    .addAllValidationErrors(errors);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }
        groupRepository.save(group);
        reply
                .setIsSuccess(true)
                .setNewGroupId(group.getId())
                .setMessage("Group created successfully");
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Adds a list of users to an existing group and returns an AddGroupMembersResponse
     * @param request An object containing the details of the group to add to and the list of users to add
     */
    @Override
    public void addGroupMembers(AddGroupMembersRequest request, StreamObserver<AddGroupMembersResponse> responseObserver) {
        logger.info("addGroupMembers() has been called");
        AddGroupMembersResponse.Builder reply = AddGroupMembersResponse.newBuilder();
        int numUsersAdded;

        try {
            numUsersAdded = groupService.addUsersToGroup(request.getGroupId(), request.getUserIdsList());
        } catch (NoSuchElementException e) {
            reply
                    .setIsSuccess(false)
                    .setMessage(e.getMessage());
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }
        reply
                .setIsSuccess(true)
                .setMessage(numUsersAdded + " users added to group " + request.getGroupId());

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Removes a list of users from an existing group and returns a RemoveGroupMembersResponse
     * @param request An object containing the details of the group to remove from and the list of users to remove
     */
    @Override
    public void removeGroupMembers(RemoveGroupMembersRequest request, StreamObserver<RemoveGroupMembersResponse> responseObserver) {
        logger.info("removeGroupMembers() has been called");
        RemoveGroupMembersResponse.Builder reply = RemoveGroupMembersResponse.newBuilder();
        int numUsersRemoved;

        try {
            numUsersRemoved = groupService.removeUsersFromGroup(request.getGroupId(), request.getUserIdsList());
        } catch (NoSuchElementException e) {
            reply
                    .setIsSuccess(false)
                    .setMessage(e.getMessage());
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }
        reply
                .setIsSuccess(true)
                .setMessage(numUsersRemoved+ " users removed from group " + request.getGroupId());

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        logger.info("modifyGroupDetails() has been called");
        // TODO implement this
    }

    /**
     * Deletes a group if it exists and returns a DeleteGroupResponse
     * @param request An object containing the id of the group to delete
     */
    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        // TODO this should not allow deleting Teaching Staff and Members Without A Group (once implemented)
        logger.info("deleteGroup() has been called");
        DeleteGroupResponse.Builder reply = DeleteGroupResponse.newBuilder();

        Group group;
        try {
            group = groupService.getGroup(request.getGroupId());
        } catch (NoSuchElementException e) {
            reply
                    .setIsSuccess(false)
                    .setMessage(e.getMessage());
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        // Remove all members from the group before deleting it
        // This also removes this group from all of its member's sets of joined groups
        group.removeAllMembers();
        groupRepository.delete(group);
        reply
                .setIsSuccess(true)
                .setMessage("Group " + request.getGroupId() + " has been deleted");

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GetGroupDetailsResponse> responseObserver) {
        logger.info("getGroupDetails() has been called");
        // TODO implement this
    }

    /**
     * Validates the constraints of a group object
     * If an empty list if returned, then no validation errors were found
     * @param group The group to validate
     * @return A list of ValidationErrors
     */
    List<ValidationError> getValidationErrors(Group group) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        List<ValidationError> errors = new ArrayList<>();

        // Build ValidationErrors from ConstraintViolations
        for (ConstraintViolation<Group> violation : validator.validate(group)) {
            // Get the name of the field the violation was found in
            String field = null;
            for (Path.Node node : violation.getPropertyPath()) {
                field = node.getName();
            }
            assert field != null;
            // Create a ValidationError
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName(field)
                    .setErrorText(violation.getMessage())
                    .build();
            errors.add(error);
        }

        return errors;
    }
}
