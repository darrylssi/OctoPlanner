package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * <p>Utility class for dealing with roles.</p>
 * 
 * These methods are available inside Thymeleaf, using
 * <code>${#roles.[method](args)}</code>
 */
public class RoleUtils {
    
    // Using a singleton pattern, because SonarLint doesn't like it when a static
    // utility class get initialized, but `RolesExpressionDialect` NEEDS an instance.
    private static RoleUtils singleton = null;

    public static RoleUtils getInstance() {
        if (singleton == null)
            singleton = new RoleUtils();
        return singleton;
    }

    private RoleUtils() {}

    /**
     * <p>Gets the cleaner human-readable name of each role</p>
     * e.g. <code>COURSE_ADMINISTRATOR -> "Course Admin"</code>
     */
    public static String toName(UserRole role) {
        return switch (role) {
            case STUDENT -> "Student";
            case TEACHER -> "Teacher";
            case COURSE_ADMINISTRATOR -> "Course Admin";
            case UNRECOGNIZED -> "Unrecognized (!!! this shouldn't be here !!!)";
        };
    }

    /**
     * <p>Checks if this list has a role of equal or greater "importance" than the given role.</p>
     * 
     * Remember: Role order is <code>[Student < Teacher < CourseAdmin]</code>
     * 
     * @param userRoles The collection of roles the current user has.
     * @param targetRole The role barrier we need to pass
     * @return <code>true</code> if anything in the role list is equal or greater importance than <code>targetRole</code>
     */
    public static boolean hasRoleOfAtLeast(Collection<UserRole> userRoles, UserRole targetRole) {
        // No one can have an unrecognised role, plus it breaks otherwise
        if (targetRole == UserRole.UNRECOGNIZED)
            return false;
        return userRoles.stream()
            .anyMatch(role -> role.getNumber() >= targetRole.getNumber());
    }
    /**
     * <p>Checks if this user has a role of equal or greater "importance" than the given role.</p>
     * 
     * Remember: Role order is <code>[Student < Teacher < CourseAdmin]</code>
     * 
     * @param user The user you want to compare.
     * @param targetRole The role barrier we need to pass
     * @return <code>true</code> if the user has a role of equal or greater importance than <code>targetRole</code>
     */
    public static boolean hasRoleOfAtLeast(UserResponse user, UserRole targetRole) {
        Collection<UserRole> userRoles = user.getRolesList();
        return hasRoleOfAtLeast(userRoles, targetRole);
    }

    /**
     * Gets a UserRole from a string
     * @param sRole a string representing a UserRole
     * @return the UserRole that correlates to the string
     */
    public static UserRole fromString(String sRole) {
        return UserRole.valueOf(sRole.toUpperCase(Locale.ROOT));
    }

    /**
     * Checks if there are any roles that can be added to a user that they don't already have.
     * @param acceptableRoles the roles that the current user is able to add
     * @param userRoles the roles that the user being edited currently has
     * @return false if the user already has all roles that can be added
     */
    public static boolean canAddRoles(List<UserRole> acceptableRoles, List<UserRole> userRoles) {
        return !userRoles.containsAll(acceptableRoles);
    }
}
