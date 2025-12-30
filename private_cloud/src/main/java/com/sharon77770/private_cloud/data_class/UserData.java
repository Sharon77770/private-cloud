package com.sharon77770.private_cloud.data_class;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern; 
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class UserData {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "사용자 이름은 영문과 숫자만 가능합니다.")
    private String userName;

    @Column(name = "user_password", length = 100) 
    private String userPassword; 

    private int cloudSize;
    private int userRole;
    
    public UserData(String userId, String userName, String userPassword, int cloudSize, int userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword; 
        this.cloudSize = cloudSize;
        this.userRole = userRole;
    }
}