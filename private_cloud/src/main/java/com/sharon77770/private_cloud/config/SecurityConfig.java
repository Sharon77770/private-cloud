package com.sharon77770.private_cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sharon77770.private_cloud.handler.LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll() 
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                .anyRequest().authenticated() 
            )
            
            .formLogin(form -> form
                .loginPage("/login")               
                .loginProcessingUrl("/login")    
                .usernameParameter("id")          
                .passwordParameter("pw")          
                .successHandler(loginSuccessHandler) 
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)        
                .deleteCookies("JSESSIONID", "jwt")
                .logoutSuccessUrl("/login")         
            );
            
        return http.build();
    }
}