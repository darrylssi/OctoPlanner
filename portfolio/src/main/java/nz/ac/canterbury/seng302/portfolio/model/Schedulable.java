package nz.ac.canterbury.seng302.portfolio.model;

import java.util.Date;

/**
 * An interface for objects that have a start and/or end date, and an ID, but aren't a Sprint or Project.
 * Basically, this is for Events, Deadlines, and Milestones.
 * Objects with only one date should return the same date for getStartDate and getEndDate.
 */
public interface Schedulable {
    Date getStartDate();
    Date getEndDate();
    int getId();
}
