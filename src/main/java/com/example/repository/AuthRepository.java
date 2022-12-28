package com.example.repository;

import com.example.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuthRepository extends CrudRepository<ProfileEntity,Integer> {
    boolean existsByUserId(Long userId);

    boolean existsByPhone(String phone);

    Optional<ProfileEntity> findByUserId(Long userId);
}
