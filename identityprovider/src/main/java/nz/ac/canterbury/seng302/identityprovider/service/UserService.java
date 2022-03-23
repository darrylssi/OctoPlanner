package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException.NotImplemented;

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

    /**
     * Gets a specific user from the repository
     * @param id The id of the user to retrieve
     * @return The user with the specified id
     */
    public User getUser(int id)
    {
        return userRepository.findById(id).get();
    }

    /**
     * Gets a specific user from the repository
     * @param username The username of the user to retrieve
     * @return The user with the specified username
     */
    public User getUserByUsername(String username)
    {
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
        User user = getUser(id);            // Throws NoSuchElementException if the user ID is wrong
        return user.getRoles().add(role);
    }
    
    /**
     * Removes a role from a user.
     * 
     * @param id The ID of the user getting a new role
     * @param role The string representation of a role. (STUDENT, TEACHER, COURSEADMINISTRATOR)
     * @throws NoSuchElementException The ID does not match any user
     * @return <code>true</code> if the user had this role
     */
    public boolean removeRoleFromUser(int id, UserRole role) throws NoSuchElementException {
        User user = getUser(id);            // Throws NoSuchElementException if the user ID is wrong
        return user.getRoles().remove(role);
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
