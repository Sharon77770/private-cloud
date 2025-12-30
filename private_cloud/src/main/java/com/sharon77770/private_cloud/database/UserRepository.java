package com.sharon77770.private_cloud.database;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sharon77770.private_cloud.data_class.UserData;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;
    
    
    public void insert(UserData user) {
        em.persist(user);
    }


    public void update(UserData user) {
        em.merge(user);
    }

    
    public void remove(String userId) {
        em.remove(findById(userId));
    }

    
    public UserData findById(String userId) {
        return em.find(UserData.class, userId);
    }

    
    public Boolean contains(String userId) {
        return !em.createQuery("SELECT u FROM UserData u WHERE u.userId = :targetName", UserData.class)
                .setParameter("targetName", userId)
                .getResultList().isEmpty();
    }

    
    public List<UserData> findAll() {
        return em.createQuery("SELECT u FROM UserData u", UserData.class)
                .getResultList();
    }

    
    public UserData getSys() {
        return em.createQuery("SELECT u FROM UserData u WHERE u.userRole = :role", UserData.class)
                .setParameter("role", 100)
                .getResultList()
                .getFirst();
    }

    
    public UserData getFamily() {
        return em.createQuery("SELECT u FROM UserData u WHERE u.userRole = :role", UserData.class)
                .setParameter("role", 10)
                .getResultList()
                .getFirst();
    }
}
