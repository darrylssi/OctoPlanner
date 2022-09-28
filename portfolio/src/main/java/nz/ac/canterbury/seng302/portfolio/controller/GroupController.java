package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetGroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to handle requests on the groups page.
 */
@Controller
public class GroupController extends PageController{

    public static final String GROUPS_TEMPLATE_NAME = "groups";

    @Autowired
    GroupClientService groupClientService;


    /**
     * Get request to view the groups page.
     * @param principal Authenticated user
     * @param model Parameters sent to thymeleaf template
     * @return Groups page
     */
    @GetMapping("/groups")
    public String groups(
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        boolean hasEditPermissions = PrincipalData.from(principal).hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);

        model.addAttribute("tab", 3);
        Map<Integer, GetGroupDetailsResponse> groups = new HashMap<>();
        groups.put(GlobalVars.TEACHER_GROUP_ID, groupClientService.getGroupDetails(GlobalVars.TEACHER_GROUP_ID));
        groups.put(GlobalVars.MEMBERS_WITHOUT_GROUPS_ID, groupClientService.getGroupDetails(GlobalVars.MEMBERS_WITHOUT_GROUPS_ID));

        model.addAttribute("groups", groups);
        model.addAttribute("membersWithoutGroupsId", GlobalVars.MEMBERS_WITHOUT_GROUPS_ID);

        return GROUPS_TEMPLATE_NAME;    // Return the name of the Thymeleaf template
    }

    /**
     * A post mapping to add a list of users to a group
     * @param principal the user adding members to a group. used for authentication
     * @param groupId the id of the group being added to
     * @param userIds a list of the ids of the users being added
     * @return a response entity describing whether the action was successful
     */
    @PostMapping("/groups/{group_id}/add-members")
    public ResponseEntity<String> addMembers(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("group_id") int groupId,
            @RequestParam("user_id") List<Integer> userIds
    ){
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        AddGroupMembersResponse addGroupMembersResponse = groupClientService.addGroupMembers(groupId, userIds);

        if (addGroupMembersResponse.getIsSuccess()) {
            return new ResponseEntity<>(addGroupMembersResponse.getMessage(), HttpStatus.OK);
        } else if (addGroupMembersResponse.getMessage().equals("There is no group with id " + groupId)){
            return new ResponseEntity<>(addGroupMembersResponse.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(addGroupMembersResponse.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
