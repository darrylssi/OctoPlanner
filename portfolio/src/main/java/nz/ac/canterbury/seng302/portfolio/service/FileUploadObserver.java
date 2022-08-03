package nz.ac.canterbury.seng302.portfolio.service;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUploadObserver implements StreamObserver<FileUploadStatusResponse> {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadObserver.class);


    private Boolean uploadSuccessful = null;
    private String uploadMessage = "";

    @Override
    public void onNext(FileUploadStatusResponse response) {
        uploadSuccessful = (response.getStatus() == FileUploadStatus.SUCCESS);
        uploadMessage = response.getMessage();
        logger.info("File upload status {}, Message: {}", response.getStatus(), response.getMessage());
    }

    public Boolean isUploadSuccessful() {
        return uploadSuccessful;
    }

    public String getUploadMessage() {
        return uploadMessage;
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
