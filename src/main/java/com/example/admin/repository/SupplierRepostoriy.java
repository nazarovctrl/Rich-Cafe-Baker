package com.example.admin.repository;

import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepostoriy extends CrudRepository<AdminEntity, Integer> {


    List<AdminEntity> findByRole(UserRole role);

    Optional<AdminEntity> findByPassword(String text);

    Optional<AdminEntity> findByPhone(String text);

    List<AdminEntity> findByRoleAndBusy(UserRole role, boolean busy);

    AdminEntity findByUserId(Long userId);


}
