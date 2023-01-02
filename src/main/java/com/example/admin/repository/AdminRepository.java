package com.example.admin.repository;

import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends CrudRepository<AdminEntity, Integer> {
    List<AdminEntity> findByRoleAndVisible(UserRole role, Boolean visible);

    Optional<AdminEntity> findByPassword(String password);

    Optional<AdminEntity> findByPhone(String text);

    boolean existsByUserIdAndRoleAndVisible(Long userId, UserRole role,Boolean visible);


    List<AdminEntity> findByRoleAndBusy(UserRole role, boolean busy);

    AdminEntity findByUserId(Long userId);


    @Modifying
    @Transactional
    @Query("update AdminEntity a set a.busy=?2 where a.userId=?1")
    void changeStatus(Long userId, boolean busy);
}
