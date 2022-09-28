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
        if (groupId == MEMBERS_WITHOUT_GROUPS_ID) { // If trying to add to members without groups, do nothing
            return 0;
        }
        Group group = getGroup(groupId);
        int count = 0;
        for (User user : userRepository.findAllById(userIds)) {
            group.addMember(user);
            // If adding to teaching staff, give teacher role
            if (groupId == TEACHER_GROUP_ID) {
                user.addRole(UserRole.TEACHER);
                userRepository.save(user);
            }
            removeUserFromMembersWithoutAGroup(user);
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
            if (user.getGroups().isEmpty()) {
                addUserToMembersWithoutAGroup(user);
            }
            count++;
        }
        groupRepository.save(group);
        return count;
    }

    /**
     * Adds a user to the group "Members Without A Group"
     * @param user The user to add to the group
     */
    @Transactional
    public void addUserToMembersWithoutAGroup(User user) {
        Group membersWithoutAGroup = getGroup(MEMBERS_WITHOUT_GROUPS_ID);
        membersWithoutAGroup.addMember(user);
        groupRepository.save(membersWithoutAGroup);
        userRepository.save(user);
    }

    /**
     * Removes a user from the group "Members Without A Group"
     * @param user The user to remove from the group
     */
    public void removeUserFromMembersWithoutAGroup(User user) {
        Group membersWithoutAGroup = getGroup(MEMBERS_WITHOUT_GROUPS_ID);
        if (user.getGroups().contains(membersWithoutAGroup)) {
            membersWithoutAGroup.removeMember(user);
            groupRepository.save(membersWithoutAGroup);
            userRepository.save(user);
        }
    }

    /**
     * When the app starts up, populates the Teaching Staff group and Members Without A Group
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void populateDefaultGroups() {
        populateTeachingStaffGroup();
        populateMembersWithoutAGroup();
    }

    /**
     * Gets all users with the teacher role and adds them to the Teaching Staff group
     */
    @Transactional
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

    /**
     * Gets all users without a group and adds them to the group 'Members Without A Group'
     */
    @Transactional
    public void populateMembersWithoutAGroup() {
        Iterable<User> users = userRepository.findAll();
        Group membersWithoutAGroup;
        try {   // Wrapped in try catch to avoid causing problems when app is started with mock database
            membersWithoutAGroup = getGroup(MEMBERS_WITHOUT_GROUPS_ID);
        } catch (NoSuchElementException e) {
            return;
        }
        for (User user : users) {
            if (user.getGroups().isEmpty()) {
                membersWithoutAGroup.addMember(user);
            }
        }
        groupRepository.save(membersWithoutAGroup);
    }
}
