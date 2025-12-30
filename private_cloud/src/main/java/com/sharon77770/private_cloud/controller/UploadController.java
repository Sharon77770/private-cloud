package com.sharon77770.private_cloud.controller;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import com.sharon77770.private_cloud.database.UserService;
import com.sharon77770.private_cloud.data_class.UserData;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final CloudManager cloudManager;
    private final UserService userService;

    @GetMapping("/upload")
    public String uploadPage(@RequestParam(required = false) String path,
                             @RequestParam(defaultValue = "all") String menu, Model model) {
        model.addAttribute("currentPath", path);
        model.addAttribute("currentMenu", menu);
        return "upload";
    }

    @PostMapping("/api/upload")
    public String upload(@RequestParam("files") MultipartFile[] files, 
                         @RequestParam(required = false) String path,
                         @RequestParam(required = false) String menu,
                         Authentication authentication) {

        if (authentication == null) return "redirect:/login";
        String targetId = "shared".equals(menu) ? (userService.getFamily().getUserId()) : authentication.getName();
        
        UserData user = userService.findById(targetId);
        double quotaGb = user.getCloudSize();
        double currentUsedGb = cloudManager.getUserUsedSpaceGb(targetId);
        
        long totalUploadBytes = 0;
        for (MultipartFile f : files) totalUploadBytes += f.getSize();
        double totalUploadGb = totalUploadBytes / 1073741824.0;

        if (currentUsedGb + totalUploadGb > quotaGb) {
            return "redirect:/home?error=quotaExceeded&menu=" + menu;
        }

        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) cloudManager.storeFile(targetId, path, file);
            }
        } 
        catch (IOException e) {
            return "redirect:/upload?error=fail&path=" + (path != null ? path : "");
        }
        return "redirect:/home?menu=" + (menu != null ? menu : "all") + "&path=" + (path != null ? path : "");
    }


    @PostMapping("/api/create-folder")
    public String createFolder(@RequestParam String folderName, @RequestParam(required = false) String path, 
                               @RequestParam(required = false) String menu, Authentication authentication) { 
        
        if (authentication == null) return "redirect:/login";
        
        String targetId = "shared".equals(menu) ? (userService.getFamily().getUserId()) : authentication.getName();
        
        try { cloudManager.createFolder(targetId, path, folderName); } catch (IOException e) {}
        
        return "redirect:/home?menu=" + (menu != null ? menu : "all") + "&path=" + (path != null ? path : "");
    }
}