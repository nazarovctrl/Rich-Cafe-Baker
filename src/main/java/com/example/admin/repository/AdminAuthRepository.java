package com.example.admin.repository;

import com.example.entity.AdminEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminAuthRepository extends CrudRepository<AdminEntity,Integer> {
    Optional<AdminEntity> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Optional<AdminEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
    
}
