package com.example.service;

import com.example.entity.ProfileEntity;
import com.example.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private ProfileRepository userRepository;


    public boolean isExists(Long id) {
        Optional<ProfileEntity> optional = userRepository.getByUserId(id);
        ProfileEntity entity = optional.get();

        Long userId= entity.getUserId();
        System.out.println(userId);

            if (userId.equals(id)) {
                return true;
        }
        return false;
    }

    public void addUser(ProfileEntity profile) {
        userRepository.save(profile);
    }





}
