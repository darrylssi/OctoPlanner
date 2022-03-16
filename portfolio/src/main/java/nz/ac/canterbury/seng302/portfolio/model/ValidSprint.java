package nz.ac.canterbury.seng302.portfolio.model;


@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SprintValidator.java)
public @interface ValidSprint {
    String message() default "add message";
    Class<?>[] groups () default {};
    Class<? extends Payload>[] payload () default {};
}