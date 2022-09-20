package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
            throw new IllegalArgumentException("There is no group with id " + id);
        }
    }

    /**
     * Adds a set of users to a group
     * @param groupId The id of the group to add users to
     * @param userIds The ids of the users to add to the group
     */
    public void addUsersToGroup(int groupId, Set<Integer> userIds) {
        /* TODO check that the group being added to is not members without groups
        also remove users from members without groups if they are being added to a group that is not that
         */
        Group group = getGroup(groupId);
        Set<User> users = new HashSet<>((Collection<? extends User>) userRepository.findAllById(userIds));
        for (User user : users) {
            if (user != null) {
                group.addMember(user);
            }
        }
        groupRepository.save(group);
    }

    /**
     * Removes a set of users from a group
     * @param groupId The id of the group to remove users from
     * @param userIds The ids of the users to remove from the group
     */
    public void removeUsersFromGroup(int groupId, Set<Integer> userIds) {
        // TODO check that the group being removed from isnt a special group - needs to be special cases for that
        Group group = getGroup(groupId);
        Set<User> users = (Set<User>) userRepository.findAllById(userIds);
        for (User user : users) {
            if (user != null) {
                group.removeMember(user);
            }
        }
        groupRepository.save(group);
    }
}
