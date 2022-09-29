package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


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
    private DeadlineService deadlineService;                    // initializing the DeadlineService
    @Autowired
    private EventService eventService;
    @Autowired
    private MilestoneService milestoneService;
    @Autowired
    private DetailsController detailsController;

    /**
     *
     * @param principal Current authentication state
     * @param id Current project id
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     * @throws ResponseStatusException if the project id is invalid
     */
    @GetMapping("/monthlyCalendar/{id}")
    public String getMonthlyCalendar(
                                    @AuthenticationPrincipal AuthState principal,
                                    @PathVariable(name="id") int id,
                                    Model model) throws ResponseStatusException {
        // Get the current project id
        Project project = projectService.getProjectById(id);

        // If the user is at least a teacher, sprint durations will be editable
        PrincipalData thisUser = PrincipalData.from(principal);
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("sprintsEditable", hasEditPermissions);
        model.addAttribute("userName", thisUser.getFullName());
        model.addAttribute("userId", thisUser.getID());

        model.addAttribute("project", project);
        model.addAttribute("projectStartDate", project.getProjectStartDate().toString());
        model.addAttribute("projectEndDate", DateUtils.addOneDayToDate(project.getProjectEndDate()));

        List<Sprint> sprintList = sprintService.getSprintsInProject(id);
        if (!sprintList.isEmpty()) {
            // Gets the sprint string list containing five strings which are sprintIds, sprintNames, sprintStartDates, sprintEndDates and sprintColours
            ArrayList<String> getSprintsArrayList = getSprintsStringList(sprintList);

            model.addAttribute("sprintIds", getSprintsArrayList.get(0));
            model.addAttribute("sprintNames", getSprintsArrayList.get(1));
            model.addAttribute("sprintStartDates", getSprintsArrayList.get(2));
            model.addAttribute("sprintEndDates", getSprintsArrayList.get(3));
            model.addAttribute("sprintColours", getSprintsArrayList.get(4));
        }

        List<Schedulable> schedulableList = detailsController.getAllSchedulablesInProject(id);
        List<String> schedulableDetailsList = getStringListFromSchedulables(schedulableList);
        model.addAttribute("schedulableNames", schedulableDetailsList.get(0));
        model.addAttribute("schedulableTypes", schedulableDetailsList.get(1));
        model.addAttribute("schedulableStartDates", schedulableDetailsList.get(2));
        model.addAttribute("schedulableEndDates", schedulableDetailsList.get(3));

        model.addAttribute("tab", 2);

        return "monthlyCalendar";
    }

    /**
     * Returns a list with four strings, each comma separated (actually ", " separated).
     * The strings are, in order, the names, types, start dates, and end dates of the provided schedulable objects.
     * Note: the dates are in "yyyy-mm-dd HH:mm" format.
     * @param schedulableList list of schedulable objects
     * @return list of 4 strings as described above
     */
    private List<String> getStringListFromSchedulables(List<Schedulable> schedulableList) {
        ArrayList<String> schedulableDetailsList = new ArrayList<>();

        ArrayList<String> schedulableNames = new ArrayList<>();
        ArrayList<String> schedulableTypes = new ArrayList<>();
        ArrayList<String> schedulableStartDates = new ArrayList<>();
        ArrayList<String> schedulableEndDates = new ArrayList<>();

        for (Schedulable schedulable: schedulableList) {
            schedulableNames.add(schedulable.getName());
            schedulableTypes.add(schedulable.getType());
            schedulableStartDates.add(DateUtils.toDateTimeString(schedulable.getStartDate()));
            schedulableEndDates.add(DateUtils.toDateTimeString(schedulable.getEndDate()));
        }

        schedulableDetailsList.add(String.join(", ", schedulableNames));
        schedulableDetailsList.add(String.join(", ", schedulableTypes));
        schedulableDetailsList.add(String.join(", ", schedulableStartDates));
        schedulableDetailsList.add(String.join(", ", schedulableEndDates));

        return schedulableDetailsList;
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
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        Sprint sprintToUpdate;
        try {
            sprintToUpdate = sprintService.getSprintById(sprintId);
        } catch (Exception e) {
            // sprint does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint " + sprintId + " does not exist");
        }
        sprintToUpdate.setStartDate(sprintStartDate);
        sprintToUpdate.setEndDate(DateUtils.removeOneDayFromDate(sprintEndDate));
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

        ArrayList<String> sprintIds = new ArrayList<>();
        ArrayList<String> sprintNames = new ArrayList<>();                // Initiating the sprint names list
        ArrayList<String> sprintStartDates = new ArrayList<>();           // Initiating the sprint start dates list
        ArrayList<String> sprintEndDates = new ArrayList<>();             // Initiating the sprint end dates list
        ArrayList<String> sprintColours = new ArrayList<>();             // Initiating the sprint colours list

        // For loop to add each sprint ids, names, start date, end date and colours to the respective strings
        for (Sprint eachSprint: sprintList) {
            sprintIds.add(String.valueOf(eachSprint.getId()));
            sprintNames.add(eachSprint.getSprintName());
            sprintStartDates.add(DateUtils.toString(eachSprint.getSprintStartDate()));
            sprintEndDates.add(DateUtils.addOneDayToDate(eachSprint.getSprintEndDate()));
            sprintColours.add(eachSprint.getSprintColour());
        }

        sprintsDetailsList.add(String.join(",", sprintIds));
        sprintsDetailsList.add(String.join(",", sprintNames));
        sprintsDetailsList.add(String.join(",", sprintStartDates));
        sprintsDetailsList.add(String.join(",", sprintEndDates));
        sprintsDetailsList.add(String.join(",", sprintColours));

        return sprintsDetailsList;
    }


    /**
     * Get all schedulables of a given type.
     * @param id ID of the project the schedulables belong to
     * @param schedulableType Type of schedulable to be taken
     * @return ResponseEntity object with the list of schedulables
     */
    @GetMapping("/project/{id}/schedulables/{type}")
    public ResponseEntity<List<Schedulable>> getAllSchedulables(
            @PathVariable(name="id") int id,
            @PathVariable(name="type") String schedulableType
    ){
        List<Schedulable> schedulableList = new ArrayList<>();
        if (EVENT_TYPE.equals(schedulableType)) {
            schedulableList.addAll(eventService.getAllEvents());
        } else if (DEADLINE_TYPE.equals(schedulableType)) {
            schedulableList.addAll(deadlineService.getAllDeadlines());
        } else if (MILESTONE_TYPE.equals(schedulableType)) {
            schedulableList.addAll(milestoneService.getAllMilestones());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, schedulableType + " is not a type of schedulable!");
        }
        return new ResponseEntity<>(schedulableList, HttpStatus.OK);
    }

}
