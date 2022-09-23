package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.authentication.AuthenticationServerInterceptor;
import nz.ac.canterbury.seng302.identityprovider.authentication.JwtTokenUtil;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc.AuthenticationServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@GrpcService
public class AuthenticateServerService extends AuthenticationServiceImplBase{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticateServerService.class);
    private static final String LOGIN_FAIL_MSG_FORMAT = "Login attempt by {} failed: incorrect username or password";

    @Autowired
    private UserService userService;

    private JwtTokenUtil jwtTokenService = JwtTokenUtil.getInstance();

    /**
     * Attempts to authenticate a user with a given username and password. 
     */
    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
        AuthenticateResponse.Builder reply = AuthenticateResponse.newBuilder();

        User user = userService.getUserByUsername(request.getUsername());
        logger.info("authenticate() has been called by {}", request.getUsername());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (user == null || !encoder.matches(request.getPassword(), user.getPassword())) {
            logger.info(LOGIN_FAIL_MSG_FORMAT, request.getUsername());
            reply
            .setMessage("Username or password is incorrect.")
            .setSuccess(false)
            .setToken("");
        } else if (request.getUsername().equals(user.getUsername())) {
            logger.info("Login attempt by {} succeeded", request.getUsername());
            String token = jwtTokenService.generateTokenForUser(user);
            reply
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setMessage("Logged in successfully!")
                .setSuccess(true)
                .setToken(token)
                .setUserId(user.getID())
                .setUsername(user.getUsername());
        } else {
            logger.info(LOGIN_FAIL_MSG_FORMAT, request.getUsername());
            reply
            .setMessage("Log in attempt failed: username or password incorrect")
            .setSuccess(false)
            .setToken("");
        }
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * The AuthenticationInterceptor already handles validating the authState for us, so here we just need to
     * retrieve that from the current context and return it in the gRPC body
     */
    @Override
    public void checkAuthState(Empty request, StreamObserver<AuthState> responseObserver) {
        responseObserver.onNext(AuthenticationServerInterceptor.AUTH_STATE.get());
        responseObserver.onCompleted();
    }
}
