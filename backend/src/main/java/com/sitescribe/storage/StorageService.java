package com.sitescribe.storage;

import java.io.IOException;

public interface StorageService {
    /** Store bytes and return an opaque reference (path or URL) for later retrieval. */
    String store(String filename, String contentType, byte[] bytes) throws IOException;
    byte[] retrieve(String reference) throws IOException;
    void delete(String reference);
}
