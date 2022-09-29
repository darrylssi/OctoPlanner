package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.GroupForm;
import nz.ac.canterbury.seng302.portfolio.model.Group;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.NAME_ERROR_MESSAGE;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.NAME_REGEX;

/**
 * Controller to handle requests on the groups page.
 */
@Controller
public class GroupController extends PageController{

    @Autowired
    private ProjectService projectService;
    @Autowired
    private GroupClientService groupClientService;

    //TODO update this when the template is actually created or just get rid of the TODO
    public static final String GROUPS_TEMPLATE_NAME = "groups";

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

    /**
     * Post request to add groups.
     * @param principal The authenticated or currently logged in user
     * @param projectId ID of the group's parent project
     * @param groupForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @return A response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    @PostMapping("/project/{projectId}/add-group")
    public ResponseEntity<String> postAddGroup(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("projectId") int projectId,
            @Valid GroupForm groupForm,
            BindingResult bindingResult
    ) {
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        // validate group
        ResponseEntity<String> validationResponse = validateGroup(groupForm, bindingResult);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            Group group = new Group();

            // Set details of new group object
            group.setParentProject(parentProject);
            group.setGroupShortName(groupForm.getShortName());
            group.setGroupLongName(groupForm.getLongName());

            CreateGroupResponse savedGroup = groupClientService.createGroup(group.getGroupShortName(), group.getGroupLongName());

            return ResponseEntity.ok(String.valueOf(savedGroup));
        } else {
            return validationResponse;
        }
    }

    /**
     * Handles edit requests for groups
     * @param principal Authenticated user
     * @param projectId The id of the project the group belongs to
     * @param groupId The id of the group to edit
     * @param editGroupForm Form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given the group failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{projectId}/edit-group/{groupId}")
    public ResponseEntity<String> postEditGroup(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("projectId") int projectId,
            @PathVariable("groupId") int groupId,
            @Valid GroupForm editGroupForm,
            BindingResult bindingResult
    ) {
        // Check that user has Teacher role or above
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            /*
            It is easier to check whether the user has the Teacher role than trying to check
            whether the user is part of the group, so checking Teacher role happens first
            */
            PrincipalData thisUser = PrincipalData.from(principal);
            GetGroupDetailsResponse thisGroup = groupClientService.getGroupDetails(groupId);

            // Checks that the user is in the group
            if (thisGroup.getMembersList().stream().noneMatch(o -> thisUser.getID() ==  o.getId())) {
                return new ResponseEntity<>("You are not a part of this group", HttpStatus.FORBIDDEN);
            }
        }

        // Validate form
        ResponseEntity<String> validationResponse = validateGroup(editGroupForm, bindingResult);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            ModifyGroupDetailsResponse editResponse = groupClientService.modifyGroupDetails(groupId,
                    editGroupForm.getShortName(), editGroupForm.getLongName());
            if (editResponse.getIsSuccess()) {
                return ResponseEntity.ok(editResponse.getMessage());
            } else {
                return new ResponseEntity<>(editResponse.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return validationResponse;
        }
    }

    /**
     * A method to validate groups when they are added or edited
     * @param groupForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @return A response entity that contains any errors that were found; Bad Request if there are errors, Ok if there are none
     */
    private ResponseEntity<String> validateGroup(GroupForm groupForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        ValidationError shortNameErrors = ValidationUtils.validateText(groupForm.getShortName(), NAME_REGEX, NAME_ERROR_MESSAGE);
        ValidationError longNameErrors = ValidationUtils.validateText(groupForm.getLongName(), NAME_REGEX, NAME_ERROR_MESSAGE);
        String errorString = ValidationUtils.joinErrors(new ValidationError(), shortNameErrors, longNameErrors);
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }

    /**
     * Mapping for an endpoint to remove users from a group
     * @param principal Authenticated user
     * @param groupId id of the group to remove members from
     * @param userIds list of ids of users to remove from said group
     * @return a response entity describing the outcome of the remove users request
     */
    @DeleteMapping("/groups/{group_id}/remove-members")
    public ResponseEntity<String> removeMembers(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("group_id") int groupId,
            @RequestParam("user_id") List<Integer> userIds
    ) {
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        RemoveGroupMembersResponse removeGroupMembersResponse = groupClientService.removeGroupMembers(groupId, userIds);

        if (removeGroupMembersResponse.getIsSuccess()) {
            return new ResponseEntity<>(removeGroupMembersResponse.getMessage(), HttpStatus.OK);
        } else if (removeGroupMembersResponse.getMessage().equals("There is no group with id " + groupId)) {
            return new ResponseEntity<>(removeGroupMembersResponse.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(removeGroupMembersResponse.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
