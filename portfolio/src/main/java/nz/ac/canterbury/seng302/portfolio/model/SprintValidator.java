package nz.ac.canterbury.seng302.portfolio.model;

public class SprintValidator implements ConstraintValidator<ValidSprint, Sprint> {
    @Override
    public boolean isValid (Sprint sprint, ConstraintValidatorContext context) {
        return (sprint.getStartDate().before(sprint.getEndDate()));
    }
}