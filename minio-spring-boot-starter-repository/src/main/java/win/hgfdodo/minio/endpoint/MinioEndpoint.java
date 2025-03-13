package win.hgfdodo.minio.endpoint;


import io.minio.ObjectStat;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import win.hgfdodo.minio.exception.MinioBadRequestException;
import win.hgfdodo.minio.service.MinioTemplate;
import win.hgfdodo.minio.vo.MinioBucket;
import win.hgfdodo.minio.vo.MinioItem;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minio common controller
 *
 * @author Guangfu He
 */
@ConditionalOnWebApplication
@RestController
@RequestMapping("${spring.minio.endpoint.name:/minio}")
public class MinioEndpoint {

    private final static Logger log = LoggerFactory.getLogger(MinioEndpoint.class);

    public final static int MAX_SLICE_RESPONSE_BUFFER = 1024 * 1024 * 5;
    public final static int MAX_SLICE_DATA = Integer.MAX_VALUE;

    private final MinioTemplate template;

    public MinioEndpoint(MinioTemplate template) {
        this.template = template;
    }

    /**
     * Bucket Endpoints
     */
    @PostMapping("/bucket/{bucketName}")
    public MinioBucket createBucker(@PathVariable String bucketName) throws IOException, InvalidResponseException, RegionConflictException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {

        template.createBucket(bucketName);
        return new MinioBucket(template.getBucket(bucketName).get());
    }

    @GetMapping("/buckets")
    public List<MinioBucket> getBuckets() throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        log.debug("get all buckets");
        return template.getAllBuckets().stream().map(MinioBucket::new).toList();
    }

    @GetMapping("/bucket/{bucketName}")
    public MinioBucket getBucket(@PathVariable String bucketName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        return new MinioBucket(template.getBucket(bucketName).orElseThrow(() -> new IllegalArgumentException("Bucket Name not found!")));
    }

    @DeleteMapping("/bucket/{bucketName}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteBucket(@PathVariable String bucketName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {

        template.removeBucket(bucketName);
    }

    /**
     * Object Endpoints
     *
     * @return
     */
    @PostMapping("/object/{bucketName}")
    public ObjectStat createObject(@RequestBody MultipartFile object, @PathVariable String bucketName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
        String name = object.getOriginalFilename();
        template.saveKnownSizeObject(bucketName, name, object.getInputStream(), object.getSize(), object.getContentType());
        return template.getObjectInfo(bucketName, name);
    }

    @PostMapping("/object/{bucketName}/{objectName}")
    public ObjectStat createObject(@RequestBody MultipartFile object, @PathVariable String bucketName, @PathVariable String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
        template.saveKnownSizeObject(bucketName, objectName, object.getInputStream(), object.getSize(), object.getContentType());
        return template.getObjectInfo(bucketName, objectName);
    }

    @GetMapping("/object/{bucketName}/{objectName}")
    public void getObject(@PathVariable String bucketName, @PathVariable String objectName, @RequestHeader HttpHeaders headers, HttpServletResponse response) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException, MinioBadRequestException {
        log.debug("get object: {}/{}", bucketName, objectName);
        List<Item> items = template.getAllObjectsByPrefix(bucketName, objectName, true);
        ObjectStat stat = template.getObjectInfo(bucketName, objectName);
        if (stat == null) {
            throw new MinioBadRequestException(bucketName + "/" + objectName + ": not exists");
        }

        long contentLength = stat.length();
        long start = 0;
        long rangeLength = 0;

        if (headers.getRange().isEmpty()) {
            rangeLength = Math.min(MAX_SLICE_DATA, contentLength);
        } else {
            HttpRange range = headers.getRange().get(0);
            start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            rangeLength = Math.min(MAX_SLICE_DATA, end - start + 1);
        }
        long end = Math.min(start + rangeLength - 1, contentLength - 1);
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Content-Range", "bytes " + start + '-' + end + '/' + contentLength);
        response.addHeader("Content-Length", String.valueOf(end - start + 1));
        response.addHeader("Content-Type", MediaTypeFactory.getMediaType(stat.name()).orElse(MediaType.APPLICATION_OCTET_STREAM).getType());

        InputStream in = template.getObject(bucketName, objectName);
        response.setBufferSize(MAX_SLICE_RESPONSE_BUFFER);
        try {
            StreamUtils.copyRange(in, response.getOutputStream(), start, end);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    @GetMapping("/object/filter/{bucketName}/{objectName}")
    public List<MinioItem> filterObject(@PathVariable String bucketName, @PathVariable String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        List<Item> items = template.getAllObjectsByPrefix(bucketName, objectName, true);
        return items.stream().map(MinioItem::new).collect(Collectors.toList());
    }

    @GetMapping("/object/share/{bucketName}/{objectName}/{expires}")
    public Map<String, Object> getObject(@PathVariable String bucketName, @PathVariable String objectName, @PathVariable Integer expires) throws IOException, InvalidResponseException, InvalidKeyException, InvalidExpiresRangeException, ServerException, ErrorResponseException, NoSuchAlgorithmException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        Map<String, Object> responseBody = new HashMap<>();
        // Put Object info
        responseBody.put("bucket", bucketName);
        responseBody.put("object", objectName);
        responseBody.put("url", template.getObjectURL(bucketName, objectName, expires));
        responseBody.put("expires", expires);
        return responseBody;
    }

    @DeleteMapping("/object/{bucketName}/{objectName}/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteObject(@PathVariable String bucketName, @PathVariable String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        template.removeObject(bucketName, objectName);
    }

}
