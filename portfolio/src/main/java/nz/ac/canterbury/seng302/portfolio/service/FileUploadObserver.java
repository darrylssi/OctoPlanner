package nz.ac.canterbury.seng302.portfolio.service;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;

public class FileUploadObserver implements StreamObserver<FileUploadStatusResponse> {


    @Override
    public void onNext(FileUploadStatusResponse response) {
        System.out.println(
                "File Upload status : " + response.getStatus()
        );
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
