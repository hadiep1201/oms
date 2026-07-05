package com.example.aims.controller;

import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.UploadImageResponse;
import com.example.aims.service.ProductImageStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileUploadController {

    ProductImageStorageService productImageStorageService;

    @PostMapping("/upload/image")
    public ApiResponse<UploadImageResponse> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = productImageStorageService.storeProductImage(file);
        return ApiResponse.<UploadImageResponse>builder()
                .result(UploadImageResponse.builder().url(url).build())
                .build();
    }
}
