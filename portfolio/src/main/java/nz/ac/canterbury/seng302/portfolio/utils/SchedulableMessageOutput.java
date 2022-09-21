package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A message object to be sent through websockets when a schedulable is updated.
 * Contains all information used by javascript to update the schedulable display in html.
 */
public class SchedulableMessageOutput {

    enum BoxLocation {FIRST, INSIDE, AFTER}

    public static final String LIST_IN_ID_FORMAT = "schedulables-in-%d"; // id of sprint box
    public static final String LIST_AFTER_ID_FORMAT = "schedulables-after-%d"; // id of box after sprint
    public static final String LIST_BEFORE_ALL_ID_NAME = "schedulables-before"; // id of first box
    public static final String NEXT_SCHEDULABLE_ID_FORMAT = "%s-%d"; // id of a schedulable (used for the next schedulable)
    public static final String SCHEDULABLE_BEFORE_ALL_ID_FORMAT = "%d-before"; // id of schedulable in first box
    public static final String SCHEDULABLE_IN_ID_FORMAT = "%d-in-%d"; // id of schedulable in sprint box
    public static final String SCHEDULABLE_AFTER_ID_FORMAT = "%d-after-%d"; // id of schedulable after sprint box

    private int id;
    private String type;
    // NOTE if you change the names of these lists (or any of these variables), you will need to change them in websocketHandler.js
    private List<String> schedulableListIds; // List of all display boxes that this schedulable is included in
    private List<String> nextSchedulableIds; // List of schedulables displayed immediately after this schedulable in each box
    private List<String> schedulableBoxIds; // List of ids of the schedulable box being created

    /**
     * Empty constructor so we can send an output for a nonexistent schedulable
     */
    public SchedulableMessageOutput() {
    }

    /**
     * Constructor for a SchedulableMessageOutput
     * @param schedulable the schedulable to take parameters from
     * @param sprints list of all sprints for the project of this schedulable
     * @param schedulables list of all schedulables for the project of this schedulable
     */
    public SchedulableMessageOutput(Schedulable schedulable, List<Sprint> sprints, List<Schedulable> schedulables) {
        this.id = schedulable.getId();
        this.type = schedulable.getType();

        // Set the lists
        this.generateLists(schedulable, sprints, schedulables);
    }

    /**
     * Finds where the schedulable should go on the page, setting the IDs of:
     * the list box it should go in,
     * the schedulable immediately after this schedulable, and
     * the id of the new box being created.
     * @param schedulable the schedulable that is being updated
     * @param sprints list of all sprints in this schedulable's project
     * @param schedulables list of all schedulables in this schedulable's project
     */
    private void generateLists(Schedulable schedulable, List<Sprint> sprints, List<Schedulable> schedulables) {
        sprints.sort(Comparator.comparing(Sprint::getSprintEndDate));
        schedulables.sort(Comparator.comparing(Schedulable::getStartDate));

        this.schedulableListIds = new ArrayList<>();
        this.nextSchedulableIds = new ArrayList<>();
        this.schedulableBoxIds = new ArrayList<>();
        // check if the schedulable occurs before any sprints
        if(sprints.isEmpty()){
            // get the id of the schedulable that is displayed after this schedulable so that they appear in the correct order
            nextSchedulableIds.add(getNextSchedulable(schedulables, schedulable.getParentProject().getProjectStartDate(),
                    schedulable.getParentProject().getProjectEndDate(), schedulable, true));
            setSchedulableLocationIds(BoxLocation.FIRST, -1);
        } else if(sprints.get(0).getSprintStartDate().after(schedulable.getStartDate())) {
            // get the id of the schedulable that is displayed after this schedulable so that they appear in the correct order
            nextSchedulableIds.add(getNextSchedulable(schedulables, schedulable.getParentProject().getProjectStartDate(),
                    sprints.get(0).getSprintStartDate(), schedulable, true));
            setSchedulableLocationIds(BoxLocation.FIRST, -1);
        }

        //get list of all schedulable box ids to include the schedulable on the project details page
        for (int i = 0; i < sprints.size(); i++) {
            // Check if the schedulable overlaps with the sprint at index i
            if(DateUtils.timesOverlapIncl(sprints.get(i).getSprintStartDate(), sprints.get(i).getSprintEndDate(),
                    schedulable.getStartDate(), schedulable.getEndDate())){
                nextSchedulableIds.add(getNextSchedulable(schedulables, sprints.get(i).getSprintStartDate(),
                        sprints.get(i).getSprintEndDate(), schedulable, false));
                setSchedulableLocationIds(BoxLocation.INSIDE, sprints.get(i).getId());
            }
            // Check if schedulable occurs between the end of this sprint and the start of the next one
            if((sprints.size() > i+1) && DateUtils.timesOverlapExcl(sprints.get(i).getSprintEndDate(), sprints.get(i+1).getSprintStartDate(),
                    schedulable.getStartDate(), schedulable.getEndDate())) {
                nextSchedulableIds.add(getNextSchedulable(schedulables, sprints.get(i).getSprintEndDate(),
                        sprints.get(i+1).getSprintStartDate(), schedulable, true));
                setSchedulableLocationIds(BoxLocation.AFTER, sprints.get(i).getId());
            }
            // If this sprint is the last sprint, check if the schedulable occurs after the end of this sprint
            if(sprints.size() == i+1 && sprints.get(i).getSprintEndDate().before(schedulable.getEndDate())){
                nextSchedulableIds.add(getNextSchedulable(schedulables, sprints.get(i).getSprintEndDate(),
                        schedulable.getParentProject().getProjectEndDate(), schedulable, false));
                setSchedulableLocationIds(BoxLocation.AFTER, sprints.get(i).getId());
            }
        }
    }

