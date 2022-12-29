package com.example.admin.repository;

import com.example.entity.MenuEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminMenuRepository extends CrudRepository<MenuEntity,Integer> {
    List<MenuEntity> findAll();

    Optional<MenuEntity> findByName(String name);
}
