package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.GroupForm;
import nz.ac.canterbury.seng302.portfolio.model.*;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
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
        /*
        Insert code that does things with the page here
         */
        return GROUPS_TEMPLATE_NAME;    // Return the name of the Thymeleaf template
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
     * Deletes a group based on the given id
     * @param principal used to check if the user is authorised to delete group
     * @param groupId the id of the group to be deleted
     * @return a redirect to the project view
     */
    @DeleteMapping("/delete-group/{groupId}")
    @ResponseBody
    public ResponseEntity<String> deleteGroup(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name = "groupId") int groupId
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        GetGroupDetailsResponse groupDetails = groupClientService.getGroupDetails(groupId);
        System.out.println("Group details-> " + groupDetails);
        try {
            if (groupDetails.getShortName() != "Teaching staff" && groupDetails.getShortName() != "Members without groups") {
                groupClientService.deleteGroup(groupId);
                return new ResponseEntity<>("Group deleted.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Default group cannot be deleted", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
