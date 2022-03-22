package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<User> getUsersPaginated(int page, int size, String orderBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(orderBy));
        return userRepository.findAll(pageable);
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