    /**
     * Sets the list of id and schedulable box id that the schedulable in this message will occupy.
     * Should be called once the actual location has already been determined.
     * @param boxLocation the location, relative to sprints, of the updated schedulable
     * @param sprintId the id of the sprint this schedulable is located in or after. -1 if schedulable is before all sprints
     */
    private void setSchedulableLocationIds(BoxLocation boxLocation, int sprintId) {
        switch (boxLocation) {
            case FIRST -> {
                this.schedulableListIds.add(LIST_BEFORE_ALL_ID_NAME);
                this.schedulableBoxIds.add(String.format(SCHEDULABLE_BEFORE_ALL_ID_FORMAT, this.id));
            }
            case INSIDE -> {
                this.schedulableListIds.add(String.format(LIST_IN_ID_FORMAT, sprintId));
                this.schedulableBoxIds.add(String.format(SCHEDULABLE_IN_ID_FORMAT, this.id, sprintId));
            }
            case AFTER -> {
                this.schedulableListIds.add(String.format(LIST_AFTER_ID_FORMAT, sprintId));
                this.schedulableBoxIds.add(String.format(SCHEDULABLE_AFTER_ID_FORMAT, this.id, sprintId));
            }
        }
    }

    /**
     * Given a schedulable, gets the next schedulable within a certain time period
     * @param schedulables the list of all schedulables
     * @param periodStart the start of the time period
     * @param periodEnd the end of the time period
     * @param updatedSchedulable the schedulable being updated
     * @param exclusive whether the period is exclusive of the start and end dates
     * @return the box id of the next schedulable or -1 if there is no next schedulable
     */
    private String getNextSchedulable(List<Schedulable> schedulables, Date periodStart, Date periodEnd, Schedulable updatedSchedulable, boolean exclusive){
        for (Schedulable schedulable : schedulables) {
            if (schedulable.getStartDate().after(updatedSchedulable.getStartDate()) &&
                    (exclusive && DateUtils.timesOverlapExcl(periodStart, periodEnd,
                        schedulable.getStartDate(), schedulable.getEndDate()) ||
                            (!exclusive && DateUtils.timesOverlapIncl(periodStart, periodEnd,
                                    schedulable.getStartDate(), schedulable.getEndDate())))){
                return (String.format(NEXT_SCHEDULABLE_ID_FORMAT, schedulable.getType(), schedulable.getId()));
            }
        }
        return "-1";
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }

    public List<String> getSchedulableListIds() {
        return schedulableListIds;
    }

    public void setSchedulableListIds(List<String> schedulableListIds) {
        this.schedulableListIds = schedulableListIds;
    }

    public List<String> getNextSchedulableIds() {
        return nextSchedulableIds;
    }

    public void setNextSchedulableIds(List<String> nextSchedulableIds) {
        this.nextSchedulableIds = nextSchedulableIds;
    }

    public List<String> getSchedulableBoxIds() {
        return schedulableBoxIds;
    }

    public void setSchedulableBoxIds(List<String> schedulableBoxIds) {
        this.schedulableBoxIds = schedulableBoxIds;
    }

}
