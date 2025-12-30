package com.sharon77770.private_cloud.controller;

import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class FileViewerController {

    private final CloudManager cloudManager;


    @GetMapping("/viewer")
    public String openViewer(@RequestParam String userId, @RequestParam String fileName, Model model) {
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        model.addAttribute("userId", userId);
        model.addAttribute("fileName", fileName);
        model.addAttribute("ext", ext);
        
        String type = "etc";
        if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "webp".equals(ext)) type = "image";
        else if ("mp4".equals(ext) || "webm".equals(ext)) type = "video";
        else if ("pdf".equals(ext)) type = "pdf";
        else if ("txt".equals(ext)) type = "text";
        
        model.addAttribute("type", type);
        return "viewer";
    }


    @GetMapping("/stream/file")
    public ResponseEntity<Resource> streamFile(@RequestParam String userId, @RequestParam String fileName) throws MalformedURLException {
        Path path = Paths.get(cloudManager.getUserRootPath(userId).toString()).resolve(fileName);
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(getContentType(fileName)))
                .body(resource);
    }

    
    private String getContentType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        
        return "application/octet-stream";
    }
}