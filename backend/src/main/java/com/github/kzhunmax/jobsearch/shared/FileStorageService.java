package com.github.kzhunmax.jobsearch.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
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
}
