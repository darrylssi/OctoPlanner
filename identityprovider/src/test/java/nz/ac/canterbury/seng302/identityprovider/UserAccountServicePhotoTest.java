package nz.ac.canterbury.seng302.identityprovider;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.utils.Base64DecodedMultipartFile;
import nz.ac.canterbury.seng302.identityprovider.service.UserAccountServerService;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * Holds tests for the photo-related methods in {@link UserAccountServerService}.
 * The tests cover all cases except those with unexpected exceptions.
 */
@SpringBootTest
@DirtiesContext
@SuppressWarnings("unchecked")
class UserAccountServicePhotoTest {

    @Autowired
    private UserAccountServerService userAccountServerService;

    private static final String BASE64_PREFIX_JPG = "data:image/jpeg;base64,";
    private static final String BASE64_PREFIX_PNG = "data:image/png;base64,";
    private static final String BASE64_PREFIX_TXT = "data:@file/plain;base64,";
    private static final int TEST_PHOTO_USERID = 1;
    private static final String TEST_PHOTO_NAME = "/" + TEST_PHOTO_USERID + "_photo.";
    private static final String TEST_PHOTO_FORMAT_JPG = "jpg";
    private static final String TEST_PHOTO_FORMAT_PNG = "png";
    private static final int TEST_PHOTO_DIMENSIONS = 200;

    @Value("${profile-image-folder}")
    private Path profileImageFolder;

    @AfterEach
    @BeforeEach
    void setup() throws IOException {
        Files.deleteIfExists(profileImageFolder.resolve(TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG));
        Files.deleteIfExists(profileImageFolder.resolve(TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_PNG));
    }

    /**
     * Creates a square jpg image saved in the photo folder for the test user id.
     */
    void createTestUserImage(int dimensions) throws IOException {
        BufferedImage image = new BufferedImage(dimensions, dimensions, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, TEST_PHOTO_FORMAT_JPG, new File( profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG));
    }

    /**
     * Creates a blank, square image of custom size and format, and packs it into a MultipartFile for sending over GRPC.
     * @param imageSize width and height of image
     * @param format format string for the image; jpg or png
     * @return MultipartFile with this image enclosed
     * @throws IOException if an error occurs when writing the image
     */
    MultipartFile getTestUserImageMultipartFile(int imageSize, String format) throws IOException {
        BufferedImage newImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(newImage, format, outputStream);
        byte[] fileBytes = outputStream.toByteArray();
        String base64ImageString = DatatypeConverter.printBase64Binary(fileBytes);

        return switch (format) {
            case "png" -> new Base64DecodedMultipartFile(BASE64_PREFIX_PNG + base64ImageString);
            case "jpg" -> new Base64DecodedMultipartFile(BASE64_PREFIX_JPG + base64ImageString);
            default -> throw new IllegalArgumentException("Error running UserAccountServiceTest: getTestUserImageMultipartFile parameter 'format' must be 'jpg' or 'png'");
        };
    }

