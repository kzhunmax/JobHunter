package com.github.kzhunmax.jobsearch.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket:resumes}")
    private String supabaseBucket;

    public String uploadFileToSupabase(MultipartFile file, Long userId, String requestId) {
        try {
            String uuid = UUID.randomUUID().toString();
            String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileName = uuid + "_" + StringUtils.getFilename(originalName);
            String path = "candidates/" + userId + "/" + fileName;

            log.debug("Request [{}]: Uploading to Supabase S3 at path={}", requestId, path);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(supabaseBucket)
                    .key(path)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + path;

            log.info("Request [{}]: CV uploaded successfully to Supabase S3 - url={}", requestId, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Request [{}]: Failed to upload to Supabase S3 for userId={}", requestId, userId, e);
            throw new RuntimeException("Upload to Supabase S3 failed: " + e.getMessage());
        }
    }

    public void deleteFileFromSupabase(String fileUrl, String requestId) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.debug("Request [{}]: No file URL provided, skipping deletion.", requestId);
            return;
        }

        try {
            String baseUrlPrefix = supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/";
            if (!fileUrl.startsWith(baseUrlPrefix)) {
                log.warn("Request [{}]: File URL {} does not match the expected Supabase URL structure. Skipping deletion.", requestId, fileUrl);
                return;
            }
            String objectKey = fileUrl.substring(baseUrlPrefix.length());
            log.debug("Request [{}]: Deleting file from Supabase S3 with key={}", requestId, objectKey);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(supabaseBucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("Request [{}]: File deleted successfully from Supabase S3 - key={}", requestId, objectKey);
        } catch (Exception e) {
        log.warn("Request [{}]: Failed to delete file {} from Supabase S3. It might need manual cleanup.", requestId, fileUrl, e);
        }
    }
}
