package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends CrudRepository<Sprint, Integer> {
    List<Sprint> findBySprintName(String sprintName);
    Sprint findSprintById(int id);  // renamed to avoid returning an <Optional>
    List<Sprint> findByParentProjectId(int parentProjectId);
}
