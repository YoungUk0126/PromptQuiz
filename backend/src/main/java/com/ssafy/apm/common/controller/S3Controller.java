package com.ssafy.apm.common.controller;

import com.ssafy.apm.common.domain.ResponseData;
import com.ssafy.apm.common.dto.S3FileResponseDto;
import com.ssafy.apm.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/api/v1/file/upload")
    public ResponseEntity<ResponseData<?>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        S3FileResponseDto responsedto = s3Service.uploadFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(responsedto));
    }

    @PostMapping("/api/v1/image/upload")
    public ResponseEntity<ResponseData<?>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        S3FileResponseDto responsedto = s3Service.uploadImage(file);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(responsedto));
    }

    @PostMapping("/api/v1/video/upload")
    public ResponseEntity<ResponseData<?>> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        S3FileResponseDto responsedto = s3Service.uploadVideo(file);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(responsedto));
    }

//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("bucketName") String bucketName) {
//        try {
//            s3Service.uploadFile(bucketName, file.getOriginalFilename(), file.getInputStream(), file.getSize());
//            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
//        }
//    }
//
//    @GetMapping("/download")
//    public ResponseEntity<Resource> downloadFile(@RequestParam("bucketName") String bucketName, @RequestParam("fileName") String fileName, HttpServletResponse response) {
//        try {
//            response.setContentType("application/octet-stream");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//            s3Service.downloadFile(bucketName, fileName, response.getOutputStream());
//            return ResponseEntity.ok()
//                    .body(new InputStreamResource(response.getOutputStream()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
}