package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.Map;
import java.util.stream.Collectors;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;

/*
 * Note for anyone trying to extend this in future:
 * The IdP DOES put information about the current user in the token
 * (specifically the username, ID, roles, and full name).
 * HOWEVER, this token is generated *on login*, and *never* updated.
 * "fullname" and "roles" are mutable, so if you want their values,
 * ask the IdP directly, as the JWT information could get out-of-sync.
*/

/**
 * Class for extracting information out of an AuthState principal.
 */
public class PrincipalData {
    
    private boolean authenticated;
    private int id;
    private String username;

    private static final String CLAIM_ID_TYPE = "nameid";
    private static final String CLAIM_USERNAME_TYPE = "unique_name";


    protected PrincipalData(int id, String username) {
        this.authenticated = true;
        this.id = id;
        this.username = username;
    }

    /**
     * An empty initialiser, used when the given principal is for a non-authenticated user
     */
    protected PrincipalData() {
        this.authenticated = false;
        this.id = -1;
    }

    public boolean isAuthenticated() { return authenticated; }
    public int getID()               { return id; }
    public String getUsername()      { return username; }

    /* There are no setters, this is just a parser class */

    /**
     * Parses relevant data from a AuthState's DTOs
     * 
     * @param principal The authentication principal you want to makes sense of.
     * @return A PrincipalData class, containing relevant data from the 
     */
    public static PrincipalData from(AuthState principal) {
        // Return an empty class if unauthenticated
        if (!principal.getIsAuthenticated()) {
            return new PrincipalData();
        }
        // Convert to a dictionary, so we don't stream filter 5 times.
        Map<String, String> principalValues = principal
            .getClaimsList()
            .stream()
            .collect(Collectors.toMap(ClaimDTO::getType, ClaimDTO::getValue));
        
        // And, get the only immutable values we have any use for.
        int id = Integer.parseInt(principalValues.get(CLAIM_ID_TYPE));
        String username = principalValues.get(CLAIM_USERNAME_TYPE);
        
        return new PrincipalData(id, username);
    }
}
