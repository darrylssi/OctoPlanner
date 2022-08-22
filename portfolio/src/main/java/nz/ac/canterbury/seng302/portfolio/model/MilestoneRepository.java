package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository to query data related to milestone.
 */
@Repository
public interface MilestoneRepository extends CrudRepository<Milestone, Integer> {
    List<Milestone> findByMilestoneName(String milestoneName);
    Milestone findMilestoneById(int id);
    List<Milestone> findMilestoneByParentProjectId(int parentProjectId);
}
