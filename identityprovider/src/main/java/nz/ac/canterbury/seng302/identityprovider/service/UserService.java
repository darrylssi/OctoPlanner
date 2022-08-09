package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
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
        userRepository.findAll().forEach(users::add);
        return users;
    }

    /**
     * @param page What "page" of the users you want. Affected by the ordering and page size
     * @param limit How many items are in a page
     * @param orderBy How the list is ordered.
     *                Your options are:
     *                  <ul>
     *                    <li><code>"name"</code> - Ordered by their first, middle, and last name alphabetically</li>
     *                    <li><code>"username"</code> - Ordered by their username alphabetically</li>
     *                    <li><code>"nickname"</code> - Ordered by their nickname alphabetically</li>
     *                    <li><code>"roles"</code> - Ordered by their highest permission role</li>
     *                  </ul>
     * @param isAscending Is the list in ascending or descending order
     * @return A list of users from that "page"
     * @throws IllegalArgumentException Thrown if the provided orderBy string isn't one of the valid options
     */
    public List<User> getUsersPaginated(int page, int limit, String orderBy, boolean isAscending) throws IllegalArgumentException {
        Sort sortBy = switch (orderBy) {
            case "name"     -> Sort.by("firstName").and(Sort.by("middleName")).and(Sort.by("lastName"));
            case "username" -> Sort.by("username");
            case "nickname" -> Sort.by("nickname");
            case "role"     -> JpaSort.unsafe("MAX(roles)");    // WARNING: Doesn't work when using H2, so we bypass it in UserAccountServerService.getPaginatedUsers
            default -> throw new IllegalArgumentException(String.format("Can not order users by '%s'", orderBy));
        };

        if (!isAscending) {
            sortBy = sortBy.descending();
        }

        Pageable pageable = PageRequest.of(page, limit, sortBy);

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
