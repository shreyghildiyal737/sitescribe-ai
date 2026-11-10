package com.sitescribe.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "sitescribe.storage.mode", havingValue = "supabase")
public class SupabaseStorageService implements StorageService {

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final HttpClient http;

    public SupabaseStorageService(
        @Value("${sitescribe.storage.supabase-url}") String supabaseUrl,
        @Value("${sitescribe.storage.supabase-service-key}") String serviceKey,
        @Value("${sitescribe.storage.supabase-bucket:audio-uploads}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl.replaceAll("/$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.http = HttpClient.newHttpClient();
    }

    @Override
    public String store(String filename, String contentType, byte[] bytes) throws IOException {
        String key = UUID.randomUUID() + "_" + filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + key;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uploadUrl))
            .header("Authorization", "Bearer " + serviceKey)
            .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
            .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
            .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 201)
                throw new IOException("Supabase upload failed (HTTP " + response.statusCode() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + key;
    }

    @Override
    public byte[] retrieve(String reference) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(reference))
            .header("Authorization", "Bearer " + serviceKey)
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200)
                throw new IOException("Supabase download failed (HTTP " + response.statusCode() + ")");
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }

    @Override
    public void delete(String reference) throws IOException {
        // Extract the key from the public URL
        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!reference.startsWith(prefix))
            throw new IOException("Not a reference to this bucket, so nothing was deleted");
        String key = reference.substring(prefix.length());

        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + key;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(deleteUrl))
            .header("Authorization", "Bearer " + serviceKey)
            .DELETE()
            .build();

        try {
            var response = http.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() / 100 != 2)
                throw new IOException("Supabase delete failed (HTTP " + response.statusCode() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Delete interrupted", e);
        }
    }
}
