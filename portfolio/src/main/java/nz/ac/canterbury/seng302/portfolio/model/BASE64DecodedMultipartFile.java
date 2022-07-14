package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 *<p>
 * Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
 * from a BASE64 encoded String
 * copied from https://stackoverflow.com/questions/18381928/how-to-convert-byte-array-to-multipartfile
 *</p>
 */
public class BASE64DecodedMultipartFile implements MultipartFile {
    private final byte[] imgContent;
    private final String imgType;

    // TODO fix constructor
    public BASE64DecodedMultipartFile(byte[] imgContent, String imgType) {
        this.imgContent = imgContent;
        this.imgType = imgType;
    }

    @Override
    public String getName() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public String getOriginalFilename() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public String getContentType() {
        // TODO this is bad
        return "image/jpeg";
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
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream(dest).write(imgContent);
    }
}
