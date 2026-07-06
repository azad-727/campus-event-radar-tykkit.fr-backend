package com.tykkit.fr.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/s3")
@CrossOrigin(origins = "*")
public class S3Controller {

    @GetMapping("/presigned-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPresignedUrl(@RequestParam String fileName, @RequestParam String fileType) {
        // MOCK IMPLEMENTATION of S3 Presigned URL Generation
        // In production, inject software.amazon.awssdk.services.s3.presigner.S3Presigner
        String objectKey = "events/" + UUID.randomUUID() + "-" + fileName;
        
        // The URL the frontend will PUT the file to
        String uploadUrl = "https://tykkit-assets.s3.ap-south-1.amazonaws.com/" + objectKey + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=mock-signature";
        
        // The public URL to save in the database
        String publicUrl = "https://tykkit-assets.s3.ap-south-1.amazonaws.com/" + objectKey;

        return ResponseEntity.ok(Map.of(
            "uploadUrl", uploadUrl,
            "objectKey", objectKey,
            "publicUrl", publicUrl
        ));
    }
}
