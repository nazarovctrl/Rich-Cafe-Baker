package com.example.admin.repository;

import com.example.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {
    boolean existsByPhone(String phone);


    Optional<AdminEntity> findByPhone(String phone);
}
