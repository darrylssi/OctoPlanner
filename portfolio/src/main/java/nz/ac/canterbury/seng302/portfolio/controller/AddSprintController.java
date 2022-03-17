package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
//import nz.ac.canterbury.seng302.portfolio.service.SprintRepository;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the add sprint details page
 */
@Controller
public class AddSprintController {

//    @Autowired
//    private SprintRepository sprintRepository;

    @GetMapping("/add-sprint")
    public String projectForm(
            Model model, Sprint sprint) {
        model.addAttribute("sprintLabel", sprint.getLabel());
        /* Return the name of the Thymeleaf template */
        return "addSprint";
    }

    @PostMapping("/add-sprint")
    public String projectSave(
            @RequestParam(name="sprintName") String sprintName,
            @RequestParam(name="sprintStartDate") String sprintStartDate,
            @RequestParam(name="sprintEndDate") String sprintEndDate,
            @RequestParam(name="sprintDescription") String sprintDescription,
            Sprint sprint,
            Model model
    ) {
        Sprint newSprint = new Sprint(2, sprintName, sprintDescription, sprintStartDate, sprintEndDate);

//        sprintRepository.save(newSprint);

        return "teacherProjectDetails";
    }

}










//
//    Project project = new Project("Project 2022", "", "04/Mar/2022",
//            "04/Nov/2022");
//
////    /* Create default sprint page. */
////    Sprint sprint = new Sprint(1, "First Sprint", "This is my first" +
////            " sprint.", "04/11/2021", "08/07/2022");
//
//    @GetMapping("/add-sprint")
//    public String sprintForm(Model model) {
////        // Shows all project details
////        model.addAttribute("projectName", project.getName());
////        model.addAttribute("projectStartDate", project.getStartDateString());
////        model.addAttribute("projectEndDate", project.getEndDateString());
////        model.addAttribute("projectDescription", project.getDescription());
//
////        // Shows all sprint details
////        model.addAttribute("sprintLabel", sprint.getLabel());
////        model.addAttribute("sprintName", sprint.getName());
////        model.addAttribute("sprintStartDate", sprint.getStartDateString());
////        model.addAttribute("sprintEndDate", sprint.getEndDateString());
////        model.addAttribute("sprintDescription", sprint.getDescription());
//
//        return "addSprint";
//    }
//
//    @PostMapping("/add-sprint")
//    public String sprintSave(
//            @AuthenticationPrincipal AuthState principal,
//            @RequestParam(name="sprintName") String sprintName,
//            @RequestParam(name="sprintStartDate") String sprintStartDate,
//            @RequestParam(name="sprintEndDate") String sprintEndDate,
//            @RequestParam(name="sprintDescription") String sprintDescription,
//            Model model
//    ) {
//        Sprint newSprint = new Sprint(2, sprintName, sprintDescription, sprintStartDate, sprintEndDate);
//        return "addSprint";
//    }
//
//}
