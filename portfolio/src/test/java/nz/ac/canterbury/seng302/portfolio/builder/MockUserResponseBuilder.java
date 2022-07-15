package nz.ac.canterbury.seng302.portfolio.builder;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import com.google.protobuf.Timestamp;

import org.junit.jupiter.api.TestInfo;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * Builder class to help make building long gRPC user responses for testing
 */
public class MockUserResponseBuilder {

    // Pretend the account was built this time yesterday
    private final static Timestamp TIMESTAMP = Timestamp.newBuilder()
            .setSeconds(Instant.now().getEpochSecond() - (60l * 60l * 24l))
            .setNanos(Instant.now().getNano())
            .build();

    /**
     * Builds a UserResponse object with the given ID and role
     * <p>
     * All name fields (username, firstname, nickname...) will be set to
     * <code>role + id</code> e.g. STUDENT
     * </p>
     * 
     * @param role The (single) role the user will have.
     * @param id   The user ID of the user. Useful if distinguishing by ID is needed
     * @return A built userResponse object with all fields built.
     */
    public static UserResponse buildMockUserResponse(UserRole role, int id) {
        String sRole = role.toString() + id; // e.g. STUDENT1
        UserResponse.Builder reply = UserResponse.newBuilder()
                .setId(id)
                .setUsername(sRole)
                .setFirstName(sRole)
                .setMiddleName(sRole)
                .setLastName(sRole)
                .setNickname(sRole)
                .setPersonalPronouns("they/them")
                .setEmail(sRole + "@testmail.com")
                .setProfileImagePath("/")
                .addAllRoles(List.of(role))
                .setCreated(TIMESTAMP);
        return reply.build();
    }

    /**
     * Builds a UserResponse object from a test annotated with {@link WithMockPrincipal}.
     * <p>Method annotations take priority over class annotations</p>
     * @param beforeAllTestInfo A JUnit TestInfo unit, received from an &commat;BeforeAll method.
     * @return A UserResponse, with its fields set according to the test's WithMockPrincipal values.
     * @throws IllegalStateException If neither the test method, nor test class, have a WithMockPrincipal annotation.
     * @see MockUserResponseBuilder#buildMockUserResponse
     */
    public static UserResponse buildUserResponseFromMockPrincipalAnnotatedTest(TestInfo beforeAllTestInfo) {
        // The method's annotation has a higher priority than the class's annotation
        // If either of the following `.get()` calls fail, something's gone wrong.
        Method testMethod = beforeAllTestInfo.getTestMethod().get();
        WithMockPrincipal methodPrincipal = testMethod.getAnnotation(WithMockPrincipal.class);
        if (methodPrincipal != null) {
            return buildMockUserResponse(methodPrincipal.value(), methodPrincipal.id());
        }
        Class<?> testClass = beforeAllTestInfo.getTestClass().get();
        WithMockPrincipal classPrincipal = testClass.getAnnotation(WithMockPrincipal.class);
        if (classPrincipal != null) {
            return buildMockUserResponse(classPrincipal.value(), classPrincipal.id());
        }

        throw new IllegalStateException(
                String.format("Neither '%s' nor '%s' have a `@WithMockPrincipal` annotation.",
                        testClass.getName(), testMethod.getName()));
    }
}
