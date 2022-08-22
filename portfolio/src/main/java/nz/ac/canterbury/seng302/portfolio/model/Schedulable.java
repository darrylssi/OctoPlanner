package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

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

    String getType();

    String determineColour(List<Sprint> sprints, boolean end);

    @Override
    default int compareTo(Schedulable other) {
        return this.getStartDate().compareTo(other.getStartDate());
    }
}
