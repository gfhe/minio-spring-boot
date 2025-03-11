package win.hgfdodo.minio.service.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.print.attribute.standard.Media;

import static org.junit.jupiter.api.Assertions.*;

class MediaTypeFactoryTest {

    private static final String FILENAME_MP3 = "aaa.mp4";
    private static final String FILENAME_JPG = "aaa.jpg";
    private static final String FILENAME_PNG = "aaa.png";

    @Test
    public void test() {
        MediaType mediaType = MediaTypeFactory.getMediaType(FILENAME_JPG).orElseThrow();
        Assertions.assertEquals("image/jpeg", mediaType.toString());
        mediaType = MediaTypeFactory.getMediaType(FILENAME_PNG).orElseThrow();
        Assertions.assertEquals("image/png", mediaType.toString());
        mediaType = MediaTypeFactory.getMediaType(FILENAME_MP3).orElseThrow();
        Assertions.assertEquals("video/mp4", mediaType.toString());
    }

    @Test
    void testImage() {
        MediaType mediaType = MediaTypeFactory.getMediaType(FILENAME_JPG).orElseThrow();
        Assertions.assertEquals(true, mediaType.isPicture());
    }

}