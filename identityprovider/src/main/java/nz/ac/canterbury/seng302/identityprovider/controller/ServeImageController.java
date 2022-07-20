package nz.ac.canterbury.seng302.identityprovider.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ServeImageController {

    private static final Logger logger = LoggerFactory.getLogger(ServeImageController.class);

    @Value("${profile-image-folder}")
    private Path avatarFolder;

    // https://www.baeldung.com/spring-mvc-image-media-data
    /**
     * Gets the requested file from the profile-image folder, and writes
     * it to the request.
     * 
     * Note: This method ONLY works if the file is a .jpg
     * 
     * Note #2: I know the method sucks, but I've spent 3 god damned hours
     * trying to get `Resource` or something else working, something that
     * doesn't care about the filetype, something that doesn't read the
     * whole file into memory first.
     * 
     * Unfortunately, any solution using `servletContext` will cause it
     * to look for the files in my AppData temp folder, and the other
     * solutions either don't work, or cause it to look inside the
     * /build/classes/ folder.
     * 
     * If you can get this working, power to you.
     * If you can't, fair enough.
     * 
     * Note #3: If we start accepting other file-types, this'll break.
     */
    @GetMapping(value = "${profile-image-uri}{filename}")
    public void profileImage(
            @PathVariable(name = "filename") String filename,
            HttpServletResponse response) throws IOException {
        // Construct the directory path
        Path pathToImage = avatarFolder.resolve(filename).normalize();
        logger.info("pathToImage={}", pathToImage);
        logger.info("avatarFolder={}", avatarFolder);

        if (!pathToImage.startsWith(avatarFolder)) { // Directory Traversal check
            response.setStatus(404);
            return;
        }

        File imageFile = Path.of(".").resolve(pathToImage).toFile();
        boolean fileExists = imageFile.isFile();
        if (!fileExists) {
            response.setStatus(404);
            return;
        }


        // Write the file
        try (InputStream fileStream = new FileInputStream(imageFile)) {
            IOUtils.copy(fileStream, response.getOutputStream());
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        } catch (FileNotFoundException e) {
            logger.info("Everything is fucked");
            response.setStatus(404);
            return;
        }

        response.setStatus(200);
    }

}
