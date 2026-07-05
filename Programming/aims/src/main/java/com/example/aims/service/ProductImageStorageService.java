package com.example.aims.service;

import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif");
    private static final long MAX_BYTES = 5 * 1024 * 1024;

    @Value("${server.servlet.context-path:/}")
    String contextPath;

    Path productUploadDir;

    @PostConstruct
    void init() throws IOException {
        productUploadDir = Paths.get("uploads", "products").toAbsolutePath();
        Files.createDirectories(productUploadDir);
    }

    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST.getCode(), "Image file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new AppException(ErrorCode.INVALID_REQUEST.getCode(), "Image must be 5 MB or smaller");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST.getCode(),
                    "Only JPEG, PNG, WebP, and GIF images are allowed");
        }

        String extension = extensionForContentType(contentType);
        String filename = UUID.randomUUID() + extension;
        Path target = productUploadDir.resolve(filename);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new AppException(
                    ErrorCode.UNCATEGORIZED.getCode(),
                    "Failed to save image file");
        }

        String base = contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
        return base + "/uploads/products/" + filename;
    }

    private String extensionForContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> {
                String original = StringUtils.cleanPath(
                        contentType.replace("image/", "."));
                yield original.startsWith(".") ? original : ".jpg";
            }
        };
    }
}
