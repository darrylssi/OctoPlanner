package nz.ac.canterbury.seng302.portfolio.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
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

    /**
     * Sends a request to the identity provider to get a paginated list of all groups
     * @param offset What "page" of the groups you want. Affected by the ordering and page size. This starts at 0.
     * @param limit How many items you want from
     * @param orderBy How the list is ordered.
     *                Your options are:
     *                  <ul>
     *                    <li><code>"shortNname"</code> - Ordered by groups short name alphabetically</li>
     *                    <li><code>"longName"</code> - Ordered by groups long name alphabetically</li>
     *                  </ul>
     * @param isAscending Is the list in ascending or descending order
     * @return A PaginatedGroupsResponse containing a list of groups
     * @throws IllegalArgumentException Thrown if the provided orderBy string isn't one of the valid options
     */
    public PaginatedGroupsResponse getPaginatedGroups(final int offset, final int limit, final String orderBy, final boolean isAscending) throws IllegalArgumentException {
        PaginationRequestOptions requestOptions = PaginationRequestOptions.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscending)
                .build();

        GetPaginatedGroupsRequest paginatedGroupsRequest = GetPaginatedGroupsRequest.newBuilder()
                .setPaginationRequestOptions(requestOptions)
                .build();
        try {
            return groupStub.getPaginatedGroups(paginatedGroupsRequest);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                // Didn't order by a valid column
                throw new IllegalArgumentException(e.getMessage());
            } else {
                throw e;
            }
        }
    }
}
