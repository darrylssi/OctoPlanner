package nz.ac.canterbury.seng302.identityprovider.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;

/**
 * Implementation of the {@link MultipartFile} interface to wrap a base64 encoded string as a byte[] object.
 * Only intended to be used to upload user profile photos in EditUserController, hence some methods aren't
 * properly implemented.
 * Copied & adapted from https://stackoverflow.com/questions/18381928/how-to-convert-byte-array-to-multipartfile
 * TODO find a way to put this in shared so the file isn't duplicated in two different places
 * Only used for running tests of the user profile photo upload
 * Look at "gradle mark project as library"?
 */
public class Base64DecodedMultipartFile implements MultipartFile {
    private final byte[] imgContent;
    private final String imgType;

    /**
     * Constructor.
     * Converts a full base64 image string into a byte array and metadata and stores that in the object.
     * @param base64String full base64 image string including the content type metadata at the start, e.g. data:image/jpeg;base64,[image data]
     * @throws IllegalArgumentException if the base64 string cannot be split into two strings at a ',' character
     */
    public Base64DecodedMultipartFile(String base64String) throws IllegalArgumentException {
        String[] splitString = base64String.split(",");
        if (splitString.length != 2) {
            throw new IllegalArgumentException("Input must be a base 64 string that can be split into two strings at a ',' character.");
        } else {
            this.imgType = splitString[0];
            this.imgContent = DatatypeConverter.parseBase64Binary(splitString[1]);
        }
    }

    /**
     * Gets the content type of the base64 string.
     * Only expects jpeg or png, and will return null otherwise.
     * @return string of either "image/jpeg" or "image/png" based on the base64 string's metadata
     */
    @Override
    public String getContentType() {
        return switch (imgType) {
            case "data:image/jpeg;base64" -> "image/jpeg";
            case "data:image/png;base64" -> "image/png";
            case "data:@file/plain;base64" -> "@file/plain";
            default -> null;
        };
    }

    // these methods aren't intended to be used, but need to be implemented to satisfy the interface
    // use at your own risk!

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes() {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream stream = new FileOutputStream(dest)) {
            stream.write(imgContent);
        }
    }
}
