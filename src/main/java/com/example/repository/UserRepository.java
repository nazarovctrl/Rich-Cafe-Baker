package com.example.repository;

import com.example.entity.AdminEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<AdminEntity,Integer> {

    Optional<AdminEntity> getByUserId(Long id);

}
