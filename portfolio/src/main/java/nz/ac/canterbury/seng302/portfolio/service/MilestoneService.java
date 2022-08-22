package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class handles the business requirements that relates to milestones.
 */
@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository repository;

    /**
     * Get list of all milestones
     */
    public List<Milestone> getAllMilestones() {
        return (List<Milestone>) repository.findAll();
    }

    /**
     * Returns a list of all milestones that match the given name
     * @param name the milestone name to search for
     * @return list of matching milestones, or empty list if no matches
     */
    public List<Milestone> getMilestoneByName(String name) {
        return repository.findByMilestoneName(name);
    }

    /**
     * Returns a list of all milestones for a given project
     * @param id the id of the project to find milestones from
     * @return list of matching milestones, or empty list if no matches
     */
    public List<Milestone> getMilestoneByParentProjectId(int id) {
        return repository.findMilestoneByParentProjectId(id);
    }

    /**
     * Get a milestone object by its ID.
     * @param id ID of the milestone to get
     * @return Milestone Object
     * @throws ResponseStatusException When the milestone is not found
     */
    public Milestone getMilestoneById(Integer id) throws ResponseStatusException {
        Milestone milestone = repository.findMilestoneById(id);
        if (milestone != null) {
            return milestone;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found.");
        }
    }

    /**
     * Gets all the milestones that belong to a given project.
     */
    public List<Milestone> getMilestonesInProject(Integer id) {
        return repository.findMilestoneByParentProjectId(id);
    }

    /**
     * Deletes a milestone from the repository
     * @param milestoneId the id of the milestone to be deleted
     */
    public void deleteMilestone(int milestoneId) {
        repository.deleteById(milestoneId);
    }


    public void saveMilestone(Milestone milestone) {
        repository.save(milestone);
    }
}
