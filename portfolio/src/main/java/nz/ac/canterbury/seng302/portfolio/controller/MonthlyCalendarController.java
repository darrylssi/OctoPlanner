package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Controller for the display project details on the monthly calendar
 */
@Controller
public class MonthlyCalendarController extends PageController {

    @Autowired
    private ProjectService projectService;                      // initializing the ProjectService
    @Autowired
    private SprintService sprintService;                        // initializing the SprintService
    @Autowired
    private DateUtils utils;                                    // initializing the DateUtils
    private UserAccountClientService userAccountClientService;

    /**
     *
     * @param principal Current authentication state
     * @param id Current project id
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     * @throws Exception if the project id is invalid
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

        model.addAttribute("project", project);
        model.addAttribute("projectStartDate", project.getProjectStartDate().toString());
        model.addAttribute("projectEndDate", addOneDayToEndDate(project.getProjectEndDate()));

        List<Sprint> sprintList = sprintService.getSprintsOfProjectById(id);
        if (!sprintList.isEmpty()) {
            // Gets the sprint string list containing five strings which are sprintIds, sprintNames, sprintStartDates, sprintEndDates and sprintColours
            ArrayList<String> getSprintsArrayList = getSprintsStringList(sprintList);

            model.addAttribute("sprintIds", getSprintsArrayList.get(0));
            model.addAttribute("sprintNames", getSprintsArrayList.get(1));
            model.addAttribute("sprintStartDates", getSprintsArrayList.get(2));
            model.addAttribute("sprintEndDates", getSprintsArrayList.get(3));
            model.addAttribute("sprintColours", getSprintsArrayList.get(4));
        }

        return "monthlyCalendar";
    }

    /**
     * Post Mapping for when a sprint date is updated in the monthly calendar. Saves the changed sprint dates.
     * @param principal Current authentication state
     * @param id the id of the project the sprint belongs to
     * @param sprintId the id of the sprint being updated
     * @param sprintStartDate the new sprint start date
     * @param sprintEndDate the new sprint end date (will be one day after the actual endDate because of the way
     *                      that FullCalendar works
     * @return a redirect to the monthly calendar page
     */
    @PostMapping("/monthlyCalendar/{id}")
    public String updateMonthlyCalendar(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="id") int id,
            @RequestParam(name="sprintId") Integer sprintId,
            @RequestParam(name="sprintStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date sprintStartDate,
            @RequestParam(name="sprintEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date sprintEndDate
    ) throws ParseException {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        Sprint sprintToUpdate;
        try {
            sprintToUpdate = sprintService.getSprintById(sprintId);
        } catch (Exception e) {
            // sprint does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint " + sprintId + " does not exist");
        }
        sprintToUpdate.setStartDate(sprintStartDate);
        sprintToUpdate.setEndDate(removeOneDayFromEndDate(sprintEndDate));
        sprintService.saveSprint(sprintToUpdate);

        return "redirect:../monthlyCalendar/" + id;
    }


    /**
     * The list to containing five strings, which are sprint ids, sprint names, sprint start dates, sprint end dates,
     * and sprint colours respectively. These are to be used for displaying sprints on the Monthly Calendar.
     * @param sprintList The list containing all the sprints in the current project
     * @return The string array list of sprint ids, names, start dates, end dates, and colours
     */
    private ArrayList<String> getSprintsStringList(List<Sprint> sprintList) {
        // Initializing the array list
        ArrayList<String> sprintsDetailsList = new ArrayList<>();

        StringBuilder sprintIds = new StringBuilder();
        StringBuilder sprintNames = new StringBuilder();                // Initiating the sprint names list
        StringBuilder sprintStartDates = new StringBuilder();           // Initiating the sprint start dates list
        StringBuilder sprintEndDates = new StringBuilder();             // Initiating the sprint end dates list
        StringBuilder sprintColours = new StringBuilder();             // Initiating the sprint colours list

        // For loop to add each sprint ids, names, start date, end date and colours to the respective strings
        for (Sprint eachSprint: sprintList) {
            sprintIds.append(eachSprint.getId() + ",");
            sprintNames.append(eachSprint.getSprintName() + ",");
            sprintStartDates.append(eachSprint.getSprintStartDate().toString().substring(0, 10) + ",");
            sprintEndDates.append(addOneDayToEndDate(eachSprint.getSprintEndDate()) + ",");
            sprintColours.append(eachSprint.getSprintColour() + ",");
        }

        // Removing the string's last character, which is "," and adding to the sprintsDetailsList
        sprintsDetailsList.add(sprintIds.substring(0 , sprintIds.length()-1));
        sprintsDetailsList.add(sprintNames.substring(0 , sprintNames.length()-1));
        sprintsDetailsList.add(sprintStartDates.substring(0, sprintStartDates.length()-1));
        sprintsDetailsList.add(sprintEndDates.substring(0, sprintEndDates.length()-1));
        sprintsDetailsList.add(sprintColours.substring(0, sprintColours.length()-1));

        return sprintsDetailsList;
    }


     /**
      * FullCalendar displays events with an end date exclusive format. This function adds a day to
      * the end date of a sprint so that the date can be used to display the sprint correctly in the calendar.
     * @param endDate The project or sprint end date
     * @return The updated new end date as a string to be passed to FullCalendar
     */
    private String addOneDayToEndDate(Date endDate) {
        // Converting date to LocalDate
        LocalDate localEndDate = endDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Adding 1 day to current project/sprint end date
        LocalDate newLocalEndDate = localEndDate.plusDays(1);

        // Converting the new project/sprint LocalDate object to Date object
        return DateUtils.toString(Date.from(newLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * FullCalendar displays events with an end date exclusive format. This function removes a day from
     * the end date given by FullCalendar so that the correct date can be saved to the database.
     * @param endDate The project or sprint end date
     * @return The updated new end date as a date to be saved
     */
    private Date removeOneDayFromEndDate(Date endDate) {
        // Converting date to LocalDate
        LocalDate localEndDate = endDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Adding 1 day to current project/sprint end date
        LocalDate newLocalEndDate = localEndDate.minusDays(1);

        // Converting the new project/sprint LocalDate object to Date object
        return Date.from(newLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
