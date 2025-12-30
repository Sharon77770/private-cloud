package com.sharon77770.private_cloud;

import java.util.Random;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sharon77770.private_cloud.cloud_manager.CloudManager;
import com.sharon77770.private_cloud.data_class.UserData;
import com.sharon77770.private_cloud.database.UserService;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class PrivateCloudApplication implements CommandLineRunner {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudManager cloudManager; 

    public static void main(String[] args) {
        SpringApplication.run(PrivateCloudApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        initializeAdmin();
    }

    private void initializeAdmin() {
        if (!userService.findAll().isEmpty()) {
            System.out.println("기존 데이터가 존재하여 초기화 단계를 스킵합니다");
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("=== 시스템 초기화 및 관리자 설정 ===");
        System.out.print("관리자 아이디 > ");
        String id = sc.nextLine();
        System.out.print("관리자 비밀번호 > ");
        String pw = sc.nextLine();

        try {
            cloudManager.setupUserDirectory(id);

            String encodedPassword = passwordEncoder.encode(pw);
            UserData admin = new UserData(id, "admin", encodedPassword, (int)cloudManager.getSystemStorageStats()[1] / 2 / 10, 0);
            userService.save(admin);


            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";  
            StringBuilder famId = new StringBuilder();        
            StringBuilder famPw = new StringBuilder();        
            Random random = new Random();         
            
            for (int i = 0; i < 10; i++) {            
                int index = random.nextInt(characters.length());            
                famId.append(characters.charAt(index));        
            }

            for (int i = 0; i < 100; i++) {            
                int index = random.nextInt(characters.length());            
                famPw.append(characters.charAt(index));        
            }
            
            UserData family = new UserData(famId.toString(), "family", famPw.toString(), (int)cloudManager.getSystemStorageStats()[1] / 2, 10);
            userService.save(family);

            cloudManager.setupUserDirectory(famId.toString());


            StringBuilder sysId = new StringBuilder();        
            StringBuilder sysPw = new StringBuilder();  
            for (int i = 0; i < 100; i++) {            
                int index = random.nextInt(characters.length());            
                sysId.append(characters.charAt(index));        
            }

            for (int i = 0; i < 100; i++) {            
                int index = random.nextInt(characters.length());            
                sysPw.append(characters.charAt(index));        
            }
            
            UserData sys = new UserData(sysId.toString(), "sys", sysPw.toString(), 10, 100);
            userService.save(sys);

            cloudManager.setupUserDirectory(sysId.toString());
            
            System.out.println("시스템 초기화 완료");
        } 
        catch (Exception e) {
            System.err.println("초기화 실패: " + e.getMessage());
        }
    }
}