package com.ptit.booking.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CloudinaryService {
    Map<?, ?> uploadFile(MultipartFile file, String folderName);
    List<String> uploadImages(List<MultipartFile> files, String folderName) throws IOException;
    CompletableFuture<String> uploadImage(MultipartFile imageFile);
    CompletableFuture<List<String>> uploadImageList(List<MultipartFile> imageFileList);
}
