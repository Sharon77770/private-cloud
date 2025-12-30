package com.sharon77770.private_cloud.cloud_manager;

import com.sharon77770.private_cloud.data_class.FileData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CloudManager {

    private final Path rootLocation = Paths.get("family_cloud_storage_________");


    public Path getUserRootPath(String userId) {
        return rootLocation.resolve(userId);
    }


    public void setupUserDirectory(String userId) throws IOException {
        Path userPath = getUserRootPath(userId);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath);
        }
    }


    public void createFolder(String userId, String currentPath, String folderName) throws IOException {
        Path targetPath = getUserRootPath(userId)
                .resolve(currentPath != null ? currentPath : "")
                .resolve(folderName);
        if (!Files.exists(targetPath)) Files.createDirectories(targetPath);
    }


    public String storeFile(String userId, String currentPath, MultipartFile file) throws IOException {
        Path uploadPath = getUserRootPath(userId).resolve(currentPath != null ? currentPath : "");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileName = file.getOriginalFilename();
        if (fileName == null) fileName = "unnamed_" + System.currentTimeMillis();

        Path destination = uploadPath.resolve(fileName);
        if (Files.exists(destination)) {
            fileName = System.currentTimeMillis() + "_" + fileName;
            destination = uploadPath.resolve(fileName);
        }

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }


    public List<FileData> getFileList(String userId, String currentPath) throws IOException {
        Path targetDir = getUserRootPath(userId).resolve(currentPath != null ? currentPath : "");
        if (!Files.exists(targetDir)) return List.of();

        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream.map(path -> {
                String name = path.getFileName().toString();
                boolean isFolder = Files.isDirectory(path);
                long size = 0;
                try { size = isFolder ? 0 : Files.size(path); } catch (IOException ignored) {}
                return new FileData(name, isFolder ? "folder" : determineFileType(name), size, isFolder);
            }).collect(Collectors.toList());
        }
    }


    public double[] getSystemStorageStats() {
        java.io.File root = rootLocation.toFile();
        if (!root.exists()) root.mkdirs();
        long total = root.getTotalSpace();
        long free = root.getUsableSpace();
        long used = total - free;
        return new double[]{
            total / 1073741824.0, used / 1073741824.0, free / 1073741824.0, (double)used/total * 100
        };
    }


    public double getUserUsedSpaceGb(String userId) {
        Path userPath = getUserRootPath(userId);
        if (!Files.exists(userPath)) return 0.0;
        try (Stream<Path> walk = Files.walk(userPath)) {
            long totalBytes = walk.filter(Files::isRegularFile).mapToLong(p -> {
                try { return Files.size(p); } catch (IOException e) { return 0L; }
            }).sum();
            return totalBytes / 1073741824.0;
        } catch (IOException e) { return 0.0; }
    }

    
    private String determineFileType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg")) return "image";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "video";
        return "file";
    }


    public long getSystemTotalFileCount() {
        if (!Files.exists(rootLocation)) return 0L;

        try (Stream<Path> walk = Files.walk(rootLocation)) {
            return walk
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            return 0L;
        }
    }


    public long getTotalFileCount(String userId) {
        Path userPath = getUserRootPath(userId);
        
        if (!Files.exists(userPath)) return 0L;
        
        try (Stream<Path> walk = Files.walk(userPath)) {
            return walk.filter(Files::isRegularFile).count();
        } 
        catch (IOException e) { return 0L; }
    }
}