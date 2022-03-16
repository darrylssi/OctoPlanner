package nz.ac.canterbury.seng302.portfolio.model;

public class ProjectValidator implements ConstraintValidator<ValidProject, Project> {
    @Override
    public boolean isValid (Project project, ConstraintValidatorContext context) {
        return (project.getStartDate().before(project.getEndDate()));
    }
}