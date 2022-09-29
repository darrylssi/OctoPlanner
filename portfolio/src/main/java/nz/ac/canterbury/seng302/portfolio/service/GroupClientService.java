package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupClientService {

    @GrpcClient("identity-provider-grpc-server")
    private GroupsServiceGrpc.GroupsServiceBlockingStub groupStub;

    @GrpcClient("identity-provider-grpc-server")
    private GroupsServiceGrpc.GroupsServiceStub groupServiceStub;

    private static final Logger logger = LoggerFactory.getLogger(GroupClientService.class);

    /**
     * Gets the paginated groups
     * @param offSet How many results to skip (offset of 0 means start at beginning, i.e page 1)
     * @param limit Max results to get - "results per page"
     * @param orderBy When paginating, we must sort on the server, not the frontend (why is this?)
     * @param isAscendingOrder gets the boolean whether it is order by ascending or descending
     * @return
     */
    public PaginatedGroupsResponse getPaginatedGroups(int offSet, int limit, String orderBy, boolean isAscendingOrder){
        logger.info("Sending request to retrieve all the groups");

        PaginationRequestOptions paginationRequestOptions = PaginationRequestOptions.newBuilder().setOffset(1)
                .setOffset(offSet)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscendingOrder)
                .build();
        GetPaginatedGroupsRequest getPaginatedGroupsRequest = GetPaginatedGroupsRequest.newBuilder().setPaginationRequestOptions(paginationRequestOptions).build();
        return groupStub.getPaginatedGroups(getPaginatedGroupsRequest);
    }

    /**
     * Sends a request to the identity provider to create a new group
     * @param shortName The new group's short name
     * @param longName The new group's long name
     * @return A CreateGroupResponse, containing the success of the request and the new group's id
     */
    public CreateGroupResponse createGroup(final String shortName, final String longName) {
        logger.info("Sending request to create a new group");
        CreateGroupRequest createGroupRequest = CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName(longName)
                .build();
        return groupStub.createGroup(createGroupRequest);
    }

    /**
     * Sends a request to the identity provider to add a set of users to a group
     * @param groupId The id of the group to add members to
     * @param userIds A set containing the member's user ids to add to the group
     * @return An AddGroupMembersResponse, containing the success of the request
     */
    public AddGroupMembersResponse addGroupMembers(final int groupId, final List<Integer> userIds) {
        logger.info("Sending request to add members to the group with id {}", groupId);
        AddGroupMembersRequest addGroupMembersRequest = AddGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupStub.addGroupMembers(addGroupMembersRequest);
    }

    /**
     * Sends a request to the identity provider to remove a set of users from a group
     * @param groupId The id of the group to remove members from
     * @param userIds A set containing the member's user ids to remove from the group
     * @return A RemoveGroupMembersResponse, containing the success of the request
     */
    public RemoveGroupMembersResponse removeGroupMembers(final int groupId, final List<Integer> userIds) {
        logger.info("Sending request to remove members from the group with id {}", groupId);
        RemoveGroupMembersRequest removeGroupMembersRequest = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupStub.removeGroupMembers(removeGroupMembersRequest);
    }

    /**
     * Sends a request to the identity provider to modify an existing group's details
     * @param groupId The id of the group to modify
     * @param shortName The group's new short name
     * @param longName The group's new long name
     * @return A ModifyGroupDetailsResponse, containing the success of the request and the new group's id
     */
    public ModifyGroupDetailsResponse modifyGroupDetails(final int groupId, final String shortName,
                                                         final String longName) {
        logger.info("Sending request to modify details of the group with id {}", groupId);
        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .setShortName(shortName)
                .setLongName(longName)
                .build();
        return groupStub.modifyGroupDetails(modifyGroupDetailsRequest);
    }

    /**
     * Sends a request to the identity provider to delete an existing group
     * @param groupId The id of the group to delete
     * @return A DeleteGroupResponse containing, the success of the request
     */
    public DeleteGroupResponse deleteGroup(final int groupId) {
        logger.info("Sending request to delete the group with id {}", groupId);
        DeleteGroupRequest deleteGroupRequest = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        return groupStub.deleteGroup(deleteGroupRequest);
    }

    /**
     * Sends a request to the identity provider to get an existing group's details
     * @param groupId The id of the group to fetch details of
     * @return A GetGroupDetailsResponse containing the groups short name, long name and a list of members
     */
    public GroupDetailsResponse getGroupDetails(final int groupId) {
        logger.info("Sending request to retrieve details from the group with id {}", groupId);
        GetGroupDetailsRequest getGroupDetailsRequest = GetGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        return groupStub.getGroupDetails(getGroupDetailsRequest);
    }


}
