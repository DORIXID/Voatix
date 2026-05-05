package dev.Voatix.service.minio;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    public void putObject(String bucket, String id, InputStream stream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(id) // Теперь это строковое представление Long id
                            .stream(stream, stream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "MinIO upload error: " + e.getMessage());
        }
    }

    public InputStream getObject(String bucket, String id) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO read error for ID: " + id, e);
        }
    }

    public void removeObject(String bucket, String id) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error removing object from Minio, ID: " + id, e);
        }
    }
}