package com.sharon77770.private_cloud.database;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sharon77770.private_cloud.data_class.UserData;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserData user = userRepo.findById(userId); 

        String role = (user.getUserRole() == 0) ? "ROLE_ADMIN" : "ROLE_USER";

        if (user.getUserRole() >= 100) {
            throw new UsernameNotFoundException("Access Denied: System Account");
        }
        
        return User.builder()
                .username(user.getUserId())
                .password(user.getUserPassword()) 
                .authorities(role)      
                .build();
    }
    
    public void save(UserData user) {
        if (contains(user.getUserId())) {
            userRepo.update(user);
        }
        else {
            userRepo.insert(user);
        }
    }

    public void remove(String userId) {
        userRepo.remove(userId);
    }

    public UserData findById(String userId) {	    	
        return userRepo.findById(userId);
    }
    
    public Boolean contains(String userId) {
        return userRepo.contains(userId);
    }

    public List<UserData> findAll() {
        return userRepo.findAll();
    }

    public UserData getSys() {
        return userRepo.getSys();
    }

    public UserData getFamily() {
        return userRepo.getFamily();
    }
}
