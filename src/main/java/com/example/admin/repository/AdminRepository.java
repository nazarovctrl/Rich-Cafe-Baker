package com.example.admin.repository;

import com.example.entity.AdminEntity;
import com.example.enums.ProfileRole;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends CrudRepository<AdminEntity,Integer> {
    List<AdminEntity> findByRole(UserRole role);
    Optional<AdminEntity> findByPassword(String password);

    Optional<AdminEntity> findByPhone(String text);

}
