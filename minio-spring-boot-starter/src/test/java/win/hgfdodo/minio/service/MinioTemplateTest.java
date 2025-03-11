package win.hgfdodo.minio.service;

import io.minio.errors.*;
import io.minio.messages.Bucket;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MinioTemplateTest {
    private final static Logger log = LoggerFactory.getLogger(MinioTemplateTest.class);

    @Autowired
    MinioTemplate minioTemplate;
    private final static String BUCKET_NAME = "odmp";
    private final static String OBJECT_NAME = "x.jpg";

    @BeforeEach
    void setUp() {
    }

    @Test
    void createBucket() throws IOException, InvalidResponseException, RegionConflictException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        minioTemplate.createBucket(BUCKET_NAME);
        List<Bucket> bucketList = minioTemplate.getAllBuckets();
        log.info("bucket list :{}", bucketList);
        log.info("list containers {}? {}", BUCKET_NAME, bucketList.contains(BUCKET_NAME));
        assertTrue(bucketList.contains(BUCKET_NAME));
    }

    @Test
    void getBucket() throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException {
        Optional<Bucket> bucket = minioTemplate.getBucket(BUCKET_NAME);
        if (bucket.isPresent()) {
            Bucket b = bucket.get();
            log.info("bucket info: name={}, createTime={}", b.name(), b.creationDate());
        }
        assertTrue(bucket.isPresent());
    }

    @Test
    void removeBucket() {
    }

    @Test
    void getAllObjectsByPrefix() {
    }

    @Test
    void getObjectURL() {
    }

    @Test
    void saveKnownSizeObject() {
    }

    @Test
    void saveUnknownSizeObject() {
    }

    @Test
    void mkdir() {
    }

    @Test
    void saveObject() throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
        FileInputStream fileInputStream = new FileInputStream(new File("/Users/guangfuhe/Project/components/minio-spring-boot/minio-spring-boot-starter/src/test/resources/a.jpg"));
        minioTemplate.saveKnownSizeObject(BUCKET_NAME, OBJECT_NAME, fileInputStream, fileInputStream.available(), "image/jpeg");
    }

    @Test
    void saveObjectExtra() {
    }

    @Test
    void saveObjectExtraWithSSE() {
    }

    @Test
    void getObjectInfo() throws ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        System.out.println(minioTemplate.getObjectInfo(BUCKET_NAME, OBJECT_NAME));
    }

    @Test
    void getVersionedObjectInfo() {
    }

    @Test
    void removeObject() {
    }

    @Test
    void removeVersionedObject() {
    }

    @Test
    void removeObjects() {
    }

    @Test
    void getOjectAsFile() throws ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        InputStream inputStream = minioTemplate.getObject(BUCKET_NAME, OBJECT_NAME);
        File destFile = new File("/Users/guangfuhe/Downloads/xxx.jpg");
        FileUtils.copyInputStreamToFile(inputStream, destFile);
    }

    @Test
    void test() throws ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        InputStream inputStream = minioTemplate.getObject("odmp", "files/04062f05621cf352ce2b48eac0cbeba4f1_2.pdf");
        File destFile = new File("/Users/guangfuhe/Downloads/xxx.pdf");
        FileUtils.copyInputStreamToFile(inputStream, destFile);
    }
}