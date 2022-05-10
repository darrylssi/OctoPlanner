package nz.ac.canterbury.seng302.portfolio.factory;

import java.util.Locale;
import java.util.List;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.authentication.JwtAuthenticationFilter;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;

/**
 * Factory method for {@link WithSecurityContext} to mock the job of
 * {@link JwtAuthenticationFilter} for tests.
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockPrincipal> {

    // From identityprovider's JwtTokenUtil.java
    private static final String AUTHENTICATION_TYPE = "AuthenticationTypes.Federation";
    private static final String USERNAME_CLAIM_TYPE = "unique_name";
    private static final String ID_CLAIM_TYPE = "nameid";
    private static final String ROLE_CLAIM_TYPE = "role";
    private static final String FULLNAME_CLAIM_TYPE = "name";

    /**
     * <p>
     * Given {@link WithMockPrincipal}'s role, builds an AuthState object, and
     * makes a security context with said AuthState as the principal.
     * </p>
     * This context gets set for the controller tests, so the
     * AuthenticationPrincipal annotations can use something.
     */
    @Override
    public SecurityContext createSecurityContext(WithMockPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // If any more roles are added in future, add 'em here
        AuthState authState = switch (annotation.value()) {
            case COURSE_ADMINISTRATOR -> buildAuthState("Adien", COURSE_ADMINISTRATOR);
            case TEACHER -> buildAuthState("Thomas", TEACHER);
            case STUDENT -> buildAuthState("Song", STUDENT);
            case UNRECOGNIZED -> throw new IllegalArgumentException(
                    "Can't test an UNRECOGNIZED role, use STUDENT, TEACHER etc...s");
        };
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(authState, null);
        context.setAuthentication(authentication);

        return context;

    }

    /**
     * Builds an AuthState object with a name & role.
     * 
     * @param name The first name of the student. YAGNI and all that but it's fun
     * @param role The role of the student
     * @return A built AuthState object.
     */
    private AuthState buildAuthState(String name, UserRole role) {
        String roleString = role.toString().toLowerCase(Locale.ROOT);

        return AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType(FULLNAME_CLAIM_TYPE)
                .setRoleClaimType(ROLE_CLAIM_TYPE)
                .setAuthenticationType(AUTHENTICATION_TYPE)
                .addAllClaims(List.of(
                        makeClaimDTO(USERNAME_CLAIM_TYPE, name),
                        makeClaimDTO(ID_CLAIM_TYPE, "1"),
                        makeClaimDTO(ROLE_CLAIM_TYPE, roleString),
                        makeClaimDTO(FULLNAME_CLAIM_TYPE, name + ' ' + roleString)))
                .build();
    }

    private ClaimDTO makeClaimDTO(String key, String value) {
        return ClaimDTO.newBuilder()
                .setType(key)
                .setValue(value)
                .build();
    }

}