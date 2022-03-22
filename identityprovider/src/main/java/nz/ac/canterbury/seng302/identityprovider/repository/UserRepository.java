package nz.ac.canterbury.seng302.identityprovider.repository;

import org.springframework.data.repository.CrudRepository;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    User findByUsername(String username);
}
