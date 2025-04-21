package com.ptit.booking.service.Impl;

import com.cloudinary.Cloudinary;
import com.ptit.booking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl  implements CloudinaryService {
    private final Cloudinary cloudinary;
    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try{
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            Map<?,?> uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadedFile.get("url").toString();

        }catch (java.io.IOException e){
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folderName) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        HashMap<Object, Object> options = new HashMap<>();
        options.put("folder", folderName);

        for (MultipartFile file : files) {
            Map<?, ?> uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            imageUrls.add(uploadedFile.get("url").toString());
        }

        return imageUrls;
    }


    @Override
    public CompletableFuture<String> uploadImage(MultipartFile imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HashMap<Object, Object> options = new HashMap<>();
                options.put("folder", "rooms");
                Map<?, ?> uploadedFile = cloudinary.uploader().upload(imageFile.getBytes(), options);
                return uploadedFile.get("url").toString();
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> uploadImageList(List<MultipartFile> imageFileList) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile imageFile : imageFileList) {
                try {
                    HashMap<Object, Object> options = new HashMap<>();
                    options.put("folder", "rooms");
                    Map<?, ?> uploadedFile = cloudinary.uploader().upload(imageFile.getBytes(), options);
                    String imageUrl = uploadedFile.get("url").toString();
                    System.out.println("imageUrl: " + imageUrl);
                    imageUrls.add(imageUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
                }
            }
            return imageUrls;
        });
    }

}
