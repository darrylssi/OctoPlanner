package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    @Autowired
    SprintService sprintService;

    private Sprint getSprintDetails(String label) {
        return sprintService.getSprintByLabel(label);
    }

    @GetMapping("/edit-sprint")
    public String sprintForm(@RequestParam("label") String label, Model model) {
        /* Add sprint details to the model */
        Sprint sprint = getSprintDetails(label);
        model.addAttribute("sprintLabel", label);
        model.addAttribute("sprintName", sprint.getName());
        model.addAttribute("sprintStartDate", sprint.getStartDateString());
        model.addAttribute("sprintEndDate", sprint.getEndDateString());
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
//        this.sprintName = sprintName;
//        this.sprintStartDate = sprintStartDate;
//        this.sprintEndDate = sprintEndDate;
//        this.sprintDescription = sprintDescription;
        return "redirect:/edit-sprint";
    }

}
