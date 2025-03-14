package win.hgfdodo.minio.service.http;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MediaTypeFactory {

    private static final String MIME_TYPES_FILE_NAME = "/mime.types";

    private static final MultiValueMap<String, MediaType> fileExtensionToMediaTypes = parseMimeTypes();


    private MediaTypeFactory() {
    }


    /**
     * Parse the {@code mime.types} file found in the resources. Format is:
     * <code>
     * # comments begin with a '#'<br>
     * # the format is &lt;mime type> &lt;space separated file extensions><br>
     * # for example:<br>
     * text/plain    txt text<br>
     * # this would map file.txt and file.text to<br>
     * # the mime type "text/plain"<br>
     * </code>
     * @return a multi-value map, mapping media types to file extensions.
     */
    private static MultiValueMap<String, MediaType> parseMimeTypes() {
        InputStream is = MediaTypeFactory.class.getResourceAsStream(MIME_TYPES_FILE_NAME);
        Assert.state(is != null, MIME_TYPES_FILE_NAME + " not found in classpath");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII))) {
            MultiValueMap<String, MediaType> result = new LinkedMultiValueMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                String[] tokens = StringUtils.tokenizeToStringArray(line, " \t\n\r\f");
                MediaType mediaType = MediaType.parseMediaType(tokens[0]);
                for (int i = 1; i < tokens.length; i++) {
                    String fileExtension = tokens[i].toLowerCase(Locale.ENGLISH);
                    result.add(fileExtension, mediaType);
                }
            }
            return result;
        }
        catch (IOException ex) {
            throw new IllegalStateException("Could not read " + MIME_TYPES_FILE_NAME, ex);
        }
    }

    /**
     * Determine a media type for the given resource, if possible.
     * @param resource the resource to introspect
     * @return the corresponding media type, or {@code null} if none found
     */
    public static Optional<MediaType> getMediaType(@Nullable Resource resource) {
        return Optional.ofNullable(resource)
                .map(Resource::getFilename)
                .flatMap(MediaTypeFactory::getMediaType);
    }

    /**
     * Determine a media type for the given file name, if possible.
     * @param filename the file name plus extension
     * @return the corresponding media type, or {@code null} if none found
     */
    public static Optional<MediaType> getMediaType(@Nullable String filename) {
        return getMediaTypes(filename).stream().findFirst();
    }

    /**
     * Determine the media types for the given file name, if possible.
     * @param filename the file name plus extension
     * @return the corresponding media types, or an empty list if none found
     */
    public static List<MediaType> getMediaTypes(@Nullable String filename) {
        List<MediaType> mediaTypes = null;
        String ext = StringUtils.getFilenameExtension(filename);
        if (ext != null) {
            mediaTypes = fileExtensionToMediaTypes.get(ext.toLowerCase(Locale.ENGLISH));
        }
        return (mediaTypes != null ? mediaTypes : Collections.emptyList());
    }

}