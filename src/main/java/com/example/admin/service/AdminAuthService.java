package com.example.admin.service;


import com.example.admin.repository.AdminAuthRepository;
import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminAuthRepository authRepository;

    public AdminAuthService(AdminAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public boolean isExists(String text){
return authRepository.existsByPhone(text);
    }

    public AdminEntity findByPhone(String phone){
       return authRepository.findByPhone(phone).get();
    }

    public void saveUserId(AdminEntity adminEntity){
        authRepository.save(adminEntity);
    }

    public boolean isExistsByUserId(Long userId){
        return authRepository.existsByUserId(userId);
    }


    public UserRole getByUserId(Long userId) {
        return authRepository.findByUserId(userId).get().getRole();
    }
}
