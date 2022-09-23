package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.MEMBERS_WITHOUT_GROUPS_ID;
import static nz.ac.canterbury.seng302.identityprovider.utils.GlobalVars.TEACHER_GROUP_ID;


@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets all groups from the repository
     * @return A list of all groups in the repository
     */
    public List<Group> getAllGroups()
    {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAll().forEach(groups::add);
        return groups;
    }

    /**
     * Gets a specific group from the repository
     * @param id The id of the group to retrieve
     * @return The group with the specified id, or <code>null</code>
     */
    public Group getGroup(int id) {
        Group group = groupRepository.findById(id);
        if (group != null) {
            return group;
        } else {
            throw new NoSuchElementException("There is no group with id " + id);
        }
    }

    /**
     * Adds a set of users to a group
     * @param groupId The id of the group to add users to
     * @param userIds The ids of the users to add to the group
     * @return The number of users added to the group
     */
    @Transactional
    public int addUsersToGroup(int groupId, List<Integer> userIds) {
        // TODO if users were in members without groups, then they need to be removed from it
        if (groupId == MEMBERS_WITHOUT_GROUPS_ID) { // If trying to add to members without groups, do nothing
            return 0;
        }

        Group group = getGroup(groupId);
        int count = 0;
        for (User user : userRepository.findAllById(userIds)) {
            group.addMember(user);
            if (groupId == TEACHER_GROUP_ID) {  // If adding to teaching staff, give teacher role
                user.addRole(UserRole.TEACHER);
                userRepository.save(user);
            }
            count++;
        }
        groupRepository.save(group);
        return count;
    }

    /**
     * Removes a set of users from a group
     * @param groupId The id of the group to remove users from
     * @param userIds The ids of the users to remove from the group
     * @return The number of users removed from the group
     */
    @Transactional
    public int removeUsersFromGroup(int groupId, List<Integer> userIds) {
        // TODO check that the group being removed from isn't a special group - needs to be special cases for that
        if (groupId == MEMBERS_WITHOUT_GROUPS_ID) { // If trying to remove from members without groups, do nothing
            return 0;
        }

        Group group = getGroup(groupId);
        int count = 0;
        for (User user : userRepository.findAllById(userIds)) {
            group.removeMember(user);
            if (groupId == TEACHER_GROUP_ID) {  // If removing from teaching staff, remove teacher role
                if (user.getRoles().size() == 1) {
                    user.addRole(UserRole.STUDENT);
                }
                user.removeRole(UserRole.TEACHER);
                userRepository.save(user);
            }
            count++;
        }
        groupRepository.save(group);
        return count;
    }

    /**
     * When the app starts up, gets all users with the teacher role and adds them to the teaching staff group
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void populateTeachingStaffGroup() {
        List<User> teachers = userRepository.findAllByRoles(UserRole.TEACHER);
        Group teachingStaff;
        try {   // Wrapped in try catch to avoid causing problems when app is started with mock database
            teachingStaff = getGroup(TEACHER_GROUP_ID);
        } catch (NoSuchElementException e) {
            return;
        }
        teachingStaff.setMembers(new HashSet<>(teachers));
        groupRepository.save(teachingStaff);
    }
}
