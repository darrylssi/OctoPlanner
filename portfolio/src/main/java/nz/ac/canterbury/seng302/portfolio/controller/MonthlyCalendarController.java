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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.controller.PageController.requiresRoleOfAtLeast;

/**
 * Controller for the display project details on the monthly calendar
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
    private DateUtils utils;                                    // initializing the date utils

    private List<Sprint> sprintList;                            // initializing the sprint list
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
        PrincipalData principalData = PrincipalData.from(principal);
        boolean hasEditPermissions = principalData.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("sprintsEditable", hasEditPermissions);

        // Get current user's username for the header
        model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
        model.addAttribute("project", project);
        model.addAttribute("projectStartDate", project.getProjectStartDate().toString());
        model.addAttribute("projectEndDate", addOneDayToEndDate(project.getProjectEndDate()));

        sprintList = sprintService.getSprintsOfProjectById(id);
        if (!sprintList.isEmpty()) {
            // Gets the sprint string list containing three strings which are sprintNames, sprintStartDates and sprintEndDate
            ArrayList<String> getSprintsArrayList = getSprintsStringList(sprintList, true);

            model.addAttribute("sprintIds", getSprintsArrayList.get(0));
            model.addAttribute("sprintNames", getSprintsArrayList.get(1));
            model.addAttribute("sprintStartDates", getSprintsArrayList.get(2));
            model.addAttribute("sprintEndDates", getSprintsArrayList.get(3));
        }

        return "monthlyCalendar";
    }


    @PostMapping("/monthlyCalendar/{id}")
    public String updateMonthlyCalendar(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="id") int id,
            @RequestParam(name="sprintId", required = false) Integer sprintId,
            @RequestParam(name="sprintStartDate", required = false) Date sprintStartDate,
            @RequestParam(name="sprintEndDate", required = false) Date sprintEndDate,
            Model model
    ) throws Exception  {
        // checks ...
        if (sprintId == null) {
            getMonthlyCalendar(principal, id, model);
        }

        // updating the sprint dates at the given sprint id
        Sprint getSprintAtId = sprintService.getSprintById(sprintId);
        getSprintAtId.setStartDate(sprintStartDate);
        getSprintAtId.setEndDate(sprintEndDate);
        sprintService.saveSprint(getSprintAtId);

        // Get the current project id
        Project project = projectService.getProjectById(id);

        // If the user is at least a teacher, sprint durations will be editable
        PrincipalData principalData = PrincipalData.from(principal);
        boolean hasEditPermissions = principalData.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("sprintsEditable", hasEditPermissions);

        // Get current user's username for the header
        model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
        model.addAttribute("project", project);
        model.addAttribute("projectStartDate", project.getProjectStartDate().toString());
        model.addAttribute("projectEndDate", addOneDayToEndDate(project.getProjectEndDate()));

        sprintList = sprintService.getSprintsOfProjectById(id);
        if (!sprintList.isEmpty()) {
            // Gets the sprint string list containing three strings which are sprintNames, sprintStartDates and sprintEndDate
            ArrayList<String> getSprintsArrayList = getSprintsStringList(sprintList, false);

            model.addAttribute("sprintIds", getSprintsArrayList.get(0));
            model.addAttribute("sprintNames", getSprintsArrayList.get(1));
            model.addAttribute("sprintStartDates", getSprintsArrayList.get(2));
            model.addAttribute("sprintEndDates", getSprintsArrayList.get(3));
        }

        return "monthlyCalendar";
    }


    /**
     * The list to containing three strings, which are sprint names, sprint start dates, and sprint end dates,
     * respectively. These are to be used for displaying sprints on the Monthly Calendar.
     * @param sprintList The list containing all the sprints in the current project
     * @return The string array list of sprint names, start dates and end dates
     */
    private ArrayList<String> getSprintsStringList(List<Sprint> sprintList, boolean isEndDateChanged) {
        // Initializing the array list
        ArrayList<String> sprintsDetailsList = new ArrayList<String>();

        String sprintIds = "";                  // Initiating the sprint ids list
        String sprintNames = "";                // Initiating the sprint names list
        String sprintStartDates = "";           // Initiating the sprint start dates list
        String sprintEndDates = "";             // Initiating the sprint end dates list

        // For loop to add each sprint names, start date and end date to the respective strings
        for (Sprint eachSprint: sprintList) {
            sprintIds += eachSprint.getId() + ",";
            sprintNames += eachSprint.getSprintName() + ",";
            sprintStartDates += eachSprint.getSprintStartDate().toString().substring(0, 10) + ",";

            // checks if the end date has been updated, based on that it adds one day to it or not
            if (isEndDateChanged) {
                sprintEndDates += addOneDayToEndDate(eachSprint.getSprintEndDate()) + ",";
            } else {
                sprintEndDates += eachSprint.getSprintEndDate().toString().substring(0, 10) + ",";
            }
        }

        // Removing the string's last character, which is ","
        sprintIds = sprintIds.substring(0, sprintIds.length()-1);
        sprintNames = sprintNames.substring(0 , sprintNames.length()-1);
        sprintStartDates = sprintStartDates.substring(0, sprintStartDates.length()-1);
        sprintEndDates = sprintEndDates.substring(0, sprintEndDates.length()-1);

        // Adding to the sprintsDetailsList
        sprintsDetailsList.add(sprintIds);
        sprintsDetailsList.add(sprintNames);
        sprintsDetailsList.add(sprintStartDates);
        sprintsDetailsList.add(sprintEndDates);

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
        String newEndDate = utils.toString(Date.from(newLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return newEndDate;
    }

}
