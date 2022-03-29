package nz.ac.canterbury.seng302.identityprovider.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import nz.ac.canterbury.seng302.identityprovider.model.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    User findByUsername(String username);

    List<User> findAll(Pageable pageable);
    User findById(int id);
}
