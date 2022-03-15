package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;

@GrpcService
public class UserAccountServerService extends UserAccountServiceImplBase {

    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();

        // TODO create a user object with the fields from request

        // User user = new User(...bunch of fields...)

        reply
                .setIsSuccess(true)
                .setNewUserId(0) // user.getId() instead of 0
                .setMessage("some message");


        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }
}
