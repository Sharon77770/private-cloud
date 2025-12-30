package com.sharon77770.private_cloud.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.sharon77770.private_cloud.data_class.UserData;
import com.sharon77770.private_cloud.database.UserService;
import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudManager cloudManager;


    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
        List<UserData> allUsers = userService.findAll();

        int totalAllocated = 0;
        List<Map<String, Object>> userStatsList = allUsers.stream()
                .filter(u -> u.getUserRole() < 100) 
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    double used = cloudManager.getUserUsedSpaceGb(user.getUserId());
                    int quota = user.getCloudSize();
                    map.put("info", user);
                    map.put("used", Math.round(used * 100.0) / 100.0); 
                    map.put("percent", Math.min(100, (int)((used / quota) * 100)));
                    map.put("fileCount", cloudManager.getTotalFileCount(user.getUserId()));
                    return map;
                }).collect(Collectors.toList());

        totalAllocated = allUsers.stream()
                .filter(u -> u.getUserRole() < 100)
                .mapToInt(UserData::getCloudSize).sum();

        UserData systemAccount = allUsers.stream()
                .filter(u -> u.getUserRole() == 100).findFirst().orElse(null);

        int systemQuota = (systemAccount != null) ? systemAccount.getCloudSize() : 500;
        double systemUsed = cloudManager.getSystemStorageStats()[1]; 
        int systemPercent = (int) ((systemUsed / systemQuota) * 100);
        
        model.addAttribute("users", userStatsList);
        model.addAttribute("systemQuota", systemQuota);
        model.addAttribute("systemUsed", Math.round(systemUsed * 10.0) / 10.0);
        model.addAttribute("systemPercent", Math.min(100, systemPercent));
        
        model.addAttribute("totalAllocated", totalAllocated);
        model.addAttribute("allocationPercent", Math.min(100, (int)((double)totalAllocated / systemQuota * 100)));
        
        model.addAttribute("fileCount", cloudManager.getSystemTotalFileCount());
        model.addAttribute("userCount", userStatsList.size());
        model.addAttribute("adminNickname", principal != null ? principal.getName() : "관리자");

        return "admin";
    }


    @PostMapping("/createac")
    public String createac(HttpServletRequest request, RedirectAttributes rttr) {
        String id = request.getParameter("id");
        String pw = request.getParameter("pw");
        String name = request.getParameter("nm");
        int cSize = Integer.parseInt(request.getParameter("sz"));

        int systemQuota = userService.getSys().getCloudSize();
        int currentAllocated = userService.findAll().stream()
                .filter(u -> u.getUserRole() < 100).mapToInt(UserData::getCloudSize).sum();

        if (currentAllocated + cSize > systemQuota) {
            rttr.addFlashAttribute("error", "용량 초과: 시스템 할당 가능 용량이 부족합니다. (남은 용량: " + (systemQuota - currentAllocated) + "GB)");
            return "redirect:/admin";
        }

        UserData newUser = new UserData(id, name, passwordEncoder.encode(pw), cSize, 1);
        userService.save(newUser);
        return "redirect:/admin";
    }


    @PostMapping("/admin/update-user-quota")
    public String updateQuota(@RequestParam String userId, @RequestParam int sz, RedirectAttributes rttr) {
        int systemQuota = userService.getSys().getCloudSize();
        int otherAllocated = userService.findAll().stream()
                .filter(u -> u.getUserRole() < 100 && !u.getUserId().equals(userId))
                .mapToInt(UserData::getCloudSize).sum();

        if (otherAllocated + sz > systemQuota) {
            rttr.addFlashAttribute("error", "수정 실패: 시스템 총 용량을 초과하는 할당입니다.");
            return "redirect:/admin";
        }

        UserData userData = userService.findById(userId);
        if(userData != null) {
            userData.setCloudSize(sz);
            userService.save(userData);
        }

        return "redirect:/admin";
    }

    
    @PostMapping("/admin/update-system-quota")
    public String updateSystemQuota(@RequestParam int quota, RedirectAttributes rttr) {
        double hardwareMax = cloudManager.getSystemStorageStats()[0];
        if (quota > hardwareMax) {
            rttr.addFlashAttribute("error", "하드웨어 한계(" + (int)hardwareMax + "GB)를 초과할 수 없습니다.");
            return "redirect:/admin";
        }

        int currentTotalAllocated = userService.findAll().stream()
                .filter(u -> u.getUserRole() < 100).mapToInt(UserData::getCloudSize).sum();

        if (quota < currentTotalAllocated) {
            rttr.addFlashAttribute("error", "설정 불가: 이미 유저들에게 할당된 용량의 합계(" + currentTotalAllocated + "GB)보다 커야 합니다.");
            return "redirect:/admin";
        }

        UserData sys = userService.getSys(); 
        sys.setCloudSize(quota);
        userService.save(sys);

        return "redirect:/admin";
    }


    @GetMapping("/admin/delete-user")
    public String deleteUser(@RequestParam(name="id") String userId) {
        UserData target = userService.findById(userId);
    
        if (target != null && target.getUserRole() < 10 && !"admin".equals(userId)) {
            userService.remove(userId);
        }
    
        return "redirect:/admin";
    }
}