package com.example.music.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
public class LocalFileController {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileController.class);
    @Value("${local.file.storage-path}")
    private String storagePath;

    // 获取歌曲
    @GetMapping("/user01/{fileName:.+}")
    public ResponseEntity<byte[]> getMusic(@PathVariable String fileName) {
        return getFileResponse(fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    // 获取歌手图片
    @GetMapping("/user01/singer/img/{fileName:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        return getFileResponse("singer/img/" + fileName, MediaType.IMAGE_JPEG);
    }

    // 获取歌单图片
    @GetMapping("/user01/songlist/{fileName:.+}")
    public ResponseEntity<byte[]> getImage1(@PathVariable String fileName) {
        return getFileResponse("songlist/" + fileName, MediaType.IMAGE_JPEG);
    }

    // 获取歌曲图片
    @GetMapping("/user01/singer/song/{fileName:.+}")
    public ResponseEntity<byte[]> getImage2(@PathVariable String fileName) {
        return getFileResponse("singer/song/" + fileName, MediaType.IMAGE_JPEG);
    }

    // 获取头像
    @GetMapping("/img/avatorImages/{fileName:.+}")
    public ResponseEntity<byte[]> getImage3(@PathVariable String fileName) {
        return getFileResponse("img/avatorImages/" + fileName, MediaType.IMAGE_JPEG);
    }

    private ResponseEntity<byte[]> getFileResponse(String relativePath, MediaType defaultMediaType) {

        try {
            Path filePath = Paths.get(storagePath, relativePath);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(determineMediaType(filePath, defaultMediaType));

            // 只有音乐文件才设置为附件下载
            if (defaultMediaType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
                headers.setContentDispositionFormData("attachment", filePath.getFileName().toString());
            }

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("文件读取失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType determineMediaType(Path filePath, MediaType defaultType) {
        // 可以根据实际需要扩展更多文件类型判断
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (fileName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }

        return defaultType;
    }
}
