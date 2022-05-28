package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

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
     * 
     * @param role
     * @return
     */
    public static String toName(UserRole role) {
        return switch (role) {
            case STUDENT -> "Student";
            case TEACHER -> "Teacher";
            case COURSE_ADMINISTRATOR -> "Course Admin";
            case UNRECOGNIZED -> "Unrecognized (!!! this shouldn't be here !!!)";
        };
    }

    
}
