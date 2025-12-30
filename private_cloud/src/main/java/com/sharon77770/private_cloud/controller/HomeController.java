package com.sharon77770.private_cloud.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import com.sharon77770.private_cloud.cloud_manager.FileService;
import com.sharon77770.private_cloud.database.UserService;
import com.sharon77770.private_cloud.data_class.UserData;
import com.sharon77770.private_cloud.data_class.FileData;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final CloudManager cloudManager;
    private final UserService userService;
    private final FileService fileService;


    @GetMapping("/home")
    public String home(Authentication authentication, Model model, 
                       @RequestParam(required = false) String path,
                       @RequestParam(defaultValue = "all") String menu) {
        
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        String userId = authentication.getName();
        String targetId = "shared".equals(menu) ? (userService.getFamily().getUserId()) : userId;
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            List<FileData> allFiles = cloudManager.getFileList(targetId, path);
            
            List<FileData> filteredFiles = allFiles.stream()
                .filter(file -> {
                    if (file.isFolder()) return true; 
                    String name = file.getFileName().toLowerCase();
                    if ("photo".equals(menu)) {
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".webp");
                    } else if ("video".equals(menu)) {
                        return name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi") || name.endsWith(".mov");
                    }
                    return true; 
                })
                .collect(Collectors.toList());

            UserData user = userService.findById(targetId);
            double usedGb = cloudManager.getUserUsedSpaceGb(targetId);
            
            int percentUsed = 0;
            if (user != null && user.getCloudSize() > 0) {
                percentUsed = (int)((usedGb / user.getCloudSize()) * 100);
            }

            model.addAttribute("files", filteredFiles); 
            model.addAttribute("currentPath", path != null ? path : "");
            model.addAttribute("currentMenu", menu);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("userNickname", userId); 
            model.addAttribute("viewerTargetId", targetId);
            model.addAttribute("percentUsed", Math.min(percentUsed, 100)); 

            String parentPath = "";
            if (path != null && path.contains("/")) {
                parentPath = path.substring(0, path.lastIndexOf("/"));
            }
            model.addAttribute("parentPath", parentPath);

        } 
        catch (IOException e) {
            model.addAttribute("error", "파일 목록 오류");
        }
        
        return "index"; 
    }


    @PostMapping("/api/delete")
    public String deleteFile(@RequestParam String fileName,
                            @RequestParam(required = false) String path,
                            @RequestParam String menu,
                            Authentication authentication) {
        
        if (authentication == null) return "redirect:/login";

        String userId = authentication.getName();
        String targetId = "shared".equals(menu) ? (userService.getFamily().getUserId()) : userId;

        fileService.deleteFile(targetId, path, fileName);

        return "redirect:/home?path=" + (path != null ? path : "") + "&menu=" + menu;
    }
}