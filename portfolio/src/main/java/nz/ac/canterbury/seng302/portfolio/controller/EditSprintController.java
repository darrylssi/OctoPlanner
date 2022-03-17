package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    /* Create default sprint page. */
    Sprint sprint = new Sprint(1, "First Sprint", "This is my first sprint.", "04/11/2021", "08/07/2022");

    @GetMapping("/edit-sprint")
    public String sprintForm(Model model) {
        /* Add sprint details to the model */
        model.addAttribute("sprintLabel", sprint.getLabel());
        model.addAttribute("sprintName", sprint.getName());
        model.addAttribute("sprintStartDate", sprint.getStartDate());
        model.addAttribute("sprintEndDate", sprint.getEndDate());
        model.addAttribute("sprintDescription", sprint.getDescription());


        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    @PostMapping("/edit-sprint")
    public String sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value="sprintName") String sprintName,
            @RequestParam(value="sprintStartDate") String sprintStartDate,
            @RequestParam(value="sprintEndDate") String sprintEndDate,
            @RequestParam(value="sprintDescription") String sprintDescription,
            Model model
    ) {
        sprint.setSprintName(sprintName);
        sprint.setStartDateString(sprintStartDate);
        sprint.setEndDateString(sprintEndDate);
        sprint.setSprintDescription(sprintDescription);
        return "redirect:/edit-sprint";
    }

}
