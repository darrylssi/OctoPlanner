package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.MEMBERS_WITHOUT_GROUPS_ID;
import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;

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

    @Autowired
    private UserAccountServerService userAccountServerService;

    @Override
    public void getPaginatedGroups(GetPaginatedGroupsRequest request, StreamObserver<PaginatedGroupsResponse> responseObserver) {
        logger.info("getPaginatedGroups has been called");
        PaginatedGroupsResponse.Builder reply = PaginatedGroupsResponse.newBuilder();
        int limit = request.getPaginationRequestOptions().getLimit();
        int offset = request.getPaginationRequestOptions().getOffset();
        String orderBy = request.getPaginationRequestOptions().getOrderBy();
        boolean isAscending = request.getPaginationRequestOptions().getIsAscendingOrder();

        List<Group> paginatedGroups;
        try {
            paginatedGroups = groupService.getGroupsPaginated(offset, limit, orderBy, isAscending);
        } catch (IllegalArgumentException e) { // `orderBy` wasn't a valid value.
            Throwable statusError = Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
            responseObserver.onError(statusError);
            return;
        }
        for(int i = 0; i< paginatedGroups.size(); i++){
            GroupDetailsResponse groupDetailsResponse = GroupDetailsResponse.newBuilder().setGroupId(paginatedGroups.get(i).getId())
            reply.addGroups(i, groupDetailsResponse);
        }


        #loop througp paginatedGroups and then add to reply


//        List<GroupDetailsResponse> groupResponses = paginatedGroups.stream().map(this::buildGroupResponse).toList();
//        int numGroupsInDatabase = (int) groupRepository.count();
//        PaginationResponseOptions.Builder responseOptions = PaginationResponseOptions.newBuilder();
//        responseOptions.setResultSetSize(numGroupsInDatabase).build();
//        reply
//                .addAllGroups(groupResponses)
//                .setPaginationResponseOptions(responseOptions);
//
//        responseObserver.onNext(reply.build());
//        responseObserver.onCompleted();
    }

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

    /**
     * Modifies the details of an existing group and returns a ModifyGroupDetailsResponse
     * @param request An object containing the id of the group to modify and the details to change
     */
    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        logger.info("modifyGroupDetails() has been called");
        ModifyGroupDetailsResponse.Builder reply = ModifyGroupDetailsResponse.newBuilder();

        // Check that the group is not one of the default groups
        if (request.getGroupId() == TEACHER_GROUP_ID) {
            reply
                    .setIsSuccess(false)
                    .setMessage("The group \"Teaching Staff\" cannot be edited");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        } else if (request.getGroupId() == MEMBERS_WITHOUT_GROUPS_ID) {
            reply
                    .setIsSuccess(false)
                    .setMessage("The group \"Members Without A Group\" cannot be edited");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        Group group;

        // Check that the group exists
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

        // Validate the fields in the group
        group.setShortName(request.getShortName());
        group.setLongName(request.getLongName());
        List<ValidationError> errors = getValidationErrors(group);

        if(!errors.isEmpty()) { // If there are errors in the request
            for (ValidationError error : errors) {
                logger.error("Modify group {} : {} - {}",
                        request.getGroupId(), error.getFieldName(), error.getErrorText());
            }
            reply
                    .setIsSuccess(false)
                    .setMessage("Group could not be edited")
                    .addAllValidationErrors(errors);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        // Once all validation passes, save the group and return success
        groupRepository.save(group);
        reply
                .setIsSuccess(true)
                .setMessage("Group edited successfully");

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Deletes a group if it exists and returns a DeleteGroupResponse
     * @param request An object containing the id of the group to delete
     */
    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        logger.info("deleteGroup() has been called");
        DeleteGroupResponse.Builder reply = DeleteGroupResponse.newBuilder();

        if (request.getGroupId() == TEACHER_GROUP_ID) {
            reply
                    .setIsSuccess(false)
                    .setMessage("The group \"Teaching Staff\" cannot be deleted");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        } else if (request.getGroupId() == MEMBERS_WITHOUT_GROUPS_ID) {
            reply
                    .setIsSuccess(false)
                    .setMessage("The group \"Members Without A Group\" cannot be deleted");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

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

    /**
     * Gets a group's details in the form of a GetGroupDetailsResponse
     * If the group does not exist, all the fields in the GetGroupDetailsResponse will be blank
     * @param request An object containing the id of the group to retrieve details from
     */
    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("getGroupDetails() has been called");
        GroupDetailsResponse.Builder reply = GroupDetailsResponse.newBuilder();

        Group group;
        try {
            group = groupService.getGroup(request.getGroupId());
        } catch (NoSuchElementException e) {
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        // Create a UserResponse for each group member
        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : group.getMembers()) {
            userResponses.add(userAccountServerService.buildUserResponse(user));
        }

        reply
                .setGroupId(group.getId())
                .setShortName(group.getShortName())
                .setLongName(group.getLongName())
                .addAllMembers(userResponses);
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Validates the constraints of a group object
     * If an empty list is returned, then no validation errors were found
     * @param group The group to validate
     * @return A list of ValidationErrors
     */
    private List<ValidationError> getValidationErrors(Group group) {
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

        // Check that the short and long names are unique
        for (Group other : groupRepository.findAll()) {
            if (group.getId() != other.getId()) {
                if (Objects.equals(group.getShortName(), other.getShortName())) {
                    ValidationError error = ValidationError.newBuilder()
                            .setFieldName("shortName")
                            .setErrorText("Group short name is already in use")
                            .build();
                    errors.add(error);
                }
                if (Objects.equals(group.getLongName(), other.getLongName())) {
                    ValidationError error = ValidationError.newBuilder()
                            .setFieldName("longName")
                            .setErrorText("Group long name is already in use")
                            .build();
                    errors.add(error);
                }
            }
        }

        return errors;
    }
}
