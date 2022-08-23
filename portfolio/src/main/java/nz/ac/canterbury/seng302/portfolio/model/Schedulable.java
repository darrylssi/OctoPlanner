package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.DEFAULT_COLOUR;

/**
 * An interface for objects that have a start and/or end date, and an ID, but aren't a Sprint or Project.
 * Basically, this is for Events, Deadlines, and Milestones.
 * Objects with only one date should return the same date for getStartDate and getEndDate.
 */
@Component
public interface Schedulable extends Comparable<Schedulable>{
    int getId();
    void setId(int id);
    Project getParentProject();
    void setParentProject(Project parentProject);
    String getName();
    void setName(String name);
    String getDescription();
    void setDescription(String description);
    Date getStartDate();
    void setStartDate(Date startDate);
    Date getEndDate();
    void setEndDate(Date endDate);

    String getType(); // TODO should this be an enum?

    /**
     * Determines the correct colour for this schedulable based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * overlaps the start date of the schedulable (or end date if the end parameter is true).
     * If it finds no sprint, it returns the default colour determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     * @param end {boolean} fetch the colour at the end of the schedulable, instead of the start.
     *            For some schedulables, the start and end date will be the same so the value of this
     *            parameter will not change the outcome.
     */
    default String determineColour(List<Sprint> sprints, boolean end) {
        Date comparisonDate = getStartDate();
        if (end) {
            comparisonDate = getEndDate();
        }

        for (Sprint checkedSprint : sprints) {
            Date sprintStart = checkedSprint.getSprintStartDate();
            Date sprintEnd = checkedSprint.getSprintEndDate();

            /* Sprints are assumed to be active on their start and end dates, so we also check for equality */
            if ((sprintStart.before(comparisonDate) || sprintStart.equals(comparisonDate)) &&
                    (sprintEnd.after(comparisonDate) || sprintEnd.equals(comparisonDate))) {
                return checkedSprint.getSprintColour();
            }
        }

        return DEFAULT_COLOUR;
    }

    @Override
    default int compareTo(Schedulable other) {
        return this.getStartDate().compareTo(other.getStartDate());
    }
}
