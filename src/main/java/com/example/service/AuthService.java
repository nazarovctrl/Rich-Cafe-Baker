package com.example.service;

import com.example.entity.ProfileEntity;
import com.example.repository.ProfileRepository;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final ProfileRepository repository;

    public AuthService(ProfileRepository authRepository) {
        this.repository = authRepository;
    }

    public boolean isExists(Long userId){
        return repository.existsByUserId(userId);
    }

    public void createProfile(ProfileEntity profileEntity) {
        repository.save(profileEntity);
    }

    public boolean isExists(String phone){
        return repository.existsByPhone(phone);
    }

    public ProfileEntity findByUserId(Long userId){
        return repository.findByUserId(userId).get();
    }
}
