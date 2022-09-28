package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Represents a group object. Groups must have a parent project object that they are a part of.
 * Group objects are stored in a table called Group, as it is an @Entity.
 */
@Entity
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_project", nullable = false)
    private Project parentProject;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The group short name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    @NotBlank(message="Group short name is required.")
    private String groupShortName;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_GROUP_LONG_NAME_LENGTH,
            message="The group long name must be between " + MIN_NAME_LENGTH + " and " + MAX_GROUP_LONG_NAME_LENGTH + " characters.")
    @NotBlank(message="Group long name is required.")
    private String groupLongName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        this.parentProject = parentProject;
    }

    public String getGroupShortName() {
        return groupShortName;
    }

    public void setGroupShortName(String groupShortName) {
        this.groupShortName = groupShortName;
    }

    public String getGroupLongName() {
        return groupLongName;
    }

    public void setGroupLongName(String groupLongName) {
        this.groupLongName = groupLongName;
    }
}
