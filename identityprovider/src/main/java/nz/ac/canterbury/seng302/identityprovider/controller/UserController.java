package nz.ac.canterbury.seng302.identityprovider.controller;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    /**
     * Gets a list of all users in the database
     * @return A list of users
     */
    @GetMapping("/users")
    private List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    /**
     * Gets a specific user from the database
     * @param id The id number of the user to retrieve
     * @return The user with the specified id
     */
    @GetMapping("/users/{id}")
    private User getUser(@PathVariable("id") int id)
    {
        return userService.getUser(id);
    }

    /**
     * Deletes a specific user from the database
     * @param id The id number of the user to delete
     */
    @DeleteMapping("/users/{id}")
    private void deleteUser(@PathVariable("id") int id)
    {
        userService.delete(id);
    }

    /**
     * Adds a user to the database
     * @param user The new user to add to the database
     * @return The ID of the user that was added
     */
    @PostMapping("/users")
    private int saveUser(@RequestBody User user)
    {
        userService.saveOrUpdate(user);
        return user.getID();
    }
}

