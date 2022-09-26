package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Controller to handle requests on the groups page.
 */
@Controller
public class GroupController extends PageController{

    //TODO update this when the template is actually created or just get rid of the TODO
    public static final String GROUPS_TEMPLATE_NAME = "groups";

    @Autowired
    UserAccountClientService userAccountClientService;

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
        /*
        Insert code that does things with the page here
         */
        return GROUPS_TEMPLATE_NAME;    // Return the name of the Thymeleaf template
    }

    @PostMapping("/groups/{group_id}/add-members")
    public ResponseEntity<String> addMembers(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("group_id") int groupId,
            @RequestParam("user_id") int[] userIds
    ){
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
        List<UserResponse> usersInGroup = groupClientService.getGroupDetails(groupId).getMembersList();
        List<Integer> idsToAdd = new ArrayList<>();

        for (int userId: userIds) {
            UserResponse userResponse = userAccountClientService.getUserAccountById(userId);
            if(userResponse == null){
                return new ResponseEntity<>("One or more users do not exist.", HttpStatus.NOT_FOUND);
            }else if(!usersInGroup.contains(userResponse)) {
                idsToAdd.add(userId);
            }
        }

        AddGroupMembersResponse addGroupMembersResponse = groupClientService.addGroupMembers(groupId, idsToAdd);

        if (addGroupMembersResponse.getIsSuccess()) {
            return new ResponseEntity<>(addGroupMembersResponse.getMessage(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(addGroupMembersResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
