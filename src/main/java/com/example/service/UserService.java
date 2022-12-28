package com.example.service;

import com.example.entity.AdminEntity;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    public boolean isExists(Long id) {
        Optional<AdminEntity> optional = userRepository.getByUserId(id);
        AdminEntity entity = optional.get();

        Long userId= entity.getUserId();
        System.out.println(userId);

            if (userId.equals(id)) {
                return true;
        }
        return false;
    }

    public void addUser(AdminEntity profile) {
        userRepository.save(profile);
    }





}
