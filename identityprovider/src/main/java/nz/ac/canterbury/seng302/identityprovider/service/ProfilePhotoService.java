package nz.ac.canterbury.seng302.identityprovider.service;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfilePhotoService {
    
    @Value("${http-endpoint}")
    private URI httpURL;

    @Value("${profile-image-uri}")
    private URI imageEndpoint;
    
    @Value("${profile-image-folder}")
    private Path profileImageFolder;

    private static final String IMAGE_FILENAME_FORMAT = "%d_photo.jpg";
    private static final String DEFAULT_PROFILE_IMAGE = "default-pfp.jpg";

    public String getUserProfileImageUrl(int id) {
        String userImageFilename = String.format(IMAGE_FILENAME_FORMAT, id);
        // The full URL where images are found (http://localhost:9000/media/images/)
        URI fullImageEndpoint = httpURL.resolve(imageEndpoint);
        
        // Does the user have a profile picture saved?
        // If the image exists in the folder, it's theirs.
        File imageInFolder = profileImageFolder.resolve(userImageFilename).toFile();
        if (imageInFolder.isFile()) {
            // They have a profile photo, use it.
            URI userImageUrl = fullImageEndpoint.resolve(userImageFilename);
            return userImageUrl.toString();
        } else {
            // Use the default image
            URI defaultImageUrl = httpURL.resolve(DEFAULT_PROFILE_IMAGE);
            return defaultImageUrl.toString();
        }
    }


}
