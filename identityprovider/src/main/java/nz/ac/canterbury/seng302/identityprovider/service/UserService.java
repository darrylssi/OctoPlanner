package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets all users from the repository
     * @return A list of all users in the repository
     */
    public List<User> getAllUsers()
    {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> users.add(user));
        return users;
    }

    public List<User> getUsersPaginated(int page, int size, String orderBy, boolean isAscending) {
        Pageable pageable;
        if (isAscending) {
            if (orderBy.equals("name"))
                pageable = PageRequest.of(page, size, Sort.by("firstName").and(Sort.by("middleName")).and(Sort.by("lastName")));
            else
                pageable = PageRequest.of(page, size, Sort.by(orderBy));
        } else {
            if (orderBy.equals("name"))
                pageable = PageRequest.of(page, size, Sort.by("firstName").descending()
                        .and(Sort.by("middleName")).descending().and(Sort.by("lastName")).descending());
            else
                pageable = PageRequest.of(page, size, Sort.by(orderBy).descending());
        }
        return userRepository.findAll(pageable);
    }

    /**
     * Gets a specific user from the repository
     * @param id The id of the user to retrieve
     * @return The user with the specified id, or <code>null</code>
     */
    public User getUser(int id) {
        return userRepository.findById(id);
    }

    /**
     * Gets a specific user from the repository
     * @param username The username of the user to retrieve
     * @return The user with the specified username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Assigns a role to a user.
     * 
     * @param id The ID of the user getting a new role
     * @param role The string representation of a role. (STUDENT, TEACHER, COURSEADMINISTRATOR)
     * @throws NoSuchElementException The ID does not match any user
     * @return <code>true</code> if the user didn't already have this role
     */
    public boolean addRoleToUser(int id, UserRole role) throws NoSuchElementException {
        User user = getUser(id);            // Throw NoSuchElementException if the user ID is wrong
        if (user == null) {
            throw new NoSuchElementException("No user has an ID of " + id);
        }
        boolean ret = user.addRole(role);
        userRepository.save(user);
        return ret;
    }
    
    /**
     * Removes a role from a user. Will not remove role if it's their only role.
     * 
     * @param id The ID of the user getting a new role
     * @param role The string representation of a role. (STUDENT, TEACHER, COURSEADMINISTRATOR)
     * @throws NoSuchElementException The ID does not match any user
     * @return <code>false</code> if it's their only role, or they don't have the role. <code>true</code> otherwise
     */
    public boolean removeRoleFromUser(int id, UserRole role) throws NoSuchElementException {
        boolean success;
        User user = getUser(id);            // Throw NoSuchElementException if the user ID is wrong
        if (user == null) {
            throw new NoSuchElementException("No user has an ID of " + id);
        }
        // Can't delete their last role
        if (user.getRoles().size() <= 1) {
            success = false;
        } else {
            success = user.removeRole(role);
            userRepository.save(user);
        }
        return success;
    }

    /**
     * Saves a user to the repository
     * @param user The user object to save to the repository
     */
    public void saveOrUpdate(User user)
    {
        userRepository.save(user);
    }

    /**
     * Deletes a specific user from the repository
     * @param id The id of the user to delete
     */
    public void delete(int id)
    {
        userRepository.deleteById(id);
    }
    
}
