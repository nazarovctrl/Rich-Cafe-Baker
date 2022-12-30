package com.example.repository;

import com.example.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer> {


    List<ProfileEntity> findAll();

    Optional<ProfileEntity> findByPhone(String phone);

    Optional<ProfileEntity> getByUserId(Long id);

    boolean existsByUserId(Long userId);


    boolean existsByPhone(String phone);

    Optional<ProfileEntity> findByUserId(Long userId);

}
