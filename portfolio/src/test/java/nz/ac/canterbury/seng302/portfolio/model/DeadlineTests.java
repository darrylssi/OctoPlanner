package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DeadlineTests {
    private static List<Sprint> sprintList = new ArrayList<Sprint>();

    private String defaultDeadlineColour = "#ff3823";

    /* Some helper functions for creation of deadlines */
    private Deadline createDeadlineOutsideSprints() throws Exception {
        Date deadlineDate = DateUtils.toDate("2022-02-04");
        int parentProjId = 5;
        return new Deadline(parentProjId, "Outside sprints", "", deadlineDate);
    }

    private Deadline createDeadlineInsideSprint() throws Exception {
        Date deadlineDate = DateUtils.toDate("2022-02-15");
        int parentProjId = 5;
        return new Deadline(parentProjId, "Inside sprints", "", deadlineDate);
    }


    @BeforeAll
    public static void createSprintList() {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        int parentProjId = 5;
        for(int i = 1; i < 6; i++) {
            Sprint tempSprint = new Sprint();
            tempSprint.setSprintLabel("Sprint " + i);
            tempSprint.setSprintName("Sprint " + i);
            tempSprint.setParentProjectId(parentProjId);
            tempSprint.setStartDateString("2022-0" + (i+1) + "-05");
            tempSprint.setEndDateString("2022-0" + (i+1) + "-24");
            tempSprint.setSprintColour("#00000" + i);
            sprintList.add(tempSprint);
        }
    }

    @Test
    void checkDeadlineWithoutSprintHasDefaultColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Deadline deadline = createDeadlineOutsideSprints();

        assertEquals(defaultDeadlineColour, deadline.determineColour(sprintList));
    }

    @Test
    void checkDeadlineInSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Deadline deadline = createDeadlineInsideSprint();

        assertEquals(sprint.getSprintColour(), deadline.determineColour(sprintList));
    }

    @Test
    void checkDeadlineInsideOfSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Deadline deadline = createDeadlineInsideSprint();
        deadline.setDate(DateUtils.toDate("2022-02-05"));

        assertEquals(sprint.getSprintColour(), deadline.determineColour(sprintList));
    }

    @Test
    void checkDeadlineEndOfSprintInheritsColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Deadline deadline = createDeadlineInsideSprint();
        deadline.setDate(DateUtils.toDate("2022-02-24"));

        assertEquals(sprint.getSprintColour(), deadline.determineColour(sprintList));
    }

    @Test
    void checkDeadlineOutsideOfSprintInheritDefaultColour() throws Exception {
        // Sprint list has five sprints with dates 05/month/2022 -- 24/month/2022 and months Feb -- Jul
        Sprint sprint = sprintList.get(0);
        Deadline deadline = createDeadlineInsideSprint();

        deadline.setDate(DateUtils.toDate("2022-02-25"));
        assertEquals(defaultDeadlineColour, deadline.determineColour(sprintList));
    }

}
