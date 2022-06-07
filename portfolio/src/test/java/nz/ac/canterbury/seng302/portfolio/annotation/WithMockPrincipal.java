package nz.ac.canterbury.seng302.portfolio.annotation;

import java.lang.annotation.*;

import org.springframework.security.test.context.support.WithSecurityContext;

import nz.ac.canterbury.seng302.portfolio.ProjectControllerTest;
import nz.ac.canterbury.seng302.portfolio.builder.MockUserResponseBuilder;
import nz.ac.canterbury.seng302.portfolio.factory.WithMockCustomUserSecurityContextFactory;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * <p>
 * <b>Annotation for mocking the AuthState principal in controller testing.</b>
 * </p>
 * <p>
 * You can either annotate each test method, or the entire test class to apply
 * them to all. See <a href=
 * "https://docs.spring.io/spring-security/site/docs/4.2.x/reference/html/test-method.html">
 * the docs</a> for info
 * </p>
 * <p>
 * Note: Authentication is done by checking the gRPC for a user's roles: {@link UserAccountClientService#getUserAccountById(int)}.
 * If you're getting null exceptions, you'll also need to <b>mock that method</b>.
 * See {@link ProjectControllerTest#before(org.junit.jupiter.api.TestInfo)} for an example.
 * </p>
 * @see MockUserResponseBuilder#buildUserResponseFromMockPrincipalAnnotatedTest
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockPrincipal {

    /**
     * <p>
     * The single role you want to give to the mocked user.
     */
    public UserRole value();

    /**
     * The ID of the current mocked user. Defaults to -1 so it doesn't collide.
     */
    public int id() default -1;
}
