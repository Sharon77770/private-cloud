package com.sharon77770.private_cloud.rest_contoller;

import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import com.sharon77770.private_cloud.database.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class FileDisplayController {

    private final CloudManager cloudManager;
    private final UserService userService;


    @GetMapping("/api/display")
    public ResponseEntity<Resource> displayFile(@RequestParam String fileName,
                                                @RequestParam(required = false) String path,
                                                @RequestParam(value = "menu", required = false) String menu,
                                                Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(403).build();

        String targetId = "shared".equals(menu) ? (userService.getFamily().getUserId()) : authentication.getName();
        
        Path filePath = cloudManager.getUserRootPath(targetId)
                                    .resolve(path != null ? path : "")
                                    .resolve(fileName);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                String contentType = "application/octet-stream";
                String name = fileName.toLowerCase();

                if (name.endsWith(".png")) contentType = "image/png";
                else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (name.endsWith(".gif")) contentType = "image/gif";
                else if (name.endsWith(".webp")) contentType = "image/webp";

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            }
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return ResponseEntity.notFound().build();
    }
}