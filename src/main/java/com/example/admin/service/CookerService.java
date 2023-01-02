package com.example.admin.service;

import com.example.admin.repository.AdminRepository;
import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CookerService {

    private final AdminRepository adminRepository;

    @Lazy
    public CookerService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }


    public List<AdminEntity> getCookerList() {
        return adminRepository.findByRole(UserRole.COOKER);
    }

    public boolean isCooker(Long userId) {
        return adminRepository.existsByUserIdAndRole(userId,UserRole.COOKER);
    }
}
