package com.sitescribe.storage;

import java.io.IOException;

public interface StorageService {
    /** Store bytes and return an opaque reference (path or URL) for later retrieval. */
    String store(String filename, String contentType, byte[] bytes) throws IOException;
    byte[] retrieve(String reference) throws IOException;
    /** Throws when the object may still exist, so the caller can keep the reference instead of orphaning it. */
    void delete(String reference) throws IOException;
}
