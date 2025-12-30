package com.sharon77770.private_cloud.cloud_manager;

import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileService {

    private final CloudManager cloudManager;

    public boolean deleteFile(String targetId, String path, String fileName) {
        Path root = cloudManager.getUserRootPath(targetId);
        Path targetPath = root.resolve(path != null ? path : "").resolve(fileName);
        File file = targetPath.toFile();

        if (file.exists()) {
            return file.delete(); 
        }
        return false;
    }
}