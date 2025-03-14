package win.hgfdodo.minio.service.http;

import org.springframework.util.InvalidMimeTypeException;

public class InvalidMediaTypeException extends IllegalArgumentException {

    private final String mediaType;


    /**
     * Create a new InvalidMediaTypeException for the given media type.
     * @param mediaType the offending media type
     * @param message a detail message indicating the invalid part
     */
    public InvalidMediaTypeException(String mediaType, String message) {
        super("Invalid media type \"" + mediaType + "\": " + message);
        this.mediaType = mediaType;
    }

    /**
     * Constructor that allows wrapping {@link InvalidMimeTypeException}.
     */
    InvalidMediaTypeException(InvalidMimeTypeException ex) {
        super(ex.getMessage(), ex);
        this.mediaType = ex.getMimeType();
    }


    /**
     * Return the offending media type.
     */
    public String getMediaType() {
        return this.mediaType;
    }

}