    /**
     * Encodes some text into a multipart file in base64 format. Should come out as a .txt... TODO
     * @param text some text that will be encoded into a MultipartFile
     * @return MultipartFile with this string enclosed
     */
    MultipartFile getMultipartFileFromString(String text) {
        return new Base64DecodedMultipartFile(BASE64_PREFIX_TXT + DatatypeConverter.printBase64Binary(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void deleteNonexistentPhoto_getFailure() {
        /* Given: There is no photo for the given user */

        /* When: A request is received to delete the user's photo */
        DeleteUserProfilePhotoRequest request = DeleteUserProfilePhotoRequest.newBuilder()
                .setUserId(TEST_PHOTO_USERID)
                .build();
        StreamObserver<DeleteUserProfilePhotoResponse> observer = mock(StreamObserver.class);
        userAccountServerService.deleteUserProfilePhoto(request, observer);

        /* Then: The response will be a failure */
        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<DeleteUserProfilePhotoResponse> captor = ArgumentCaptor.forClass(
                DeleteUserProfilePhotoResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        DeleteUserProfilePhotoResponse response = captor.getValue();

        assertFalse(response.getIsSuccess(), response.getMessage());

        assertNotNull(response.getMessage());
        assertNotEquals("", response.getMessage());
    }

    @Test
    void deleteExistingPhoto_getSuccessAndFileDoesNotExist() throws IOException {
        /* Given: There is a photo for the given user */
        createTestUserImage(TEST_PHOTO_DIMENSIONS);

        /* When: A request is received to delete the user's photo */
        DeleteUserProfilePhotoRequest request = DeleteUserProfilePhotoRequest.newBuilder()
                .setUserId(TEST_PHOTO_USERID)
                .build();
        StreamObserver<DeleteUserProfilePhotoResponse> observer = mock(StreamObserver.class);
        userAccountServerService.deleteUserProfilePhoto(request, observer);

        /* Then: The response will be a success and the file does not exist */
        verify(observer, times(1)).onCompleted();
        ArgumentCaptor<DeleteUserProfilePhotoResponse> captor = ArgumentCaptor.forClass(
                DeleteUserProfilePhotoResponse.class);
        verify(observer, times(1)).onNext(captor.capture());
        DeleteUserProfilePhotoResponse response = captor.getValue();

        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        assertFalse(pfp.exists());

        assertTrue(response.getIsSuccess());
    }

    @Test
    void addNewPhotoJpg_getSuccessAndFileExists() throws IOException {
        /* Given: There is no photo for the given user */

        /* When: a request is received to create a photo for the user */
        // This code is just copied from UserAccountClientService.java
        MultipartFile file = getTestUserImageMultipartFile(TEST_PHOTO_DIMENSIONS, TEST_PHOTO_FORMAT_JPG);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file exists and the response will be a success */
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        assertTrue(pfp.exists());
        pfp.delete();

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.SUCCESS, response.getStatus());
    }

    @Test
    void addNewPhotoPng_getSuccessAndFileExists() throws IOException {
        /* Given: There is no photo for the given user */

        /* When: a request is received to create a photo for the user */
        // This code is just copied from UserAccountClientService.java
        MultipartFile file = getTestUserImageMultipartFile(TEST_PHOTO_DIMENSIONS, TEST_PHOTO_FORMAT_PNG);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file exists and the response will be a success */
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        assertTrue(pfp.exists());
        pfp.delete();

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.SUCCESS, response.getStatus());
    }

    @Test
    void replaceExistingPhotoJpg_getSuccessAndFileDifferent() throws IOException {
        /* Given: There is an existing photo for the given user */
        // Note that this image is 100x100, so will be smaller than the one created by getTestUserImageMultipartFile()!
        createTestUserImage(100);
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        assertTrue(pfp.exists());
        Long originalFileSize = pfp.length(); // save this to check that it is different

        /* When: a request is received to create a photo for the user */
        // This code is just copied from UserAccountClientService.java
        MultipartFile file = getTestUserImageMultipartFile(200, TEST_PHOTO_FORMAT_JPG);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file will be different (different size), and the response will be a success */
        assertTrue(pfp.exists());
        assertNotEquals(originalFileSize, pfp.length());
        System.out.println(originalFileSize.toString() + pfp.length());
        pfp.delete();

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.SUCCESS, response.getStatus());
    }

    @Test
    void addNewPhotoJpgInvalidSize_getFailAndFileDoesNotExist() throws IOException {
        /* Given: There is no photo for the given user */

        /* When: a request is received to create a photo for the user */
        MultipartFile file = getTestUserImageMultipartFile(500, TEST_PHOTO_FORMAT_JPG);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file doesn't exist and the response will be a failure */
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        assertFalse(pfp.exists());

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.FAILED, response.getStatus());
    }


    @Test
    void uploadTxtFile_getFailAndFileDoesNotExist() throws IOException {
        /* Given: There is no photo for the given user */

        /* When: a request is received to upload a txt file for the user */
        MultipartFile file = getMultipartFileFromString("Lorem ipsum");

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(TEST_PHOTO_USERID)
                        .setFileType(filetype)
                        .build())
                .build();
        StreamObserver<FileUploadStatusResponse> responseStreamObserver = mock(StreamObserver.class);
        StreamObserver<UploadUserProfilePhotoRequest> requestStreamObserver = userAccountServerService.uploadUserProfilePhoto(responseStreamObserver);

        requestStreamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            assertTrue(uploadRequest.hasFileContent());
            requestStreamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        requestStreamObserver.onCompleted();

        /* Then: The file doesn't exist in either format and the response will be a failure */
        File pfp = new File(profileImageFolder + TEST_PHOTO_NAME + TEST_PHOTO_FORMAT_JPG);
        File txt = new File(profileImageFolder + TEST_PHOTO_NAME + "txt");
        assertFalse(pfp.exists());
        assertFalse(txt.exists());

        verify(responseStreamObserver, times(1)).onCompleted();
        ArgumentCaptor<FileUploadStatusResponse> captor = ArgumentCaptor.forClass(
                FileUploadStatusResponse.class);
        verify(responseStreamObserver, times(1)).onNext(captor.capture());
        FileUploadStatusResponse response = captor.getValue();

        assertEquals(FileUploadStatus.FAILED, response.getStatus());
    }
}
