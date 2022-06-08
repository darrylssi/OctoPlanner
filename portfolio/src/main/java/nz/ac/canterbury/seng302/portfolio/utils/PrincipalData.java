package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;

public class PrincipalData {
    
    private boolean authenticated;
    private Integer id;
    private String username;
    private String fullname;
    private List<UserRole> roles;

    private static final String CLAIM_ID_TYPE = "nameid";
    private static final String CLAIM_USERNAME_TYPE = "unique_name";


    protected PrincipalData(int id, String username, String fullName, List<UserRole> roles) {
        this.authenticated = true;
        this.id = id;
        this.username = username;
        this.fullname = fullName;
        this.roles = roles;
    }

    /**
     * An empty initialiser, used when the given principal is for a non-authenticated user
     */
    protected PrincipalData() {
        this.authenticated = false;
        this.id = -1;
        this.roles = List.of();
    }

    public boolean isAuthenticated()            { return authenticated; }
    public Integer getID()                      { return id; }
    @Nullable public String getUsername()       { return username; }
    @Nullable public String getFullName()       { return fullname; }
    @Nullable public List<UserRole> getRoles()  { return roles; }

    /* There are no setters, this is just a parser class */

    /**
     * Parses relevant data from a AuthState's DTOs
     * 
     * @param principal The authentication principal you want to makes sense of.
     * @return A PrincipalData class, containing relevant data from the principal,
     *          or an unauthenticated user.
     * @throws NullPointerException If the provided AuthState is null.
     */
    public static PrincipalData from(AuthState principal) throws NullPointerException {
        if (principal == null)
            throw new NullPointerException("principal is null");
        // Return an empty class if unauthenticated
        if (!principal.getIsAuthenticated()) {
            return new PrincipalData();
        }
        // Convert to a dictionary, so we don't stream filter 5 times.
        Map<String, String> principalValues = principal
            .getClaimsList()
            .stream()
            .collect(Collectors.toMap(ClaimDTO::getType, ClaimDTO::getValue));
        
        // AuthState contains the DTO names for fullname & roles, for some reason
        String claim_fullname_type = principal.getNameClaimType();
        String claim_role_type = principal.getRoleClaimType();

        int id = Integer.valueOf(principalValues.get(CLAIM_ID_TYPE));
        String username = principalValues.get(CLAIM_USERNAME_TYPE);
        String fullname = principalValues.get(claim_fullname_type);
        String stringRoles = principalValues.get(claim_role_type);
        // It's saved as a lower-case comma-separated list, let's fix that.
        List<UserRole> roles = List.of(stringRoles.split(","))
            .stream()
            .map(role -> UserRole.valueOf(role.toUpperCase(Locale.ROOT)))
            .toList();
        
        return new PrincipalData(id, username, fullname, roles);
    }

    /**
     * Generates an unauthenticated principal.
     * 
     * @return PrincipalData object with the fields unset,
     * where isAuthenticated is <code>true</code>
     */
    public static PrincipalData unauthenticated() {
        return new PrincipalData();
    }


    /**
     * Checks if this user has a role of equal or greater "importance" than the given role.
     * <br/>
     * Remember: Role order is <code>[Student < Teacher < CourseAdmin]</code>
     * <p><b>Note: </b>if the user isn't authenticated, it will always return false</p>
     * 
     * @param targetRole The role barrier we need to pass
     * @return <code>true</code> if this user has a role of equal or greater importance than <code>targetRole</code>
     */
    public boolean hasRoleOfAtLeast(UserRole targetRole) {
        // No one can have an unrecognised role
        if (targetRole == UserRole.UNRECOGNIZED) return false;
        return roles.stream()
            .anyMatch(role -> role.getNumber() >= targetRole.getNumber());
    }

}
