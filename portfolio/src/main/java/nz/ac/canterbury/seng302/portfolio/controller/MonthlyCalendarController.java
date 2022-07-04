package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Controller for the display monthly calendar
 */
@Controller
public class MonthlyCalendarController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private UserAccountClientService userAccountClientService;
    @Autowired
    private DateUtils utils;

    /**
     *
     * @param principal Current authentication state
     * @param id Current project id
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     * @throws Exception
     */
    @GetMapping("/monthlyCalendar/{id}")
    public String getMonthlyCalendar(
                                    @AuthenticationPrincipal AuthState principal,
                                    @PathVariable(name="id") int id,
                                    Model model) throws Exception {
        // Get the current project id
        Project project = projectService.getProjectById(id);

        // If the user is at least a teacher, sprint durations will be editable
        PrincipalData thisUser = PrincipalData.from(principal);
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("sprintsEditable", hasEditPermissions);

        // Get current user's username for the header
        model.addAttribute("project", project);
        model.addAttribute("projectStartDate", project.getProjectStartDate().toString());
        model.addAttribute("projectEndDate", addOneDayToEndDate(project.getProjectEndDate()));

        List<Sprint> sprintList = sprintService.getSprintsOfProjectById(id);
        if (!sprintList.isEmpty()) {
            // Gets the sprint string list containing three strings which are sprintNames, sprintStartDates and sprintEndDate
            ArrayList<String> getSprintsArrayList = getSprintsStringList(sprintService.getSprintsOfProjectById(id));

            model.addAttribute("sprintNames", getSprintsArrayList.get(0));
            model.addAttribute("sprintStartDates", getSprintsArrayList.get(1));
            model.addAttribute("sprintEndDates", getSprintsArrayList.get(2));
            model.addAttribute("sprintColours", getSprintsArrayList.get(3));
        }

        return "monthlyCalendar";
    }


    /**
     * The list to containing three strings, which are sprint names, sprint start dates, and sprint end dates,
     * respectively. These are to be used for displaying sprints on the Monthly Calendar.
     * @param sprintList The list containing all the sprints in the current project
     * @return The string array list of sprint names, start dates and end dates
     */
    private ArrayList<String> getSprintsStringList(List<Sprint> sprintList) {
        // Initializing the array list
        ArrayList<String> sprintsDetailsList = new ArrayList<>();

        StringBuilder sprintNames = new StringBuilder();                // Initiating the sprint names list
        StringBuilder sprintStartDates = new StringBuilder();           // Initiating the sprint start dates list
        StringBuilder sprintEndDates = new StringBuilder();             // Initiating the sprint end dates list
        StringBuilder sprintColours = new StringBuilder();             // Initiating the sprint colours list


        for (Sprint eachSprint: sprintList) {
            sprintNames.append(eachSprint.getSprintName() + ",");
            sprintStartDates.append(eachSprint.getSprintStartDate().toString().substring(0, 10) + ",");
            sprintEndDates.append(addOneDayToEndDate(eachSprint.getSprintEndDate()) + ",");
            sprintColours.append(eachSprint.getSprintColour() + ",");
        }

        // Removing the string's last character, which is "," and adding to the sprintsDetailsList
        sprintsDetailsList.add(sprintNames.substring(0 , sprintNames.length()-1));
        sprintsDetailsList.add(sprintStartDates.substring(0, sprintStartDates.length()-1));
        sprintsDetailsList.add(sprintEndDates.substring(0, sprintEndDates.length()-1));
        sprintsDetailsList.add(sprintColours.substring(0, sprintColours.length()-1));

        return sprintsDetailsList;
    }


     /**
     * Gets the new project or sprint end date which is updated by adding a day to it.
     * @param endDate The project or sprint end date
     * @return The updated new end date
     */
    private String addOneDayToEndDate(Date endDate) {
        // Converting date to LocalDate
        LocalDate localEndDate = endDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Adding 1 day to current project/sprint end date
        LocalDate newLocalEndDate = localEndDate.plusDays(1);

        // Converting the new project/sprint LocalDate object to Date object
        return utils.toString(Date.from(newLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
