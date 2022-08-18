package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class handles the business requirements that relates to milestones.
 */
@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository repository;

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

    public void saveMilestone(Milestone milestone) {
        repository.save(milestone);
    }
}
