package win.hgfdodo.minio.vo;

import io.minio.messages.Bucket;

public class MinioBucket {
    private final String name;

    public MinioBucket(Bucket bucket) {
        this.name = bucket.name();
    }

    public String getName() {
        return name;
    }
}
